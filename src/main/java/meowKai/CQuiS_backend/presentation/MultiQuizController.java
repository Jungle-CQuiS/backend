package meowKai.CQuiS_backend.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import meowKai.CQuiS_backend.application.GameRoomService;
import meowKai.CQuiS_backend.dto.MultiRoomListDto;
import meowKai.CQuiS_backend.dto.request.RequestCreateMultiRoomDto;
import meowKai.CQuiS_backend.dto.request.RequestKickUserDto;
import meowKai.CQuiS_backend.dto.request.RequestReadyDto;
import meowKai.CQuiS_backend.dto.request.RequestSwitchTeamDto;
import meowKai.CQuiS_backend.dto.response.*;
import meowKai.CQuiS_backend.global.base.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/quiz/multi")
public class MultiQuizController {

    private final GameRoomService gameRoomService;

    @Tag(name = "멀티모드 퀴즈")
    @Operation(summary = "퀴즈방 목록 조회")
    @GetMapping("/rooms")
    public ApiResponse<Object> getMultiRoomList() {
        ResponseGetMultiRoomListDto responseDto = gameRoomService.getMultiRoomList();
        return ApiResponse.ofSuccess(responseDto);
    }

    @Tag(name = "멀티모드 퀴즈")
    @Operation(summary = "퀴즈방 생성")
    @PostMapping("/rooms")
    public ApiResponse<Object> createMultiRoom(@Valid @RequestBody RequestCreateMultiRoomDto requestDto) {
        ResponseCreateMultiRoomDto responseDto = gameRoomService.createMultiRoom(requestDto);
        return ApiResponse.ofSuccess(responseDto);
    }

    @Tag(name = "멀티모드 퀴즈")
    @Operation(summary = "유저의 팀 바꾸기")
    @PostMapping("/team-switch")
    public ApiResponse<Object> switchTeam(@Valid @RequestBody RequestSwitchTeamDto requestDto) {
        ResponseSwitchTeamDto responseDto = gameRoomService.switchTeam(requestDto);
        return ApiResponse.ofSuccess(responseDto);
    }

    @Tag(name = "멀티모드 퀴즈")
    @Operation(summary = "준비하기")
    @PostMapping("/ready")
    public ApiResponse<Object> ready(@Valid @RequestBody RequestReadyDto requestDto) {
        ResponseReadyDto responseDto = gameRoomService.ready(requestDto);
        return ApiResponse.ofSuccess(responseDto);
    }

    @Tag(name = "멀티모드 퀴즈")
    @Operation(summary = "유저 강퇴(방장 권한 필요)")
    @PostMapping("/kick")
    public ApiResponse<Object> kickUser(@Valid @RequestBody RequestKickUserDto requestDto) {
        ResponseKickUserDto responseDto = gameRoomService.kickUser(requestDto);
        return ApiResponse.ofSuccess(responseDto);
    }
}