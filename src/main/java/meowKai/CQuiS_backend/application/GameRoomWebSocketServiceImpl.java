package meowKai.CQuiS_backend.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.domain.*;
import meowKai.CQuiS_backend.dto.MultiRoomUserDto;
import meowKai.CQuiS_backend.dto.request.*;
import meowKai.CQuiS_backend.dto.response.*;
import meowKai.CQuiS_backend.infrastructure.GameRoomRepository;
import meowKai.CQuiS_backend.infrastructure.RoomUserRepository;
import meowKai.CQuiS_backend.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GameRoomWebSocketServiceImpl implements GameRoomWebSocketService{

    private final GameRoomRepository gameRoomRepository;
    private final RoomUserRepository roomUserRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ResponseGetRoomInfoDto switchTeam(RequestSwitchTeamDto requestDto) {
        log.info("ws - 유저의 팀 바꾸기 요청: {}", requestDto);

        User foundUser = userRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));
        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        RoomUser foundRoomUser = roomUserRepository.findByGameRoomAndUser(foundRoom, foundUser).orElseThrow(
                () -> new NoSuchElementException("해당 방에 유저가 존재하지 않습니다."));

        foundRoomUser.changeTeam();
        roomUserRepository.save(foundRoomUser); // 변경사항 DB에 반영

        ResponseGetRoomInfoDto responseDto = getResponseGetRoomInfoDto(foundRoom);
        log.info("ws - 유저의 팀 바꾸기 결과: {}", responseDto);
        return responseDto;
    }

    @Override
    @Transactional
    public ResponseGetRoomInfoDto ready(RequestReadyDto requestDto) {
        log.info("ws - 레디/레디 취소 요청: {}", requestDto);

        User foundUser = userRepository.findById(requestDto.getRoomUserId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 유저입니다."));
        GameRoom foundRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        RoomUser foundRoomUser = roomUserRepository.findByGameRoomAndUser(foundRoom, foundUser).orElseThrow(
                () -> new NoSuchElementException("해당 방에 유저가 존재하지 않습니다."));

        foundRoomUser.changeReady();
        roomUserRepository.save(foundRoomUser); // 변경사항 DB에 반영

        ResponseGetRoomInfoDto responseDto = getResponseGetRoomInfoDto(foundRoom);
        log.info("ws - 레디/레디 취소 결과: {}", responseDto);
        return responseDto;
    }

    @Override
    public ResponseGetRoomInfoDto kickUser(RequestKickUserDto requestDto) {
        return null;
    }

    @Override
    public ResponseGetRoomInfoDto changeHost(RequestYieldDto requestDto) {
        return null;
    }

    @Override
    public ResponseGetRoomInfoDto changeLeader(RequestYieldDto requestDto) {
        return null;
    }

    @Override
    @Transactional
    public ResponseGetRoomInfoDto exit(RequestExitDto requestDto) {
        log.info("ws - 방 나가기 요청: {}", requestDto);

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
        roomUserRepository.delete(roomUser);

        ResponseGetRoomInfoDto responseDto = getResponseGetRoomInfoDto(gameRoom);
        log.info("ws - 방 나가기 완료: {}", responseDto);
        return responseDto;
    }

    @Override
    @Transactional
    public ResponseGetRoomInfoDto joinRoom(RequestJoinRoomDto requestDto) {
        log.info("ws - 방 입장 요청: {}", requestDto);

        GameRoom gameRoom = gameRoomRepository.findById(requestDto.getRoomId()).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 방입니다."));
        User joinUser = userRepository.findByUuid(requestDto.getUuid()).orElseThrow(
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
        gameRoom.addUser();

        roomUserRepository.save(joinedRoomUser);
        gameRoomRepository.save(gameRoom);

        ResponseGetRoomInfoDto responseDto = getResponseGetRoomInfoDto(gameRoom);
        log.info("ws - 방 입장 결과: {}", responseDto);
        return responseDto;
    }

    /**
     * GameRoom을 인자로 넘겨주면 해당 방에 있는
     * 각 유저의 정보를 바탕으로 MultiRoomUserDto를 생성한 뒤
     * 그것을 리스트로 만들고
     * ResponseGetRoomInfoDto에 넣어서 반환
     *
     * @param foundRoom
     * @return responseDto
     */
    private static ResponseGetRoomInfoDto getResponseGetRoomInfoDto(GameRoom foundRoom) {
        ResponseGetRoomInfoDto responseDto = ResponseGetRoomInfoDto
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
