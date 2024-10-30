package meowKai.CQuiS_backend.application;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.domain.*;
import meowKai.CQuiS_backend.dto.MultiRoomUserDto;
import meowKai.CQuiS_backend.dto.request.*;
import meowKai.CQuiS_backend.dto.response.*;
import meowKai.CQuiS_backend.infrastructure.GameRoomRepository;
import meowKai.CQuiS_backend.infrastructure.RoomUserRepository;
import meowKai.CQuiS_backend.infrastructure.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final Map<Long, ScheduledFuture<?>> countdownTasks = new ConcurrentHashMap<>(); // 카운트다운 관리
    private final SimpleAsyncTaskScheduler taskScheduler;
    private final SimpMessagingTemplate messagingTemplate; // 웹 소켓 통신으로 메시지 전달 시에 사용

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

        if(!foundRoomUser.getIsReady()) {
            if(foundRoom.getGameStatus() == GameStatus.ALL_READY) { // ALL_READY 상태에서 유저가 레디를 취소하면
                stopCountdown(foundRoom);
            }
        } else if(isAllReady(foundRoom)) { // 모든 유저가 레디했다면
            foundRoom.changeGameStatus(GameStatus.ALL_READY);
            gameRoomRepository.save(foundRoom);
            startCountdown(foundRoom);
        }

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
        } else {
            // 비어있는 팀이 있으면 joinedRoomUser를 해당 팀으로 보내고 리더로 설정
            Arrays.stream(RoomUserTeam.values())
                    .filter(team -> isTeamEmpty(foundRoom, team))
                    .findFirst()
                    .ifPresent(joinedRoomUser::assignTeamLeader);
        }

        roomUserRepository.save(joinedRoomUser);
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

    // 모든 유저가 레디했을 때 5초를 카운트하면서 지속적으로 남은 시간(초)을 전송
    private void startCountdown(GameRoom gameRoom) {
        if(countdownTasks.containsKey(gameRoom.getId())) {
            stopCountdown(gameRoom);
        }

        AtomicInteger count = new AtomicInteger(5);     // 원자성 보장을 위해 AtomicInteger 사용
        ScheduledFuture<?> task = taskScheduler.scheduleAtFixedRate(() -> {
            try {
                // ALL_READY 상태가 아니라면 카운트다운 중지
                if (gameRoom.getGameStatus() != GameStatus.ALL_READY) {
                    stopCountdown(gameRoom);
                    return;
                }

                int currentCount = count.getAndDecrement();
                if (currentCount > 0) {
                    // 남은 시간(초)을 전송
                    messagingTemplate.convertAndSend("/topic/rooms/" + gameRoom.getId() + "/status",
                            ResponseStartCountdownDto.builder()
                                    .gameStatus(gameRoom.getGameStatus())
                                    .count(currentCount)
                                    .build());
                } else {
                    gameRoom.changeGameStatus(GameStatus.GAME_START);   // 시간이 다 되어 GameRoom의 상태 전환
                    gameRoomRepository.save(gameRoom);
                    stopCountdown(gameRoom);

                    messagingTemplate.convertAndSend("/topic/rooms/" + gameRoom.getId() + "/status",
                            ResponseStartCountdownDto.builder()
                                    .gameStatus(gameRoom.getGameStatus())
                                    .count(currentCount)
                                    .build());
                }
            } catch (Exception e) {
                log.error("게임 시작 카운트다운 에러: {}", e.getMessage());
                stopCountdown(gameRoom);
            }
        }, Duration.ofSeconds(1));

        countdownTasks.put(gameRoom.getId(), task);
    }

    // 카운트다운 도중 레디 취소가 발생했을 때 처리
    private void stopCountdown(GameRoom gameRoom) {
        ScheduledFuture<?> task = countdownTasks.remove(gameRoom.getId());
        gameRoom.changeGameStatus(GameStatus.STOP_READY);
        if(task != null) {
            task.cancel(false); // 작업 중지
        }
        messagingTemplate.convertAndSend("/topic/rooms/" + gameRoom.getId() + "/status",
                ResponseStartCountdownDto.builder()
                        .gameStatus(gameRoom.getGameStatus())
                        .count(10)
                        .build());
        gameRoom.changeGameStatus(GameStatus.WAITING);
    }
}