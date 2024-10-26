package meowKai.CQuiS_backend.application;

import meowKai.CQuiS_backend.dto.request.*;
import meowKai.CQuiS_backend.dto.response.*;


public interface GameRoomService {
    ResponseGetMultiRoomListDto getMultiRoomList(); // 멀티 게임 방 리스트 조회
    ResponseCreateMultiRoomDto createMultiRoom(RequestCreateMultiRoomDto requestCreateMultiRoomDto); // 멀티 게임 방 생성
    ResponseGetRoomInfoDto switchTeam(RequestSwitchTeamDto requestSwitchTeamDto); // 유저의 팀 바꾸기
    ResponseGetRoomInfoDto ready(RequestReadyDto requestReadyDto); // 준비하기
    ResponseGetRoomInfoDto kickUser(RequestKickUserDto requestKickUserDto); // 유저 강퇴(방장 권한 필요)
    ResponseGetRoomInfoDto changeHost(RequestYieldDto requestYieldDto); // 방장 권한 위임
    ResponseGetRoomInfoDto changeLeader(RequestYieldDto requestYieldDto); // 리더 권한 위임
    ResponseGetRoomInfoDto exit(RequestExitDto requestExitDto); // 현재 들어와 있는 방에서 퇴장
    ResponseGetRoomInfoDto joinRoom(RequestJoinRoomDto requestJoinRoomDto); // 방 입장
    ResponseGetRoomInfoDto getRoomInfo(Long roomId); // 방 정보 조회
    ResposeCheckPasswordDto checkPassword(RequestCheckPasswordDto requestPasswordDto); // 방 비밀번호 체크
    ResponseGiveHonorDto giveHonor(RequestGiveHonorDto requestGiveHonorDto); // 특정 유저에게 명예 주기
}