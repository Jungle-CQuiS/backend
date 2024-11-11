package meowKai.CQuiS_backend.application;

import meowKai.CQuiS_backend.dto.request.*;
import meowKai.CQuiS_backend.dto.response.*;


public interface GameRoomService {
    ResponseGetMultiRoomListDto getMultiRoomList(int start, int limit); // 멀티 게임 방 리스트 조회
    ResponseCreateMultiRoomDto createMultiRoom(RequestCreateMultiRoomDto requestCreateMultiRoomDto); // 멀티 게임 방 생성
    ResponseSwitchTeamDto switchTeam(RequestSwitchTeamDto requestSwitchTeamDto); // 유저의 팀 바꾸기
    ResponseReadyDto ready(RequestReadyDto requestReadyDto); // 준비하기
    ResponseKickUserDto kickUser(RequestKickUserDto requestKickUserDto); // 유저 강퇴(방장 권한 필요)
    ResponseYieldDto changeHost(RequestYieldDto requestYieldDto); // 방장 권한 위임
    ResponseYieldDto changeLeader(RequestYieldDto requestYieldDto); // 리더 권한 위임
    ResponseExitDto exit(RequestExitDto requestExitDto); // 현재 들어와 있는 방에서 퇴장
    ResponseJoinRoomDto joinRoom(RequestJoinRoomDto requestJoinRoomDto); // 방 입장
    ResponseGetRoomInfoDto getRoomInfo(Long roomId); // 방 정보 조회
    ResposeCheckPasswordDto checkPassword(RequestCheckPasswordDto requestPasswordDto); // 방 비밀번호 체크
    ResponseGiveHonorDto giveHonor(RequestGiveHonorDto requestGiveHonorDto); // 특정 유저에게 명예 주기
    ResponseGameStartDto gameStart(RequestGameStartDto requestGameStartDto); // 게임 시작 선공팀 설정
    ResponseGetUserInfoDto getUserInfo(Long roomUserId); // 게임 시작 직전 유저의 정보 조회
    ResponseSubmitTimeoutDto<?> submitTimeout(Long roomId); // 답안 제출 제한 시간 종료 알림을 받으면 제출된 답안을 모아 리스트 형식으로 반환
    ResponseSearchMultiRoomByRoomNameDto searchMultiRoomByRoomName(String roomName, int start, int limit); // 방 이름으로 방 검색
}