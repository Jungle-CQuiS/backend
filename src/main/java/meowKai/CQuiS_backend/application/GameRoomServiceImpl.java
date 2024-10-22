package meowKai.CQuiS_backend.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.domain.GameRoom;
import meowKai.CQuiS_backend.domain.RoomUser;
import meowKai.CQuiS_backend.domain.RoomUserRole;
import meowKai.CQuiS_backend.domain.User;
import meowKai.CQuiS_backend.dto.MultiRoomListDto;
import meowKai.CQuiS_backend.dto.request.RequestCreateMultiRoomDto;
import meowKai.CQuiS_backend.dto.request.RequestKickUserDto;
import meowKai.CQuiS_backend.dto.request.RequestReadyDto;
import meowKai.CQuiS_backend.dto.request.RequestSwitchTeamDto;
import meowKai.CQuiS_backend.dto.response.*;
import meowKai.CQuiS_backend.infrastructure.GameRoomRepository;
import meowKai.CQuiS_backend.infrastructure.RoomUserRepository;
import meowKai.CQuiS_backend.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
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
                        () -> new NoSuchElementException("존재하지 않는 유저입니다.")));

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

        roomUserRepository.delete(roomUserToKick);
        foundRoom.removeUser();

        ResponseKickUserDto responseDto = ResponseKickUserDto
                .builder()
                .kickedRoomUserId(roomUserToKick.getId())
                .build();

        log.info("유저 강퇴 결과: {}", responseDto);

        return responseDto;
    }
}