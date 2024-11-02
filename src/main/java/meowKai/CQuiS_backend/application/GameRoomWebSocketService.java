package meowKai.CQuiS_backend.application;

import meowKai.CQuiS_backend.dto.SelectQuizResult;
import meowKai.CQuiS_backend.dto.request.*;
import meowKai.CQuiS_backend.dto.response.*;

public interface GameRoomWebSocketService {
    ResponseGetRoomInfoDto switchTeam(RequestSwitchTeamDto requestSwitchTeamDto); // 유저의 팀 바꾸기
    ResponseGetRoomInfoDto ready(RequestReadyDto requestReadyDto); // 준비하기
    ResponseGetRoomInfoDto kickUser(RequestKickUserDto requestKickUserDto); // 유저 강퇴(방장 권한 필요)
    ResponseGetRoomInfoDto changeHost(RequestYieldDto requestYieldDto); // 방장 권한 위임
    ResponseGetRoomInfoDto changeLeader(RequestYieldDto requestYieldDto); // 리더 권한 위임
    ResponseGetRoomInfoDto exit(RequestExitDto requestExitDto); // 현재 들어와 있는 방에서 퇴장
    ResponseGetRoomInfoDto joinRoom(RequestWebSocketJoinRoomDto requestJoinRoomDto); // 방 입장
    ResponseJoinRoomDto getRoomUserId(RequestJoinRoomDto requestJoinRoomDto); // 방 입장 후 생성된 RoomUser의 id 반환
    SelectQuizResult selectQuiz(RequestSelectQuizDto requestSelectQuizDto); // 수비 팀 리더가 선택한 퀴즈를 수비 팀 전원에게 전달
}
