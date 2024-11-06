package meowKai.CQuiS_backend.application;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.domain.*;
import meowKai.CQuiS_backend.dto.MultiRoomDto;
import meowKai.CQuiS_backend.dto.MultiRoomUserDto;
import meowKai.CQuiS_backend.dto.request.*;
import meowKai.CQuiS_backend.dto.response.*;
import meowKai.CQuiS_backend.infrastructure.GameRoomRepository;
import meowKai.CQuiS_backend.infrastructure.RoomUserRepository;
import meowKai.CQuiS_backend.infrastructure.TeamRepository;
import meowKai.CQuiS_backend.infrastructure.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static meowKai.CQuiS_backend.domain.TeamStatus.OFFENSE;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GameRoomServiceImpl implements GameRoomService {

    @PersistenceContext
    private EntityManager entityManager;

    private final GameRoomRepository gameRoomRepository;
    private final RoomUserRepository roomUserRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    private final GameRoomWebSocketServiceImpl gameRoomWebSocketService;
    private final QuizService quizService;

    private final ReentrantLock gameStartLock = new ReentrantLock(); // 게임 시작 시 팀 생성을 포함한 세팅이 한 번만 발생하도록 하기 위한 락

    // 입장할 수 있는 멀티 게임 방 조회하기
    @Override
    public ResponseGetMultiRoomListDto getMultiRoomList(int start, int limit) {
        log.info("멀티 게임 방 리스트 조회 요청");

        Pageable pageable = PageRequest.of(start, limit);
        Page<GameRoom> gameRoomList = gameRoomRepository.findByGameStatus(GameStatus.WAITING, pageable); // gameStatus가 WAITING인 방만 가져옴

        if(gameRoomList.isEmpty()) {
            ResponseGetMultiRoomListDto responseDto = ResponseGetMultiRoomListDto.builder()
                    .rooms(new ArrayList<>())
                    .build();

            log.info("멀티 게임 방 리스트 조회 결과(비어있음): {}", responseDto);
            return responseDto;
        }

        List<MultiRoomDto> multiRoomList = gameRoomList.stream()
                .map(gameRoom -> MultiRoomDto.builder()
                        .gameRoomId(gameRoom.getId())
                        .name(gameRoom.getName())
                        .currentUsers(gameRoom.getCurrentUsers())
                        .maxUsers(gameRoom.getMaxUsers())
                        .isLocked(gameRoom.getPassword() != null)
                        .build())
                .collect(Collectors.toList());

        ResponseGetMultiRoomListDto responseDto = ResponseGetMultiRoomListDto.builder()
                .rooms(multiRoomList)
                .nextPageNumber(start + gameRoomList.getTotalPages()) // 마지막 페이지 번호(프론트 입장에서 다음 페이지 로드 시 해당 번호부터 limit 갯수만큼 불러오면 됨)
                .build();

        log.info("멀티 게임 방 리스트 조회 결과: {}", responseDto);
        return responseDto;
    }

    // TODO: 중간 테이블 생성
    // 멀티 게임 방 생성하기
    @Override
    @Transactional
    public ResponseCreateMultiRoomDto createMultiRoom(RequestCreateMultiRoomDto requestDto) {
        log.info("멀티 게임 방 생성 - 멀티 게임 방 생성 요청: {}", requestDto);
        GameRoom createdRoom = GameRoom.createGameRoom(
                requestDto.getName(),
                requestDto.getMaxUser(),
                requestDto.getPassword()
        );

        gameRoomRepository.save(createdRoom);

        ResponseCreateMultiRoomDto responseDto = ResponseCreateMultiRoomDto.builder()
                .roomId(createdRoom.getId())
                .build();
        log.info("멀티 게임 방 생성 - 멀티 게임 방 생성 결과: {}", responseDto);
        return responseDto;
    }

    // 유저의 팀 바꾸기
    @Override
    @Transactional
    public ResponseSwitchTeamDto switchTeam(RequestSwitchTeamDto requestDto) {
        log.info("팀 변경 - 유저의 팀 바꾸기 요청: {}", requestDto);

        // TODO: 메서드화 해보기 나중에...
        RoomUser foundRoomUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("팀 변경 - 존재하지 않는 유저입니다."));
        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("팀 변경 - 존재하지 않는 방입니다."));
        if(!foundRoomUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("팀 변경 - 방에 해당 유저가 존재하지 않습니다.");
        }

        foundRoomUser.changeTeam();
        roomUserRepository.save(foundRoomUser);

        ResponseSwitchTeamDto responseDto = ResponseSwitchTeamDto.builder()
                .roomUserId(foundRoomUser.getId())
                .team(foundRoomUser.getTeam())
                .build();
        log.info("팀 변경 - 유저의 팀 바꾸기 결과: {}", responseDto);
        return responseDto;
    }

    // 준비하기
    @Override
    @Transactional
    public ResponseReadyDto ready(RequestReadyDto requestDto) {
        log.info("레디 - 레디/레디 취소 요청: {}", requestDto);

        RoomUser foundRoomUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("레디 - 존재하지 않는 유저입니다."));
        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("레디 - 존재하지 않는 방입니다."));
        if(!foundRoomUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("레디 - 방에 해당 유저가 존재하지 않습니다.");
        }

        foundRoomUser.changeReady();
        roomUserRepository.save(foundRoomUser);

        ResponseReadyDto responseDto = ResponseReadyDto.builder()
                .roomUserId(foundRoomUser.getId())
                .isReady(foundRoomUser.getIsReady())
                .build();
        log.info("레디 - 레디/레디 취소 결과: {}", responseDto);
        return responseDto;
    }

    // 유저 강퇴(방장 권한 필요)
    @Override
    @Transactional
    public ResponseKickUserDto kickUser(RequestKickUserDto requestDto) {

        log.info("강퇴 - 유저 강퇴 요청: {}", requestDto);

        // 방장 권한을 가진 유저가 방에 존재하는지 검토
        RoomUser foundRoomUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("강퇴 - 존재하지 않는 유저입니다."));
        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("강퇴 - 존재하지 않는 방입니다."));
        if(!foundRoomUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("강퇴 - 방에 해당 유저가 존재하지 않습니다.");
        }

        // 해당 유저에게 방장 권한이 있는지 체크
        boolean isHost = foundRoomUser.getRole() == RoomUserRole.HOST;
        if (!isHost) {
            throw new IllegalArgumentException("강퇴 - 방장만 유저를 강퇴할 수 있습니다.");
        }

        // 강퇴하려는 유저가 방에 존재하는지 검토
        RoomUser roomUserToKick = roomUserRepository.findById(requestDto.getKickRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("강퇴 - 강퇴하려는 유저가 존재하지 않습니다."));
        if(!roomUserToKick.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("강퇴 - 강퇴하려는 유저가 해당 방에 존재하지 않습니다.");
        }

        // 강퇴하려는 유저가 리더라면 리더를 양도
        if(roomUserToKick.getIsLeader()) {
            yieldLeader(foundRoom, roomUserToKick);
        }

        roomUserRepository.delete(roomUserToKick);
        foundRoom.removeUser();
        gameRoomRepository.save(foundRoom);

        ResponseKickUserDto responseDto = ResponseKickUserDto.builder()
                .kickedRoomUserId(roomUserToKick.getId())
                .build();
        log.info("강퇴 - 유저 강퇴 결과: {}", responseDto);
        return responseDto;
    }


    // 방장을 변경
    @Override
    @Transactional
    public ResponseYieldDto changeHost(RequestYieldDto requestDto) {
        log.info("방장 위임 - 방장 위임 요청: {}", requestDto);

        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("방장 위임 - 존재하지 않는 방입니다."));
        RoomUser hostUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("방장 위임 - 방장이 존재하지 않습니다."));
        RoomUser nextHostUser = roomUserRepository.findById(requestDto.getYieldUserId()).orElseThrow(
                () -> new NoSuchElementException("방장 위임 - 방장을 위임받을 유저가 존재하지 않습니다."));

        if(!hostUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("방장 위임 - 방장이 해당 방에 존재하지 않습니다.");
        }
        if(!nextHostUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("방장 위임 - 방장을 위임받을 유저가 해당 방에 존재하지 않습니다.");
        }

        if(hostUser.getRole() != RoomUserRole.HOST) {
            throw new IllegalStateException("방장 위임 - 방장 권한이 없는 유저입니다.");
        }

        hostUser.changeRole();
        nextHostUser.changeRole();
        roomUserRepository.save(hostUser);
        roomUserRepository.save(nextHostUser);

        ResponseYieldDto responseDto = ResponseYieldDto.builder()
                .yieldedUserId(nextHostUser.getId())
                .build();
        log.info("방장 위임 - 방장 위임 결과: {}", responseDto);
        return responseDto;
    }

    // 리더를 변경
    @Override
    @Transactional
    public ResponseYieldDto changeLeader(RequestYieldDto requestDto) {
        log.info("리더 위임 - 리더 위임 요청: {}", requestDto);

        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("리더 위임 - 존재하지 않는 방입니다."));
        RoomUser leaderUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("리더 위임 - 리더가 존재하지 않습니다."));
        RoomUser nextLeaderUser = roomUserRepository.findById(requestDto.getYieldUserId()).orElseThrow(
                () -> new NoSuchElementException("리더 위임 - 리더를 위임받을 유저가 존재하지 않습니다."));

        if(!leaderUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("리더 위임 - 리더가 해당 방에 존재하지 않습니다.");
        }
        if(!nextLeaderUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("리더 위임 - 리더를 위임받을 유저가 해당 방에 존재하지 않습니다.");
        }

        if(!leaderUser.getIsLeader()) {
            throw new IllegalStateException("리더 위임 - 리더 권한이 없는 유저입니다.");
        }

        if(leaderUser.getTeam() != nextLeaderUser.getTeam()) {
            throw new IllegalStateException("리더 위임 - 리더를 양도하려는 유저와 같은 팀이 아닙니다.");
        }

        leaderUser.changeLeader();
        nextLeaderUser.changeLeader();
        roomUserRepository.save(leaderUser);
        roomUserRepository.save(nextLeaderUser);

        ResponseYieldDto responseDto = ResponseYieldDto.builder()
                .yieldedUserId(nextLeaderUser.getId())
                .build();
        log.info("리더 위임 - 리더 위임 결과: {}", responseDto);
        return responseDto;
    }

    // 방 퇴장
    @Override
    @Transactional
    public ResponseExitDto exit(RequestExitDto requestDto) {
        log.info("퇴장 - 방 나가기 요청: {}", requestDto);

        RoomUser foundRoomUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("퇴장 - 존재하지 않는 유저입니다."));
        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("퇴장 - 존재하지 않는 방입니다."));
        if(!foundRoomUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("퇴장 - 방에 해당 유저가 존재하지 않습니다.");
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
            gameRoomRepository.delete(foundRoom);
            log.info("퇴장 - 방 삭제: {}", foundRoom.getId());
        } else {
            gameRoomRepository.save(foundRoom);
        }

        ResponseExitDto responseDto = ResponseExitDto.builder()
                .roomUserId(foundRoomUser.getId())
                .build();
        log.info("퇴장 - 방 나가기 완료: {}", responseDto);
        return responseDto;
    }

    // 방 입장 - ws 통신 후 생성된 RoomUser의 id 반환
    @Override
    public ResponseJoinRoomDto joinRoom(RequestJoinRoomDto requestDto) {
        log.info("입장 - 방 입장 요청: {}", requestDto);

        User joinUser = userRepository.findByUuid(requestDto.getUuid()).orElseThrow(
                () -> new NoSuchElementException("입장 - 존재하지 않는 유저입니다."));

        if(joinUser.getRoomUser() == null) {
            throw new NoSuchElementException("입장 - 유저의 방 입장 정보가 없습니다.");
        }

        ResponseJoinRoomDto responseDto = ResponseJoinRoomDto.builder()
                .roomUserId(joinUser.getRoomUser().getId())
                .build();

        log.info("입장 - 방 입장 결과: {}", responseDto);
        return responseDto;
    }

    // 방 입장하고 방에 대한 정보 가져오기
    @Override
    public ResponseGetRoomInfoDto getRoomInfo(Long roomId) {
        log.info("방 내부 정보 조회 - 방 정보 조회 요청: {}", roomId);

        GameRoom gameRoom = gameRoomRepository.findById(roomId).orElseThrow(
                () -> new NoSuchElementException("방 내부 정보 조회 - 존재하지 않는 방입니다."));

        List<RoomUser> roomUsers = roomUserRepository.findAllByGameRoom(gameRoom);
        List<MultiRoomUserDto> roomUserInfoList = roomUsers.stream()
                .map(roomUser -> MultiRoomUserDto.builder()
                        .roomUserId(roomUser.getId())
                        .username(roomUser.getUser().getUsername())
                        .honorCount(roomUser.getUser().getUserStatistics().getHonorCount())
                        .role(roomUser.getRole())
                        .team(roomUser.getTeam())
                        .isLeader(roomUser.getIsLeader())
                        .isReady(roomUser.getIsReady())
                        .build())
                .toList();

        ResponseGetRoomInfoDto responseDto = ResponseGetRoomInfoDto.builder()
                .usersData(roomUserInfoList)
                .build();

        log.info("방 내부 정보 조회 - 방 정보 조회 결과: {}", responseDto);
        return responseDto;
    }

    @Override
    public ResposeCheckPasswordDto checkPassword(RequestCheckPasswordDto requestDto) {
        log.info("비밀번호 - 비밀 방 비밀번호 입력: {}", requestDto);
        GameRoom gameRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("비밀번호 - 존재하지 않는 방입니다."));

        ResposeCheckPasswordDto responseDto = ResposeCheckPasswordDto.builder()
                .isCorrect(Objects.equals(gameRoom.getPassword(), requestDto.getPassword()))
                .build();

        log.info("비밀번호 - 비밀 방 비밀번호 입력 결과: {}", responseDto);
        return responseDto;
    }

    @Override
    @Transactional
    public ResponseGiveHonorDto giveHonor(RequestGiveHonorDto requestDto) {
        log.info("명예 - 명예 주기 요청: {}", requestDto);

        RoomUser roomUser = roomUserRepository.findById(requestDto.getHonorRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("명예 - 존재하지 않는 유저입니다."));

        roomUser.getUser().getUserStatistics().addHonorCount();

        ResponseGiveHonorDto responseDto = ResponseGiveHonorDto.builder()
                .roomUserId(roomUser.getId())
                .build();

        log.info("명예 - 명예 주기 결과: {}", responseDto);
        return responseDto;
    }

    /**
     * 게임 시작 알림을 받으면 BLUE, RED 팀을 생성하고
     * 랜덤으로 한 팀을 선택해 선공 팀으로 설정한 뒤 반환한다.
     */
    @Override
    @Transactional
    public ResponseGameStartDto gameStart(RequestGameStartDto requestDto) {
        log.info("멀티 게임 시작 - 게임 시작 알림 받음: {}", requestDto);

        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("멀티 게임 시작 - 존재하지 않는 방입니다."));

        Team firstOffenseTeam = null;

        try {
            gameStartLock.lock(); // 락을 획득할 때까지 대기
            log.info("락 획득 - 게임 상태: {}", foundRoom.getGameStatus());

            if(foundRoom.getGameStatus() == GameStatus.WAITING) {
                foundRoom.changeGameStatus(requestDto.getGameStatus()); // 입력으로 들어온 대로 방 상태 변경
                log.info("123 - 방상태 변경: {}", foundRoom.getGameStatus());

                Team blueTeam = Team.createTeam(foundRoom, RoomUserTeam.BLUE);
                Team redTeam = Team.createTeam(foundRoom, RoomUserTeam.RED);
                log.info("123 - 팀생성: {}", blueTeam);
                log.info("123 - 팀생성: {}", redTeam);

                gameRoomRepository.save(foundRoom);
                teamRepository.save(blueTeam);
                teamRepository.save(redTeam);
                entityManager.flush();
                entityManager.clear();

                log.info("123 - 영속성 컨텍스트 비움");

                // firstOffenseTeam = foundRoom.assignRandomTeamStatus(); // 랜덤으로 선공팀 결정
                firstOffenseTeam = foundRoom.getTeams().get(0); //TODO: 프론트 요청으로 임시 수정, 되돌려 놔야 함
                foundRoom.getTeams().get(0).changeTeamStatus(TeamStatus.OFFENSE);
                foundRoom.getTeams().get(1).changeTeamStatus(TeamStatus.DEFENSE);

                log.info("123 - 공격 수비 설정");

                quizService.storeQuizzes(foundRoom); // 게임 시작 전 랜덤으로 100문제를 저장해 둠
                log.info("123 - 문제 추가");

                gameRoomRepository.save(foundRoom);


            } else {
                log.info("123 - 공격팀 조회");
                firstOffenseTeam = foundRoom.getTeams().get(0).getTeamStatus() == OFFENSE
                        ? foundRoom.getTeams().get(0) : foundRoom.getTeams().get(1);
            }
        } finally {
            log.info("123 - 락 반환");
            gameStartLock.unlock();
        }

        ResponseGameStartDto responseDto = ResponseGameStartDto.builder()
                .teamColor(firstOffenseTeam.getTeamColor())
                .build();
        log.info("멀티 게임 시작 - 게임 세팅 완료, 선공 팀: {}", responseDto);
        return responseDto;
    }

    // 게임 시작 직전 유저의 정보가 더 이상 변할 수 없을 때 유저의 정보 조회
    @Override
    public ResponseGetUserInfoDto getUserInfo(Long roomUserId) {
        log.info("게임 시작 전 유저 조회 요청 - roomUserId: {}", roomUserId);

        RoomUser foundRoomUser = roomUserRepository.findById(roomUserId).orElseThrow(
                () -> new NoSuchElementException("게임 시작 전 유저 조회 - 존재하지 않는 유저입니다."));

        ResponseGetUserInfoDto responseDto = ResponseGetUserInfoDto.builder()
                .username(foundRoomUser.getUser().getUsername())
                .role(foundRoomUser.getRole())
                .team(foundRoomUser.getTeam())
                .isLeader(foundRoomUser.getIsLeader())
                .build();

        log.info("게임 시작 전 유저 조회 결과: {}", responseDto);

        return responseDto;
    }

    // 답안 제출 제한 시간 종료 알림을 받으면 제출된 답안을 모아 리스트 형식으로 반환
    @Override
    public ResponseSubmitTimeoutDto submitTimeout(Long roomId) {
        log.info("답안 제출 제한 시간 종료 - roomId: {}", roomId);

        GameRoom foundRoom = gameRoomRepository.findById(roomId).orElseThrow(
                () -> new NoSuchElementException("답안 제출 제한 시간 종료 - 존재하지 않는 방입니다."));

        ResponseSubmitTimeoutDto responseDto = ResponseSubmitTimeoutDto.builder()
                .answerList(gameRoomWebSocketService.getRoomAnswers(foundRoom.getId()))
                .build();

        log.info("제출된 답안 리스트: {}", responseDto);
        return responseDto;
    }

    // 방 제목으로 방 검색하기
    @Override
    public ResponseSearchMultiRoomByRoomNameDto searchMultiRoomByRoomName(String roomName, int start, int limit) {
        log.info("방 검색 - 방 이름으로 방 검색 요청: {}", roomName);

        Pageable pageable = PageRequest.of(start, limit);
        Page<GameRoom> gameRooms = gameRoomRepository.findByNameContaining(roomName, pageable);

        List<MultiRoomDto> multiRooms = gameRooms.stream()
                .map(gameRoom -> MultiRoomDto.builder()
                        .gameRoomId(gameRoom.getId())
                        .name(gameRoom.getName())
                        .currentUsers(gameRoom.getCurrentUsers())
                        .maxUsers(gameRoom.getMaxUsers())
                        .isLocked(gameRoom.getPassword() != null)
                        .build())
                .toList();

        ResponseSearchMultiRoomByRoomNameDto responseDto = ResponseSearchMultiRoomByRoomNameDto.builder()
                .multiRooms(multiRooms)
                .nextPageNumber(start + gameRooms.getTotalPages()) // 마지막 페이지 번호(프론트 입장에서 다음 페이지 로드 시 해당 번호부터 limit 갯수만큼 불러오면 됨)
                .build();

        log.info("방 검색 - 방 이름으로 방 검색 결과: {}", responseDto);
        return responseDto;
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
}