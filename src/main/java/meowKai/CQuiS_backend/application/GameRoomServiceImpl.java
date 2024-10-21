package meowKai.CQuiS_backend.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.domain.GameRoom;
import meowKai.CQuiS_backend.domain.RoomUser;
import meowKai.CQuiS_backend.dto.MultiRoomListDto;
import meowKai.CQuiS_backend.dto.request.RequestCreateMultiRoomDto;
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
    public List<MultiRoomListDto> getMultiRoomList() {
        log.info("멀티 게임 방 리스트 조회 요청");
        List<GameRoom> gameRoomList = gameRoomRepository.findAll();
        return gameRoomList.stream()
                .map(gameRoom -> MultiRoomListDto.builder()
                        .gameRoomId(gameRoom.getId())
                        .name(gameRoom.getName())
                        .currentUsers(gameRoom.getCurrentUsers())
                        .maxUsers(gameRoom.getMaxUsers())
                        .isLocked(gameRoom.getPassword() != null)
                        .build())
                .collect(Collectors.toList());
    }

    // TODO: 중간 테이블 생성하고 방 만든 사람 방장으로 설정하기
    // 멀티 게임 방 생성하기
    @Override
    public void createMultiRoom(RequestCreateMultiRoomDto requestDto) {
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
    }
}
