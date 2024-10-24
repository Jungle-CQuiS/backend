package meowKai.CQuiS_backend.application;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GameRoomServiceImpl implements GameRoomService {

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
                , RoomUserRole.HOST, RoomUserTeam.RED);

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
    public ResponseSwitchTeamDto switchTeam(RequestSwitchTeamDto requestDto) {
        log.info("유저의 팀 바꾸기 요청: {}", requestDto);

        // TODO: 메서드화 해보기 나중에...
        User foundUser = userRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));
        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        RoomUser foundRoomUser = roomUserRepository.findByGameRoomAndUser(foundRoom, foundUser).orElseThrow(
                () -> new NoSuchElementException("해당 방에 유저가 존재하지 않습니다."));

        foundRoomUser.changeTeam();
        ResponseSwitchTeamDto responseDto = ResponseSwitchTeamDto
                .builder()
                .team(foundRoomUser.getTeam())
                .build();
        log.info("유저의 팀 바꾸기 결과: {}", responseDto);
        return responseDto;
    }

    // 준비하기
    @Override
    @Transactional
    public ResponseReadyDto ready(RequestReadyDto requestDto) {
        log.info("준비하기 요청: {}", requestDto);

        User foundUser = userRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));
        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        RoomUser foundRoomUser = roomUserRepository.findByGameRoomAndUser(foundRoom, foundUser).orElseThrow(
                () -> new NoSuchElementException("해당 방에 유저가 존재하지 않습니다."));

        foundRoomUser.changeReady();
        ResponseReadyDto responseDto = ResponseReadyDto
                .builder()
                .isReady(foundRoomUser.getIsReady())
                .build();
        log.info("준비하기 결과: {}", responseDto);
        return responseDto;
    }

    // 유저 강퇴(방장 권한 필요)
    @Override
    @Transactional
    public ResponseKickUserDto kickUser(RequestKickUserDto requestDto) {

        log.info("유저 강퇴 요청: {}", requestDto);

        // 방장 권한을 가진 유저가 방에 존재하는지 검토
        User foundUser = userRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));
        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        RoomUser foundRoomUser = roomUserRepository.findByGameRoomAndUser(foundRoom, foundUser).orElseThrow(
                () -> new NoSuchElementException("해당 방에 유저가 존재하지 않습니다."));

        // 해당 유저에게 방장 권한이 있는지 체크
        boolean isHost = foundRoomUser.getRole() == RoomUserRole.HOST;
        if (!isHost) {
            throw new IllegalArgumentException("방장만 유저를 강퇴할 수 있습니다.");
        }

        // 강퇴하려는 유저가 방에 존재하는지 검토
        User userToKick = userRepository.findById(requestDto.getKickRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("강퇴하려는 유저가 존재하지 않습니다."));
        RoomUser roomUserToKick = roomUserRepository.findByGameRoomAndUser(foundRoom, userToKick).orElseThrow(
                () -> new NoSuchElementException("강퇴하려는 유저가 해당 방에 존재하지 않습니다."));

        // 강퇴하려는 유저가 리더라면 리더를 양도
        if(roomUserToKick.getIsLeader()) {
            leaderTransfer(foundRoom, roomUserToKick);
        }

        roomUserRepository.delete(roomUserToKick);
        foundRoom.removeUser();

        ResponseKickUserDto responseDto = ResponseKickUserDto
                .builder()
                .kickedRoomUserId(roomUserToKick.getId())
                .build();

        log.info("유저 강퇴 결과: {}", responseDto);

        return responseDto;
    }

    // 방장을 변경
    @Override
    public ResponseYieldDto changeHost(RequestYieldDto requestDto) {
        log.info("방장 위임 요청: {}", requestDto);

        GameRoom gameRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        RoomUser hostUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));
        RoomUser nextHostUser = roomUserRepository.findById(requestDto.getYieldUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));

        if(hostUser.getRole() != RoomUserRole.HOST) {
            throw new IllegalStateException("방장 권한이 없는 유저입니다.");
        }

        hostUser.changeRole();
        nextHostUser.changeRole();

        ResponseYieldDto responseDto = ResponseYieldDto.builder()
                .yieldedUserId(nextHostUser.getId())
                .build();
        log.info("방장 위임 결과: {}", responseDto);
        return responseDto;
    }

    // 리더를 변경
    @Override
    public ResponseYieldDto changeLeader(RequestYieldDto requestDto) {
        log.info("리더 위임 요청: {}", requestDto);

        GameRoom gameRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        RoomUser leaderUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));
        RoomUser nextLeaderUser = roomUserRepository.findById(requestDto.getYieldUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));

        if(!leaderUser.getIsLeader()) {
            throw new IllegalStateException("리더 권한이 없는 유저입니다.");
        }

        if(leaderUser.getTeam() != nextLeaderUser.getTeam()) {
            throw new IllegalStateException("리더를 양도하려는 유저와 같은 팀이 아닙니다.");
        }

        leaderUser.changeLeader();
        nextLeaderUser.changeLeader();

        ResponseYieldDto responseDto = ResponseYieldDto.builder()
                .yieldedUserId(nextLeaderUser.getId())
                .build();
        log.info("리더 위임 결과: {}", responseDto);
        return responseDto;
    }

    // 방 퇴장
    @Override
    @Transactional
    public ResponseExitDto exit(RequestExitDto requestDto) {
        log.info("방 나가기 요청: {}", requestDto);

        GameRoom gameRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        RoomUser roomUser = roomUserRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));

        // 방이 비게되면 방을 삭제, 나가는 유저가 권한이 있다면 권한을 양도
        if(isRoomEmpty(gameRoom, roomUser)) {
            gameRoomRepository.delete(gameRoom);
            log.info("방 삭제: {}", gameRoom.getId());
        } else {
            if (roomUser.getRole() == RoomUserRole.HOST) {
                hostTransfer(gameRoom, roomUser);
            }
            if (roomUser.getIsLeader()) {
                leaderTransfer(gameRoom, roomUser);
            }
        }

        gameRoom.removeUser();

        ResponseExitDto responseDto = ResponseExitDto.builder()
                .roomUserId(roomUser.getId())
                .build();
        log.info("방 나가기 완료: {}", responseDto);

        roomUserRepository.delete(roomUser);

        return responseDto;
    }

    // 방 입장
    @Override
    public ResponseJoinRoomDto joinRoom(RequestJoinRoomDto requestJoinRoomDto) {
        log.info("방 입장 요청: {}", requestJoinRoomDto);

        GameRoom gameRoom = gameRoomRepository.findById(requestJoinRoomDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        User joinUser = userRepository.findByUuid(requestJoinRoomDto.getUuid()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));

        if (gameRoom.getCurrentUsers().equals(gameRoom.getMaxUsers())) {
            throw new IllegalStateException("방이 꽉 찼습니다.");
        }

        // TODO: 추후에 팀 랜덤 배정 구현
        RoomUser joinedRoomUser = RoomUser.createRoomUser(gameRoom, joinUser, RoomUserRole.GUEST, RoomUserTeam.RED);

        // 방이 비어있으면 joinedRoomUser를 host, leader로
        if(countRoomUser(gameRoom) == 0) {
            joinedRoomUser.changeRole();
            joinedRoomUser.changeLeader();
        } else {
            // 비어있는 팀이 있으면 joinedRoomUser를 해당 팀으로 보내고 리더로 설정
            Arrays.stream(RoomUserTeam.values())
                    .filter(team -> isTeamEmpty(gameRoom, team))
                    .findFirst()
                    .ifPresent(joinedRoomUser::assignTeamLeader);
        }

        roomUserRepository.save(joinedRoomUser);

        // TODO: ResponseJoinRoomDto 수정하기(지금 텅 비었음)
        ResponseJoinRoomDto responseJoinRoomDto = ResponseJoinRoomDto.builder().build();
        log.info("방 입장 결과: {}", responseJoinRoomDto);
        return responseJoinRoomDto;
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
                        // TODO: 유저 데이터 통계 테이블 나오면 추가하기 이건. 일단은 임시값 100 넘겨줌
                        .honorCount(100)
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


    private void hostTransfer(GameRoom gameRoom, RoomUser hostUser) {
        if(!isRoomEmpty(gameRoom, hostUser)) {
            RoomUser nextHostUser = gameRoom.getRoomUsers().stream()
                    .filter(user -> !Objects.equals(user.getId(), hostUser.getId()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("방에 다른 유저가 없습니다."));

            hostUser.changeRole();
            nextHostUser.changeRole();
        }
    }

    private void leaderTransfer(GameRoom gameRoom, RoomUser leaderUser) {
        if(!isTeamEmpty(gameRoom, leaderUser.getTeam(), leaderUser)) {
            RoomUser nextLeaderUser = gameRoom.getRoomUsers().stream()
                    .filter(user -> user.getTeam() == leaderUser.getTeam() &&
                            !Objects.equals(user.getId(), leaderUser.getId()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("팀에 다른 팀원이 없습니다."));

            leaderUser.changeLeader();
            nextLeaderUser.changeLeader();

        }
    }

    // 현재 방에 몇 명이 있는지 확인
    private int countRoomUser(GameRoom gameRoom) {
        return (int) gameRoom.getRoomUsers().size();
    }

    // 유저가 나가는 상황에서 방이 비게 되는지 확인
    private boolean isRoomEmpty(GameRoom gameRoom, RoomUser leavingUser) {
        return gameRoom.getRoomUsers().stream()
                .noneMatch(user -> user != leavingUser);
    }

    // 유저가 나가는 상황에서 팀이 비게 되는지 확인
    private boolean isTeamEmpty(GameRoom gameRoom, RoomUserTeam teamColor, RoomUser leavingUser) {
        return gameRoom.getRoomUsers().stream()
                .filter(user -> user != leavingUser)
                .noneMatch(user -> user.getTeam() == teamColor);
    }

    // 유저가 들어오는 상황에서 팀이 비어있는지 확인
    private boolean isTeamEmpty(GameRoom gameRoom, RoomUserTeam teamColor) {
        return gameRoom.getRoomUsers().stream()
                .noneMatch(user -> user.getTeam() == teamColor);
    }
}