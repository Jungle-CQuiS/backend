package meowKai.CQuiS_backend.application;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.domain.*;
import meowKai.CQuiS_backend.dto.MultiRoomListDto;
import meowKai.CQuiS_backend.dto.MultiRoomUserDto;
import meowKai.CQuiS_backend.dto.request.*;
import meowKai.CQuiS_backend.dto.response.*;
import meowKai.CQuiS_backend.infrastructure.GameRoomRepository;
import meowKai.CQuiS_backend.infrastructure.RoomUserRepository;
import meowKai.CQuiS_backend.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GameRoomServiceImpl implements GameRoomService {

    @Autowired
    private EntityManager entityManager;

    private final GameRoomRepository gameRoomRepository;
    private final RoomUserRepository roomUserRepository;
    private final UserRepository userRepository;

    // TODO: 페이지네이션 | 무한스크롤로 구현하기
    // 존재하는 모든 멀티 게임 방 조회하기
    @Override
    public ResponseGetMultiRoomListDto getMultiRoomList() {
        log.info("멀티 게임 방 리스트 조회 요청");
        List<GameRoom> gameRoomList = gameRoomRepository.findAll();
        List<MultiRoomListDto> multiRoomList = gameRoomList.stream()
                .map(gameRoom -> MultiRoomListDto.builder()
                        .gameRoomId(gameRoom.getId())
                        .name(gameRoom.getName())
                        .currentUsers(gameRoom.getCurrentUsers())
                        .maxUsers(gameRoom.getMaxUsers())
                        .isLocked(gameRoom.getPassword() != null)
                        .build())
                .collect(Collectors.toList());

        ResponseGetMultiRoomListDto responseDto = ResponseGetMultiRoomListDto.builder()
                .rooms(multiRoomList)
                .build();

        log.info("멀티 게임 방 리스트 조회 결과: {}", responseDto);
        return responseDto;
    }

    // TODO: 중간 테이블 생성하고 방 만든 사람 방장으로 설정하기
    // 멀티 게임 방 생성하기
    @Override
    @Transactional
    public ResponseCreateMultiRoomDto createMultiRoom(RequestCreateMultiRoomDto requestDto) {
        log.info("멀티 게임 방 생성 요청: {}", requestDto);
        GameRoom createdRoom = GameRoom.createGameRoom(
                requestDto.getName(),
                requestDto.getMaxUser(),
                requestDto.getPassword()
        );

        RoomUser roomUser = RoomUser.createRoomUser(createdRoom,
                userRepository.findByUuid(requestDto.getUuid()).orElseThrow(
                        () -> new NoSuchElementException("존재하지 않는 유저입니다."))
                , RoomUserRole.HOST, RoomUserTeam.BLUE);
        roomUser.assignTeamLeader(RoomUserTeam.BLUE);

        roomUserRepository.save(roomUser);
        gameRoomRepository.save(createdRoom);

        ResponseCreateMultiRoomDto responseDto = ResponseCreateMultiRoomDto.builder()
                .roomId(createdRoom.getId())
                .role(roomUser.getRole())
                .isLeader(roomUser.getIsLeader())
                .team(roomUser.getTeam())
                .build();
        log.info("멀티 게임 방 생성 한 유저의 정보: {}", responseDto);
        return responseDto;
    }

    // 유저의 팀 바꾸기
    @Override
    @Transactional
    public ResponseGetRoomInfoDto switchTeam(RequestSwitchTeamDto requestDto) {
        log.info("유저의 팀 바꾸기 요청: {}", requestDto);

        // TODO: 메서드화 해보기 나중에...
        RoomUser foundRoomUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));
        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        if(!foundRoomUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("방에 해당 유저가 존재하지 않습니다.");
        }

        foundRoomUser.changeTeam();
        roomUserRepository.save(foundRoomUser);

        // 영속성 컨텍스트를 비워서 변경사항 DB에 반영
        entityManager.flush();
        entityManager.clear();

        // 변경사항을 DB에 반영하고 새로 데이터를 받아 옴
        GameRoom updatedGameRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        ResponseGetRoomInfoDto responseDto = getResponseGetRoomInfoDto(updatedGameRoom);
        log.info("유저의 팀 바꾸기 결과: {}", responseDto);
        return responseDto;
    }

    // 준비하기
    @Override
    @Transactional
    public ResponseGetRoomInfoDto ready(RequestReadyDto requestDto) {
        log.info("준비하기 요청: {}", requestDto);

        RoomUser foundRoomUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));
        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        if(!foundRoomUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("방에 해당 유저가 존재하지 않습니다.");
        }

        foundRoomUser.changeReady();
        roomUserRepository.save(foundRoomUser);

        // 영속성 컨텍스트를 비워서 변경사항 DB에 반영
        entityManager.flush();
        entityManager.clear();

        // 변경사항을 DB에 반영하고 새로 데이터를 받아 옴
        GameRoom updatedGameRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        ResponseGetRoomInfoDto responseDto = getResponseGetRoomInfoDto(updatedGameRoom);
        log.info("레디/레디 취소 결과: {}", responseDto);
        return responseDto;
    }

    // 유저 강퇴(방장 권한 필요)
    @Override
    @Transactional
    public ResponseGetRoomInfoDto kickUser(RequestKickUserDto requestDto) {

        log.info("유저 강퇴 요청: {}", requestDto);

        // 방장 권한을 가진 유저가 방에 존재하는지 검토
        RoomUser foundRoomUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));
        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        if(!foundRoomUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("방에 해당 유저가 존재하지 않습니다.");
        }

        // 해당 유저에게 방장 권한이 있는지 체크
        boolean isHost = foundRoomUser.getRole() == RoomUserRole.HOST;
        if (!isHost) {
            throw new IllegalArgumentException("방장만 유저를 강퇴할 수 있습니다.");
        }

        // 강퇴하려는 유저가 방에 존재하는지 검토
        RoomUser roomUserToKick = roomUserRepository.findById(requestDto.getKickRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("강퇴하려는 유저가 존재하지 않습니다."));
        if(!roomUserToKick.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("강퇴하려는 유저가 해당 방에 존재하지 않습니다.");
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
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        ResponseGetRoomInfoDto responseDto = getResponseGetRoomInfoDto(updatedGameRoom);
        log.info("ws - 유저 강퇴 결과: {}", responseDto);
        return responseDto;
    }


    // 방장을 변경
    @Override
    @Transactional
    public ResponseGetRoomInfoDto changeHost(RequestYieldDto requestDto) {
        log.info("방장 위임 요청: {}", requestDto);

        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        RoomUser hostUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));
        RoomUser nextHostUser = roomUserRepository.findById(requestDto.getYieldUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));

        if(!hostUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("방에 해당 유저가 존재하지 않습니다.");
        }
        if(!nextHostUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("방에 해당 유저가 존재하지 않습니다.");
        }

        if(hostUser.getRole() != RoomUserRole.HOST) {
            throw new IllegalStateException("방장 권한이 없는 유저입니다.");
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
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        ResponseGetRoomInfoDto responseDto = getResponseGetRoomInfoDto(updatedGameRoom);
        log.info("ws - 방장 위임 결과: {}", responseDto);
        return responseDto;
    }

    // 리더를 변경
    @Override
    @Transactional
    public ResponseGetRoomInfoDto changeLeader(RequestYieldDto requestDto) {
        log.info("리더 위임 요청: {}", requestDto);

        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        RoomUser leaderUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));
        RoomUser nextLeaderUser = roomUserRepository.findById(requestDto.getYieldUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));

        if(!leaderUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("방에 해당 유저가 존재하지 않습니다.");
        }
        if(!nextLeaderUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("방에 해당 유저가 존재하지 않습니다.");
        }

        if(!leaderUser.getIsLeader()) {
            throw new IllegalStateException("리더 권한이 없는 유저입니다.");
        }

        if(leaderUser.getTeam() != nextLeaderUser.getTeam()) {
            throw new IllegalStateException("리더를 양도하려는 유저와 같은 팀이 아닙니다.");
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
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        ResponseGetRoomInfoDto responseDto = getResponseGetRoomInfoDto(updatedGameRoom);
        log.info("ws - 리더 위임 결과: {}", responseDto);
        return responseDto;
    }

    // 방 퇴장
    @Override
    @Transactional
    public ResponseGetRoomInfoDto exit(RequestExitDto requestDto) {
        log.info("방 나가기 요청: {}", requestDto);

        RoomUser foundRoomUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));
        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        if(!foundRoomUser.getGameRoom().getId().equals(foundRoom.getId())) {
            throw new IllegalStateException("방에 해당 유저가 존재하지 않습니다.");
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
            log.info("방 삭제: {}", foundRoom.getId());
        }

        // 영속성 컨텍스트를 비워서 변경사항 DB에 반영
        entityManager.flush();
        entityManager.clear();

        // 변경사항을 DB에 반영하고 새로 데이터를 받아 옴
        return gameRoomRepository.findById(requestDto.getRoomId())
                .map(room -> {
                    ResponseGetRoomInfoDto responseDto = getResponseGetRoomInfoDto(room);
                    log.info("ws - 방 나가기 완료: {}", responseDto);
                    return responseDto;
                })
                .orElse(null);
    }

    // 방 입장
    @Override
    @Transactional
    public ResponseGetRoomInfoDto joinRoom(RequestJoinRoomDto requestDto) {
        log.info("방 입장 요청: {}", requestDto);

        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        User joinUser = userRepository.findByUuid(requestDto.getUuid()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));

        if (countRoomUser(foundRoom).equals(foundRoom.getMaxUsers())) {
            throw new IllegalStateException("방이 꽉 찼습니다.");
        }

        // TODO: 추후에 팀 랜덤 배정 구현
        RoomUser joinedRoomUser = RoomUser.createRoomUser(foundRoom, joinUser, RoomUserRole.GUEST, RoomUserTeam.BLUE);

        // 방이 비어있으면 joinedRoomUser를 host, leader로 <- 이런 일이 있을 수 있나?
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
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        ResponseGetRoomInfoDto responseDto = getResponseGetRoomInfoDto(updatedGameRoom);
        log.info("ws - 방 입장 결과: {}", responseDto);
        return responseDto;
    }

    // 방 입장하고 방에 대한 정보 가져오기
    @Override
    public ResponseGetRoomInfoDto getRoomInfo(Long roomId) {
        log.info("방 정보 조회 요청: {}", roomId);

        GameRoom gameRoom = gameRoomRepository.findById(roomId).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));

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

        log.info("방 정보 조회 결과: {}", responseDto);
        return responseDto;
    }

    @Override
    public ResposeCheckPasswordDto checkPassword(RequestCheckPasswordDto requestDto) {
        log.info("비밀 방 비밀번호 입력: {}", requestDto);
        GameRoom gameRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));

        ResposeCheckPasswordDto responseDto = ResposeCheckPasswordDto.builder()
                .isCorrect(Objects.equals(gameRoom.getPassword(), requestDto.getPassword()))
                .build();

        log.info("비밀 방 비밀번호 입력 결과: {}", responseDto);
        return responseDto;
    }

    @Override
    @Transactional
    public ResponseGiveHonorDto giveHonor(RequestGiveHonorDto requestDto) {
        log.info("명예 주기: {}", requestDto);

        RoomUser roomUser = roomUserRepository.findById(requestDto.getHonorRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));

        roomUser.getUser().getUserStatistics().addHonorCount();

        ResponseGiveHonorDto responseDto = ResponseGiveHonorDto.builder()
                .roomUserId(roomUser.getId())
                .build();

        log.info("명예 주기 결과: {}", responseDto);
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


    // 유저가 들어오는 상황에서 팀이 비어있는지 확인
    private boolean isTeamEmpty(GameRoom gameRoom, RoomUserTeam teamColor) {
        return gameRoom.getRoomUsers().stream()
                .noneMatch(user -> user.getTeam() == teamColor);
    }
}