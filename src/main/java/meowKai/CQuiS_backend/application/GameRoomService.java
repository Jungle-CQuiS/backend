package meowKai.CQuiS_backend.application;

import meowKai.CQuiS_backend.dto.MultiRoomListDto;
import meowKai.CQuiS_backend.dto.request.RequestCreateMultiRoomDto;

import java.util.List;

public interface GameRoomService {
    List<MultiRoomListDto> getMultiRoomList(); // 멀티 게임 방 리스트 조회
    void createMultiRoom(RequestCreateMultiRoomDto requestCreateMultiRoomDto); // 멀티 게임 방 생성
}
