package meowKai.CQuiS_backend.application;

import meowKai.CQuiS_backend.dto.request.*;
import meowKai.CQuiS_backend.dto.response.*;


public interface GameRoomService {
    ResponseGetMultiRoomListDto getMultiRoomList(); // 멀티 게임 방 리스트 조회
    ResponseCreateMultiRoomDto createMultiRoom(RequestCreateMultiRoomDto requestCreateMultiRoomDto); // 멀티 게임 방 생성
    ResponseSwitchTeamDto switchTeam(RequestSwitchTeamDto requestSwitchTeamDto); // 유저의 팀 바꾸기
    ResponseReadyDto ready(RequestReadyDto requestReadyDto); // 준비하기
    ResponseKickUserDto kickUser(RequestKickUserDto requestKickUserDto); // 유저 강퇴(방장 권한 필요)
    ResponseYieldDto changeHost(RequestYieldDto requestYieldDto); // 방장 권한 위임
    ResponseYieldDto changeLeader(RequestYieldDto requestYieldDto); // 리더 권한 위임
    void exit(RequestExitDto requestExitDto); // 현재 들어와 있는 방에서 퇴장
    ResponseJoinRoomDto joinRoom(RequestJoinRoomDto requestJoinRoomDto); // 방 입장
    ResponseGetRoomInfoDto getRoomInfo(Long roomId); // 방 정보 조회
}