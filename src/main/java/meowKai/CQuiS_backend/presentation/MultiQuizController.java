package meowKai.CQuiS_backend.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import meowKai.CQuiS_backend.application.GameRoomService;
import meowKai.CQuiS_backend.dto.MultiRoomListDto;
import meowKai.CQuiS_backend.dto.request.*;
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

    @Tag(name = "멀티모드 퀴즈")
    @Operation(summary = "방장 위임")
    @PostMapping("/yield-host")
    public ApiResponse<Object> changeHost(@Valid @RequestBody RequestYieldDto requestDto) {
        ResponseYieldDto responseDto = gameRoomService.changeHost(requestDto);
        return ApiResponse.ofSuccess(responseDto);
    }

    @Tag(name = "멀티모드 퀴즈")
    @Operation(summary = "리더 위임")
    @PostMapping("/yield-leader")
    public ApiResponse<Object> changeLeader(@Valid @RequestBody RequestYieldDto requestDto) {
        ResponseYieldDto responseDto = gameRoomService.changeLeader(requestDto);
        return ApiResponse.ofSuccess(responseDto);
    }

    @Tag(name = "멀티모드 퀴즈")
    @Operation(summary = "방 퇴장")
    @PostMapping("/exit")
    public ApiResponse<Object> exitRoom(@Valid @RequestBody RequestExitDto requestDto) {
        gameRoomService.exit(requestDto); // responseDto로 무엇을 돌려줘야 할까요? RoomUser나 GameRoom의 id를 돌려줬다가 그게 없어졌는데 조회해버리면 어떡하죠?
        return ApiResponse.ofSuccess();
    }

    @Tag(name = "멀티모드 퀴즈")
    @Operation(summary = "방 입장하기")
    @PostMapping("/rooms/join")
    public ApiResponse<Object> joinRoom(@Valid @RequestBody RequestJoinRoomDto requestDto) {
        ResponseJoinRoomDto responseDto = gameRoomService.joinRoom(requestDto);
        return ApiResponse.ofSuccess(responseDto);
    }

    @Tag(name = "멀티모드 퀴즈")
    @Operation(summary = "방 입장 후 방의 정보 가져오기")
    @GetMapping("/rooms/{roomId}")
    public ApiResponse<Object> getRoomInfo(@PathVariable Long roomId) {
        ResponseGetRoomInfoDto responseDto = gameRoomService.getRoomInfo(roomId);
        return ApiResponse.ofSuccess(responseDto);
    }
}