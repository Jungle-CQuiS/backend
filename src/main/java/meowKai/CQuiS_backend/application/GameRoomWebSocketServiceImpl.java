package meowKai.CQuiS_backend.application;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.domain.*;
import meowKai.CQuiS_backend.dto.MultiRoomUserDto;
import meowKai.CQuiS_backend.dto.SelectQuizResult;
import meowKai.CQuiS_backend.dto.UserAnswer;
import meowKai.CQuiS_backend.dto.request.*;
import meowKai.CQuiS_backend.dto.response.*;
import meowKai.CQuiS_backend.infrastructure.GameRoomRepository;
import meowKai.CQuiS_backend.infrastructure.QuizRepository;
import meowKai.CQuiS_backend.infrastructure.RoomUserRepository;
import meowKai.CQuiS_backend.infrastructure.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GameRoomWebSocketServiceImpl implements GameRoomWebSocketService{

    @PersistenceContext
    private EntityManager entityManager;

    private final GameRoomRepository gameRoomRepository;
    private final RoomUserRepository roomUserRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;

    private final QuizService quizService; // 채점
    private final OpenViduService openViduService;

    private final SimpMessagingTemplate messagingTemplate; // 웹 소켓 통신으로 메시지 전달 시에 사용

    private final Map<Long, List<UserAnswer>> roomAnswers = new ConcurrentHashMap<>(); // 방 단위로 유저가 보내는 답안을 관리, roomId를 key로 사용

    @Override
    @Transactional
    public ResponseGetRoomInfoDto switchTeam(RequestSwitchTeamDto requestDto) {
        log.info("ws - 팀 변경 - 유저의 팀 바꾸기 요청: {}", requestDto);

        RoomUser foundRoomUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("ws - 팀 변경 - 존재하지 않는 유저입니다."));
        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("ws - 팀 변경 - 존재하지 않는 방입니다."));
        if(!foundRoomUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("ws - 팀 변경 - 방에 해당 유저가 존재하지 않습니다.");
        }

        foundRoomUser.changeTeam();
        roomUserRepository.save(foundRoomUser);

        // 영속성 컨텍스트를 비워서 변경사항 DB에 반영
        entityManager.flush();
        entityManager.clear();

        // 변경사항을 DB에 반영하고 새로 데이터를 받아 옴
        GameRoom updatedGameRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("ws - 팀 변경 - 존재하지 않는 방입니다."));
        ResponseGetRoomInfoDto responseDto = getResponseGetRoomInfoDto(updatedGameRoom);
        log.info("ws - 팀 변경 - 유저의 팀 바꾸기 결과: {}", responseDto);
        return responseDto;
    }

    @Override
    @Transactional
    public ResponseGetRoomInfoDto ready(RequestReadyDto requestDto) {
        log.info("ws - 레디 - 레디/레디 취소 요청: {}", requestDto);

        RoomUser foundRoomUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("ws - 레디 - 존재하지 않는 유저입니다."));
        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("ws - 레디 - 존재하지 않는 방입니다."));
        if(!foundRoomUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("ws - 레디 - 방에 해당 유저가 존재하지 않습니다.");
        }
        foundRoomUser.changeReady();
        roomUserRepository.save(foundRoomUser);

        if(canStartGame(foundRoom)) { // 게임을 시작할 수 있는 상태인지 확인
            if(!foundRoomUser.getIsReady()) {
                if(foundRoom.getGameStatus() == GameStatus.ALL_READY) {
                    changeGameStatus(foundRoom, GameStatus.STOP_READY);
                    foundRoom.changeGameStatus(GameStatus.WAITING); // 클라이언트에 STOP_READY 상태를 전달 후 다시 WAITING으로 변경
                    gameRoomRepository.save(foundRoom);
                }
            } else if(isAllReady(foundRoom)) {
                changeGameStatus(foundRoom, GameStatus.ALL_READY);
                gameRoomRepository.save(foundRoom);
            }
        }

        // 영속성 컨텍스트를 비워서 변경사항 DB에 반영
        entityManager.flush();
        entityManager.clear();

        // 변경사항을 DB에 반영하고 새로 데이터를 받아 옴
        GameRoom updatedGameRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("ws - 레디 - 존재하지 않는 방입니다."));
        ResponseGetRoomInfoDto responseDto = getResponseGetRoomInfoDto(updatedGameRoom);
        log.info("ws - 레디 - 레디/레디 취소 결과: {}", responseDto);
        return responseDto;
    }

    @Override
    @Transactional
    public ResponseGetRoomInfoDto kickUser(RequestKickUserDto requestDto) {
        log.info("ws - 강퇴 - 유저 강퇴 요청: {}", requestDto);

        // 방장 권한을 가진 유저가 방에 존재하는지 검토
        RoomUser foundRoomUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("ws - 강퇴 - 존재하지 않는 유저입니다."));
        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("ws - 강퇴 - 존재하지 않는 방입니다."));
        if(!foundRoomUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("ws - 강퇴 - 방에 해당 유저가 존재하지 않습니다.");
        }

        // 해당 유저에게 방장 권한이 있는지 체크
        boolean isHost = foundRoomUser.getRole() == RoomUserRole.HOST;
        if (!isHost) {
            throw new IllegalArgumentException("ws - 강퇴 - 방장만 유저를 강퇴할 수 있습니다.");
        }

        // 강퇴하려는 유저가 방에 존재하는지 검토
        RoomUser roomUserToKick = roomUserRepository.findById(requestDto.getKickRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("ws - 강퇴 - 강퇴하려는 유저가 존재하지 않습니다."));
        if(!roomUserToKick.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("ws - 강퇴 - 강퇴하려는 유저가 해당 방에 존재하지 않습니다.");
        }

        // 강퇴하려는 유저가 리더라면 리더를 양도
        if(roomUserToKick.getIsLeader()) {
            yieldLeader(foundRoom, roomUserToKick);
        }

        roomUserRepository.delete(roomUserToKick);
        foundRoom.removeUser();
        gameRoomRepository.save(foundRoom);

        // 영속성 컨텍스트를 비워서 변경사항 DB에 반영
        entityManager.flush();
        entityManager.clear();

        // 변경사항을 DB에 반영하고 새로 데이터를 받아 옴
        GameRoom updatedGameRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("ws - 강퇴 - 존재하지 않는 방입니다."));
        ResponseGetRoomInfoDto responseDto = getResponseGetRoomInfoDto(updatedGameRoom);
        log.info("ws - 강퇴 - 유저 강퇴 결과: {}", responseDto);
        return responseDto;
    }

    @Override
    @Transactional
    public ResponseGetRoomInfoDto changeHost(RequestYieldDto requestDto) {
        log.info("ws - 방장 위임 - 방장 위임 요청: {}", requestDto);

        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("ws - 방장 위임 - 존재하지 않는 방입니다."));
        RoomUser hostUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("ws - 방장 위임 - 방장이 존재하지 않습니다."));
        RoomUser nextHostUser = roomUserRepository.findById(requestDto.getYieldUserId()).orElseThrow(
                () -> new NoSuchElementException("ws - 방장 위임 - 방장을 위임받을 유저가 존재하지 않습니다."));

        if(!hostUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("ws - 방장 위임 - 방장이 해당 방에 존재하지 않습니다.");
        }
        if(!nextHostUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("ws - 방장 위임 - 방장을 위임받을 유저가 해당 방에 존재하지 않습니다.");
        }

        if(hostUser.getRole() != RoomUserRole.HOST) {
            throw new IllegalStateException("ws - 방장 위임 - 방장 권한이 없는 유저입니다.");
        }

        hostUser.changeRole();
        nextHostUser.changeRole();
        roomUserRepository.save(hostUser);
        roomUserRepository.save(nextHostUser);

        // 영속성 컨텍스트를 비워서 변경사항 DB에 반영
        entityManager.flush();
        entityManager.clear();

        // 변경사항을 DB에 반영하고 새로 데이터를 받아 옴
        GameRoom updatedGameRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("ws - 방장 위임 - 존재하지 않는 방입니다."));
        ResponseGetRoomInfoDto responseDto = getResponseGetRoomInfoDto(updatedGameRoom);
        log.info("ws - 방장 위임 결과: {}", responseDto);
        return responseDto;
    }

    @Override
    @Transactional
    public ResponseGetRoomInfoDto changeLeader(RequestYieldDto requestDto) {
        log.info("ws - 리더 위임 - 리더 위임 요청: {}", requestDto);

        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("ws - 리더 위임 - 존재하지 않는 방입니다."));
        RoomUser leaderUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("ws - 리더 위임 - 리더가 존재하지 않습니다."));
        RoomUser nextLeaderUser = roomUserRepository.findById(requestDto.getYieldUserId()).orElseThrow(
                () -> new NoSuchElementException("ws - 리더 위임 - 리더를 위임받을 유저가 존재하지 않습니다."));

        if(!leaderUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("ws - 리더 위임 - 리더가 해당 방에 존재하지 않습니다.");
        }
        if(!nextLeaderUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("ws - 리더 위임 - 리더를 위임받을 유저가 해당 방에 존재하지 않습니다.");
        }

        if(!leaderUser.getIsLeader()) {
            throw new IllegalStateException("ws - 리더 위임 - 리더 권한이 없는 유저입니다.");
        }

        if(leaderUser.getTeam() != nextLeaderUser.getTeam()) {
            throw new IllegalStateException("ws - 리더 위임 - 리더를 양도하려는 유저와 같은 팀이 아닙니다.");
        }

        leaderUser.changeLeader();
        nextLeaderUser.changeLeader();
        roomUserRepository.save(leaderUser);
        roomUserRepository.save(nextLeaderUser);

        // 영속성 컨텍스트를 비워서 변경사항 DB에 반영
        entityManager.flush();
        entityManager.clear();

        // 변경사항을 DB에 반영하고 새로 데이터를 받아 옴
        GameRoom updatedGameRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("ws - 리더 위임 - 존재하지 않는 방입니다."));
        ResponseGetRoomInfoDto responseDto = getResponseGetRoomInfoDto(updatedGameRoom);
        log.info("ws - 리더 위임 결과: {}", responseDto);
        return responseDto;
    }

    @Override
    @Transactional
    public ResponseGetRoomInfoDto exit(RequestExitDto requestDto) {
        log.info("ws - 방 나가기 요청: {}", requestDto);

        RoomUser foundRoomUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("ws - 퇴장 - 존재하지 않는 유저입니다."));
        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("ws - 퇴장 - 존재하지 않는 방입니다."));
        if(!foundRoomUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("ws - 퇴장 - 방에 해당 유저가 존재하지 않습니다.");
        }

        boolean shouldDeleteRoom = countRoomUser(foundRoom) <= 1;

        // 나가는 유저에게 권한이 있다면 권한을 양도
        if(!shouldDeleteRoom) {
            if (foundRoomUser.getRole() == RoomUserRole.HOST) {
                yieldHost(foundRoom, foundRoomUser);
            }
            if (foundRoomUser.getIsLeader()) {
                yieldLeader(foundRoom, foundRoomUser);
            }
        }

        foundRoom.removeUser();
        roomUserRepository.delete(foundRoomUser);

        if(shouldDeleteRoom) {
            openViduService.closeSession(foundRoom.getSessionId());
            gameRoomRepository.delete(foundRoom);
            log.info("ws - 퇴장 - 방 삭제: {}", foundRoom.getId());
        } else {
            gameRoomRepository.save(foundRoom);
        }

        // 영속성 컨텍스트를 비워서 변경사항 DB에 반영
        entityManager.flush();
        entityManager.clear();

        // 변경사항을 DB에 반영하고 새로 데이터를 받아 옴
        return gameRoomRepository.findById(requestDto.getRoomId())
                .map(room -> {
                    ResponseGetRoomInfoDto responseDto = getResponseGetRoomInfoDto(room);
                    log.info("ws - 퇴장 - 방 나가기 완료: {}", responseDto);
                    return responseDto;
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public ResponseGetRoomInfoDto joinRoom(RequestWebSocketJoinRoomDto requestDto) {
        log.info("ws - 방 입장 요청: {}", requestDto);

        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("ws - 입장 - 존재하지 않는 방입니다."));
        User joinUser = userRepository.findByUuid(requestDto.getUuid()).orElseThrow(
                () -> new NoSuchElementException("ws - 입장 - 존재하지 않는 유저입니다."));

        if (countRoomUser(foundRoom).equals(foundRoom.getMaxUsers())) {
            throw new IllegalStateException("ws - 입장 - 방이 꽉 찼습니다.");
        }

        RoomUser joinedRoomUser = RoomUser.createRoomUser(foundRoom, joinUser, RoomUserRole.GUEST, RoomUserTeam.BLUE);
        if (isTeamFull(foundRoom, RoomUserTeam.BLUE)) {
            joinedRoomUser.changeTeam();
        }

        // 방이 비어있으면 joinedRoomUser를 host, leader로
        System.out.println(countRoomUser(foundRoom));
        if(countRoomUser(foundRoom) <= 0) {
            joinedRoomUser.changeRole();
            joinedRoomUser.changeLeader();
            log.info("ws - 입장 - 첫 번째 유저입니다: {}", joinedRoomUser.getId());
        } else {
            // 비어있는 팀이 있으면 joinedRoomUser를 해당 팀으로 보내고 리더로 설정
            Arrays.stream(RoomUserTeam.values())
                    .filter(team -> isTeamEmpty(foundRoom, team))
                    .findFirst()
                    .ifPresent(joinedRoomUser::assignTeamLeader);
        }

        roomUserRepository.save(joinedRoomUser);
        foundRoom.getRoomUsers().add(joinedRoomUser);
        foundRoom.addUser();
        gameRoomRepository.save(foundRoom);

        entityManager.flush();
        entityManager.clear();

        GameRoom updatedGameRoom = gameRoomRepository.findById(foundRoom.getId()).orElseThrow(
                () -> new NoSuchElementException("ws - 입장 - 존재하지 않는 방입니다."));
        ResponseGetRoomInfoDto responseDto = getResponseGetRoomInfoDto(updatedGameRoom);
        log.info("ws - 입장 - 방 입장 결과: {}", responseDto);
        return responseDto;
    }

    // 방 입장 후 생성된 RoomUser의 id 반환
    @Override
    public ResponseJoinRoomDto getRoomUserId(RequestJoinRoomDto requestDto) {
        log.info("ws - roomuser id 반환 요청: {}", requestDto);

        User joinUser = userRepository.findByUuid(requestDto.getUuid()).orElseThrow(
                () -> new NoSuchElementException("입장 - 존재하지 않는 유저입니다."));

        if(joinUser.getRoomUser() == null) {
            throw new NoSuchElementException("입장 - 유저의 방 입장 정보가 없습니다.");
        }

        ResponseJoinRoomDto responseDto = ResponseJoinRoomDto.builder()
                .roomUserId(joinUser.getRoomUser().getId())
                .build();

        log.info("ws - roomuser id 반환 결과: {}", responseDto);
        return responseDto;
    }

    // 수비 팀 리더가 선택을 바꿀 때마다 수비 팀 전원에게 전달
    @Override
    public SelectQuizResult<ResponseSelectOptionDto> selectOption(RequestSelectQuizDto requestDto) {
        log.info("ws - 수비 팀 리더 선택 변경 요청: {}", requestDto);

        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("ws - 수비 팀 리더 선택 변경 - 존재하지 않는 방입니다."));

        // 수비팀 찾기 -> GameRoom 클래스의 메소드로 빼야할까?
        RoomUserTeam defenseTeamColor = (foundRoom.getTeams().get(0).getTeamStatus() == TeamStatus.DEFENSE
                ? foundRoom.getTeams().get(0) : foundRoom.getTeams().get(1))
                .getTeamColor();

        ResponseSelectOptionDto responseDto = ResponseSelectOptionDto.builder()
                .responseStatus(requestDto.getResponseStatus())
                .number(requestDto.getNumber())
                .build();

        log.info("ws - 수비 팀 리더 선택 변경 결과: {}", responseDto);
        return new SelectQuizResult<ResponseSelectOptionDto>(responseDto, defenseTeamColor);
    }

    // 수비 팀 리더가 선택한 퀴즈를 수비 팀 전원에게 전달
    @Override
    @Transactional
    public SelectQuizResult<ResponseSelectQuizDto> selectQuiz(RequestSelectQuizDto requestDto) {
        log.info("ws - 퀴즈 선택 & 전달 요청: {}", requestDto);

        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("ws - 퀴즈 선택 & 전달 - 존재하지 않는 방입니다."));

        // 수비팀 찾기 -> GameRoom 클래스의 메소드로 빼야할까?
        RoomUserTeam defenseTeamColor = (foundRoom.getTeams().get(0).getTeamStatus() == TeamStatus.DEFENSE
                ? foundRoom.getTeams().get(0) : foundRoom.getTeams().get(1))
                .getTeamColor();

        foundRoom.saveCurrentQuizId(requestDto.getNumber()); // gameRoom에 currentQuizId 저장
        gameRoomRepository.save(foundRoom);
        roomAnswers.remove(foundRoom.getId()); // 같은 방에서 이전에 제출된 답안들을 삭제 -> 답안 선택 구현 시 그쪽으로 옮길 것
        log.info("ws - 퀴즈 선택 & 전달 - 이전 답안 초기화 roomId: {}", foundRoom.getId());

        Quiz foundQuiz = quizRepository.findById(requestDto.getNumber()).orElseThrow(
                () -> new NoSuchElementException("ws - 퀴즈 선택 & 전달 - 존재하지 않는 퀴즈입니다."));

        // QuizType에 따라 responseDto 만들어 반환
        ResponseSelectQuizDto responseDto = foundQuiz.getType() == QuizType.SHORT ?
                ResponseSelectShortQuizDto.builder()
                        .quizId(foundQuiz.getId())
                        .name(foundQuiz.getName())
                        .categoryType(foundQuiz.getCategory().getCategory())
                        .type(foundQuiz.getType())
                        .build() :
                ResponseSelectChoiceQuizDto.builder()
                        .quizId(foundQuiz.getId())
                        .name(foundQuiz.getName())
                        .categoryType(foundQuiz.getCategory().getCategory())
                        .type(foundQuiz.getType())
                        .choice1(foundQuiz.getChoiceAnsQuiz().getChoice1())
                        .choice2(foundQuiz.getChoiceAnsQuiz().getChoice2())
                        .choice3(foundQuiz.getChoiceAnsQuiz().getChoice3())
                        .choice4(foundQuiz.getChoiceAnsQuiz().getChoice4())
                        .build();

        log.info("ws - 퀴즈 선택 & 전달 결과: {}", responseDto);

        return new SelectQuizResult<ResponseSelectQuizDto>(responseDto, defenseTeamColor);
    }

    // 수비 팀 팀원들이 제출한 답안을 roomId를 key로 저장
    @Override
    public void submitPersonal(RequestSubmitPersonalDto requestDto) {
        log.info("ws - 수비팀 답안 제출: {}", requestDto);

        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("ws - 수비팀 답안 제출 - 존재하지 않는 방입니다."));

        roomAnswers.computeIfAbsent(foundRoom.getId(),
                k -> Collections.synchronizedList(new ArrayList<>()))   // roomAnswers에 roomId가 없는 경우 동기화된 리스트를 새로 만듦
                .add(new UserAnswer(requestDto.getRoomUserId(), requestDto.getAnswer()));

        log.info("ws - 수비팀 답안 제출 - 답안 리스트: {}", roomAnswers.get(foundRoom.getId()));
        log.info("ws - 수비팀 답안 제출 - 제출된 답안의 수: {}, 수비팀 유저 수: {}", roomAnswers.get(foundRoom.getId()).size(), foundRoom.getDefenseTeamUserCount());
        if(roomAnswers.get(foundRoom.getId()).size() >= foundRoom.getDefenseTeamUserCount()) {
            submitAll(foundRoom.getId());
        }
    }

    // 수비팀 전체가 답안을 제출하면 알림을 보냄
    private void submitAll(Long roomId) {

        ResponseSubmitAllDto responseDto = ResponseSubmitAllDto.builder()
                .responseStatus(ResponseStatus.ALL_SUBMIT)
                .build();

        log.info("ws - 수비팀 전원 답안 제출 : {}", responseDto);

        for (RoomUserTeam teamColor : RoomUserTeam.values()) {
            messagingTemplate.convertAndSend(
                    "/topic/game/" + roomId + "/" + teamColor.toString().toLowerCase(),
                    responseDto
            );
        }
    }

    // // 수비 팀 리더가 최종 답안을 제출, 채점 및 다음 문제를 위한 세팅, hp 변경 알림, 게임 종료 알림 수행
    @Override
    @Transactional
    public ResponseSubmitTeamDto submitTeam(RequestSubmitTeamDto requestDto) {
        log.info("ws - 최종 답안 제출 요청: {}", requestDto);

        // 최종 답안과 현재 퀴즈를 가져옴
        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("ws - 최종 답안 제출 - 존재하지 않는 방입니다."));

        Quiz foundQuiz = quizRepository.findById(foundRoom.getCurrentQuizId()).orElseThrow(
                () -> new NoSuchElementException("ws - 최종 답안 제출 - 존재하지 않는 퀴즈입니다."));

        UserAnswer userAnswer = roomAnswers.get(requestDto.getRoomId()).get(requestDto.getNumber().intValue());

        // 채점
        RequestGradeDto requestGradeDto = RequestGradeDto.builder().quizId(foundQuiz.getId()).userInput(userAnswer.getAnswer()).build();
        ResponseGradeDto responseGradeDto = quizService.checkGrade(requestGradeDto);


        // 틀렸다면 -> 체력 감소
        Team defenseTeam = foundRoom.getTeams().get(0).getTeamStatus() == TeamStatus.DEFENSE
                ? foundRoom.getTeams().get(0) : foundRoom.getTeams().get(1);
        if(!responseGradeDto.getIsCorrect()) {
            // 수비팀 찾기
            defenseTeam.decreaseHp();
        } else {
            defenseTeam.addCorrectCount();
        }

        // responseDto 만들어 반환
        ResponseSubmitTeamDto responseDto = ResponseSubmitTeamDto.builder()
                .isCorrect(responseGradeDto.getIsCorrect())
                .answer(responseGradeDto.getAnswer())
                .teamHp(defenseTeam.getTeamHp())
                .responseStatus(ResponseStatus.ROUND_END)
                .nextOffenseTeam(defenseTeam.getTeamColor())
                .build();

        log.info("ws - 최종 답안 제출 결과: {}", requestDto);

        // 다음 문제를 위한 세팅 -> 진행된 문제 + 1, 공격 수비 변경
        foundRoom.addQuizCount();
        foundRoom.changeTeamStatus();
        return responseDto;
    }

    // (SUB)게임 종료 조건을 체크
    @Override
    @Transactional
    public Boolean isGameover(Long roomId) {
        log.info("ws - 게임 종료 조건 체크 - roomId: {}", roomId);

        GameRoom foundRoom = gameRoomRepository.findById(roomId).orElseThrow(
                () -> new NoSuchElementException("ws - 게임 종료 조건 체크 - 존재하지 않는 방입니다."));

        // 수비팀의 HP가 0보다 크지 않다면 or 퀴즈가 10번 진행되었다면 게임 종료
        boolean isDefenseTeamDead = (foundRoom.getTeams().get(0).getTeamStatus() == TeamStatus.DEFENSE
                ? foundRoom.getTeams().get(0)
                : foundRoom.getTeams().get(1)).getTeamHp() <= 0;

        boolean isMaxQuizReached = foundRoom.getQuizCount() >= 4;

        if(isDefenseTeamDead || isMaxQuizReached) {
            RoomUserTeam winningTeamColor;
            if(isDefenseTeamDead) {
                winningTeamColor = (foundRoom.getTeams().get(0).getTeamStatus() == TeamStatus.OFFENSE
                        ? foundRoom.getTeams().get(0)
                        : foundRoom.getTeams().get(1))
                        .getTeamColor();
            } else {
                winningTeamColor = (foundRoom.getTeams().get(0).getCorrectCount() >= foundRoom.getTeams().get(1).getCorrectCount()
                        ? foundRoom.getTeams().get(0)
                        : foundRoom.getTeams().get(1))
                        .getTeamColor();
            }

            foundRoom.changeGameStatus(GameStatus.GAME_END);
            gameRoomRepository.save(foundRoom);

            ResponseIsGameoverDto responseDto = ResponseIsGameoverDto.builder()
                    .responseStatus(ResponseStatus.GAME_END)
                    .teamColor(winningTeamColor)
                    .gameStatus(foundRoom.getGameStatus())
                    .build();

            for (RoomUserTeam teamColor : RoomUserTeam.values()) {
                messagingTemplate.convertAndSend(
                        "/topic/game/" + foundRoom.getId() + "/" + teamColor.toString().toLowerCase(),
                        responseDto
                );
            }

        }

        log.info("ws - 게임 종료 조건 체크 결과 - isGameover: {}", (isDefenseTeamDead || isMaxQuizReached));
        return (isDefenseTeamDead || isMaxQuizReached);
    }

    /**
     * GameRoom을 인자로 넘겨주면 해당 방에 있는
     * 각 유저의 정보를 바탕으로 MultiRoomUserDto를 생성한 뒤
     * 그것을 리스트로 만들고
     * ResponseGetRoomInfoDto에 넣어서 반환
     */
    private static ResponseGetRoomInfoDto getResponseGetRoomInfoDto(GameRoom foundRoom) {
        return ResponseGetRoomInfoDto
                .builder()
                .usersData(foundRoom.getRoomUsers().stream()
                        .map(user -> MultiRoomUserDto.builder()
                                .roomUserId(user.getId())
                                .username(user.getUser().getUsername())
                                .honorCount(user.getUser().getUserStatistics().getHonorCount())
                                .role(user.getRole())
                                .team(user.getTeam())
                                .isLeader(user.getIsLeader())
                                .isReady(user.getIsReady()).build())
                        .toList())
                .build();
    }

    private void yieldHost(GameRoom foundRoom, RoomUser yieldRoomUser) {
        List<RoomUser> currentTeamUsers = roomUserRepository.findAllByGameRoom(foundRoom);

        // 현재 리더인 유저와 같은 팀인 유저가 있는지 체크(본인 제외)
        boolean hasTeamMembers = currentTeamUsers.stream()
                .anyMatch(user -> !user.getId()
                        .equals(yieldRoomUser.getId()));

        if(hasTeamMembers) {
            currentTeamUsers.stream()
                    .filter(user -> !user.getId().equals(yieldRoomUser.getId()))
                    .findFirst()
                    .ifPresent(newHost -> {
                        newHost.changeRole();   // 새로운 방장의 권한 추가
                        yieldRoomUser.changeRole();  // 기존 방장의 권한 제거
                        roomUserRepository.save(newHost);
                        roomUserRepository.save(yieldRoomUser);
                        entityManager.flush();
                        entityManager.clear();
                    });
        }
    }

    private void yieldLeader(GameRoom foundRoom, RoomUser yieldRoomUser) {
        List<RoomUser> currentTeamUsers = roomUserRepository.findAllByGameRoomAndTeam(foundRoom, yieldRoomUser.getTeam());

        // 현재 리더인 유저와 같은 팀인 유저가 있는지 체크(본인 제외)
        boolean hasTeamMembers = currentTeamUsers.stream()
                .anyMatch(user -> !user.getId()
                        .equals(yieldRoomUser.getId()));

        if(hasTeamMembers) {
            currentTeamUsers.stream()
                    .filter(user -> !user.getId().equals(yieldRoomUser.getId()))
                    .findFirst()
                    .ifPresent(newLeader -> {
                        newLeader.changeLeader();   // 새로운 리더의 권한 추가
                        yieldRoomUser.changeLeader();  // 기존 리더의 권한 제거
                        roomUserRepository.save(newLeader);
                        roomUserRepository.save(yieldRoomUser);
                        entityManager.flush();
                        entityManager.clear();
                    });
        }
    }


    // 현재 방에 몇 명이 있는지 확인
    private Integer countRoomUser(GameRoom gameRoom) {
        return (Integer) gameRoom.getRoomUsers().size();
    }

    // 모든 유저가 레디 상태인지 확인
    private boolean isAllReady(GameRoom gameRoom) {
        return gameRoom.getRoomUsers().stream()
                .filter(RoomUser::getIsReady)
                .count()
                == countRoomUser(gameRoom);
    }

    // 유저가 들어오는 상황에서 팀이 비어있는지 확인
    private boolean isTeamEmpty(GameRoom gameRoom, RoomUserTeam teamColor) {
        return gameRoom.getRoomUsers().stream()
                .noneMatch(user -> user.getTeam() == teamColor);
    }

    // 팀이 가득 찼는지 확인
    private boolean isTeamFull(GameRoom gameRoom, RoomUserTeam teamColor) {
        return gameRoom.getRoomUsers().stream()
                .filter(user -> user.getTeam() == teamColor)
                .count()
                >= (gameRoom.getMaxUsers() / 2);
    }

    // 게임 시작이 가능한지(양팀에 1명 이상의 유저가 존재하는지) 확인
    private boolean canStartGame(GameRoom gameRoom) {
        return gameRoom.getRoomUsers().stream()
                .map(RoomUser::getTeam)
                .distinct()
                .count() == 2;
    }

    // (SUB)모든 유저 레디, 카운트다운 도중 레디 취소로 gameStatus에 변화가 생길 경우 클라이언트에 알림 전송
    private void changeGameStatus(GameRoom gameRoom, GameStatus gameStatus) {
        gameRoom.changeGameStatus(gameStatus);
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + gameRoom.getId() + "/status",
                ResponseChangeGameStatusDto.builder()
                        .gameStatus(gameStatus)
                        .build());
    }

    public List<UserAnswer> getRoomAnswers(Long roomId) {
        return roomAnswers.getOrDefault(roomId, new ArrayList<>());
    }
}