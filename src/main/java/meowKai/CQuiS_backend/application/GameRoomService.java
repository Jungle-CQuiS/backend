package meowKai.CQuiS_backend.application;

import meowKai.CQuiS_backend.dto.request.RequestCreateMultiRoomDto;
import meowKai.CQuiS_backend.dto.request.RequestKickUserDto;
import meowKai.CQuiS_backend.dto.request.RequestReadyDto;
import meowKai.CQuiS_backend.dto.request.RequestSwitchTeamDto;
import meowKai.CQuiS_backend.dto.response.*;


public interface GameRoomService {
    ResponseGetMultiRoomListDto getMultiRoomList(); // 멀티 게임 방 리스트 조회
    ResponseCreateMultiRoomDto createMultiRoom(RequestCreateMultiRoomDto requestCreateMultiRoomDto); // 멀티 게임 방 생성
    ResponseSwitchTeamDto switchTeam(RequestSwitchTeamDto requestSwitchTeamDto); // 유저의 팀 바꾸기
    ResponseReadyDto ready(RequestReadyDto requestReadyDto); // 준비하기
    ResponseKickUserDto kickUser(RequestKickUserDto requestKickUserDto); // 유저 강퇴(방장 권한 필요)
}