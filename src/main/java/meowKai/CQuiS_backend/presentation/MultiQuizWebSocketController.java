package meowKai.CQuiS_backend.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.application.GameRoomService;
import meowKai.CQuiS_backend.application.GameRoomWebSocketService;
import meowKai.CQuiS_backend.dto.SelectQuizResult;
import meowKai.CQuiS_backend.dto.request.*;
import meowKai.CQuiS_backend.dto.response.*;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MultiQuizWebSocketController {

    private final GameRoomWebSocketService gameRoomWebSocketService;
    private final SimpMessagingTemplate messagingTemplate;

    // (PUB)방 입장 - (SUB)유저 변경 알림
    @MessageMapping("/rooms/join")
    public void joinRoom(@Payload RequestWebSocketJoinRoomDto requestDto, @Headers Map<String, Object> headers) {
        log.info("웹소켓 헤더: {}", headers);
        log.info("join 요청 받음: {}", requestDto);
        ResponseGetRoomInfoDto responseRoomInfoDto = gameRoomWebSocketService.joinRoom(requestDto);
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + requestDto.getRoomId() + "/info",
                responseRoomInfoDto
        );

        ResponseJoinRoomDto responseRoomUserDto = gameRoomWebSocketService
                .getRoomUserId(new RequestJoinRoomDto(requestDto.getUuid()));
        log.info("유저에게 개별 구독 메시지 전송: {}", requestDto.getUuid());
        messagingTemplate.convertAndSendToUser(
                requestDto.getUuid().toString(),
                "/queue/rooms/join",
                responseRoomUserDto
        );
        log.info("개별 구독 메시지 전송: {}", responseRoomUserDto);
    }

    // (PUB)팀 변경 - (SUB)유저 변경 알림
    @MessageMapping("/rooms/team-switch")
    public void switchTeam(RequestSwitchTeamDto requestDto) {
        ResponseGetRoomInfoDto responseDto = gameRoomWebSocketService.switchTeam(requestDto);
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + requestDto.getRoomId() + "/info",
                responseDto
        );
    }

    // (PUB)준비/준비 취소 - (SUB)유저 변경 알림
    @MessageMapping("/rooms/ready")
    public void ready(RequestReadyDto requestDto) {
        ResponseGetRoomInfoDto responseDto = gameRoomWebSocketService.ready(requestDto);
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + requestDto.getRoomId() + "/info",
                responseDto
        );
    }

    // (PUB)퇴장 - (SUB)유저 변경 알림
    @MessageMapping("/rooms/exit")
    public void exit(RequestExitDto requestDto) {
        ResponseGetRoomInfoDto responseDto = gameRoomWebSocketService.exit(requestDto);
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + requestDto.getRoomId() + "/info",
                responseDto
        );
    }

    // (PUB)강퇴 - (SUB)유저 변경 알림
    @MessageMapping("/rooms/kick")
    public void kickUser(RequestKickUserDto requestDto) {
        ResponseGetRoomInfoDto responseDto = gameRoomWebSocketService.kickUser(requestDto);
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + requestDto.getRoomId() + "/info",
                responseDto
        );
    }

    // (PUB)방장 위임 - (SUB)유저 변경 알림
    @MessageMapping("/rooms/yield-host")
    public void changeHost(RequestYieldDto requestDto) {
        ResponseGetRoomInfoDto responseDto = gameRoomWebSocketService.changeHost(requestDto);
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + requestDto.getRoomId() + "/info",
                responseDto
        );
    }

    // (PUB)리더 위임 - (SUB)유저 변경 알림
    @MessageMapping("/rooms/yield-leader")
    public void changeLeader(RequestYieldDto requestDto) {
        ResponseGetRoomInfoDto responseDto = gameRoomWebSocketService.changeLeader(requestDto);
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + requestDto.getRoomId() + "/info",
                responseDto
        );
    }

    // (PUB)수비팀 리더 문제 선택 - (SUB)수비팀 팀원 문제 전달
    @MessageMapping("/game/quiz-select")
    public void selectQuiz(RequestSelectQuizDto requestDto) {

        SelectQuizResult result = gameRoomWebSocketService.selectQuiz(requestDto);
        String destination = String.format(
                "/topic/game/%d/%s",
                requestDto.getRoomId(),
                result.defenseTeamColor()
        );

        messagingTemplate.convertAndSend(destination, result.responseDto());
    }
}
