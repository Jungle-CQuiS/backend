package meowKai.CQuiS_backend.application;

import meowKai.CQuiS_backend.dto.SelectAnswerResult;
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
    SelectQuizResult<ResponseSelectOptionDto> selectOption(RequestSelectQuizDto requestSelectQuizDto); // 수비 팀 리더가 선택을 바꿀 때마다 수비 팀 전원에게 전달
    SelectQuizResult<ResponseSelectQuizDto> selectQuiz(RequestSelectQuizDto requestSelectQuizDto); // 수비 팀 리더가 최종적으로 선택한 퀴즈를 수비 팀 전원에게 전달
    void submitPersonal(RequestSubmitPersonalDto requestSubmitPersonalDto); // 수비 팀 팀원들이 답안을 제출
    SelectAnswerResult<ResponseSelectAnswerDto> selectAnswer(RequestSelectAnswerDto requestDto); // 수비 팀 리더가 답안 선택을 바꿀 때 마다 알림을 전달
    SelectAnswerResult<ResponseSubmitTeamDto> submitTeam(RequestSelectAnswerDto requestDto); // 수비 팀 리더가 최종 답안을 제출, 채점 및 다음 문제를 위한 세팅, hp 변경 알림, 게임 종료 알림 수행
    Boolean isGameover(Long roomId); // 게임 종료 조건을 체크
    ResponseTransferEmojiDto transferEmoji(RequestTransferEmojiDto requestTransferEmojiDto); // 클릭한 이모티콘을 팀원들에게 전달
}
