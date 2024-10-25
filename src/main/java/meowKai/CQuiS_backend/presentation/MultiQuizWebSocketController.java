package meowKai.CQuiS_backend.presentation;

import io.swagger.v3.oas.annotations.headers.Header;
import lombok.RequiredArgsConstructor;
import meowKai.CQuiS_backend.application.GameRoomService;
import meowKai.CQuiS_backend.application.GameRoomWebSocketService;
import meowKai.CQuiS_backend.application.GameRoomWebSocketServiceImpl;
import meowKai.CQuiS_backend.dto.request.RequestExitDto;
import meowKai.CQuiS_backend.dto.request.RequestJoinRoomDto;
import meowKai.CQuiS_backend.dto.request.RequestReadyDto;
import meowKai.CQuiS_backend.dto.request.RequestSwitchTeamDto;
import meowKai.CQuiS_backend.dto.response.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MultiQuizWebSocketController {

    private final GameRoomService gameRoomService;
    private final GameRoomWebSocketService gameRoomWebSocketService;
    private final SimpMessagingTemplate messagingTemplate;

    // (PUB)방 입장 - (SUB)유저 입장 알림
    @MessageMapping("/rooms/join")
    public void joinRoom(RequestJoinRoomDto requestDto) {
        ResponseGetRoomInfoDto responseDto = gameRoomWebSocketService.joinRoom(requestDto);
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + requestDto.getRoomId() + "/info",
                responseDto
        );
    }

    // (PUB)팀 변경 - (SUB)팀 변경 내역 알림
    @MessageMapping("/rooms/team-switch")
    public void switchTeam(RequestSwitchTeamDto requestDto) {
        ResponseGetRoomInfoDto responseDto = gameRoomWebSocketService.switchTeam(requestDto);
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + requestDto.getRoomId() + "/info",
                responseDto
        );
    }

    // (PUB)준비/준비 취소 - (SUB)준비 상태 변경 알림
    @MessageMapping("/rooms/ready")
    public void ready(RequestReadyDto requestDto) {
        ResponseGetRoomInfoDto responseDto = gameRoomWebSocketService.ready(requestDto);
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + requestDto.getRoomId() + "/info",
                responseDto
        );
    }

    // (PUB)퇴장 - (SUB)유저 퇴장 알림
    @MessageMapping("/rooms/exit")
    public void exit(RequestExitDto requestDto) {
        ResponseGetRoomInfoDto responseDto = gameRoomWebSocketService.exit(requestDto);
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + requestDto.getRoomId() + "/info",
                responseDto
        );
    }
}
