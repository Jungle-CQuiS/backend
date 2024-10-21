package meowKai.CQuiS_backend.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import meowKai.CQuiS_backend.application.GameRoomService;
import meowKai.CQuiS_backend.dto.MultiRoomListDto;
import meowKai.CQuiS_backend.dto.request.RequestCreateMultiRoomDto;
import meowKai.CQuiS_backend.dto.response.ResponseGetMultiRoomListDto;
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
        List<MultiRoomListDto> multiRoomList = gameRoomService.getMultiRoomList();

        ResponseGetMultiRoomListDto responseDto = ResponseGetMultiRoomListDto.builder()
                .rooms(multiRoomList)
                .build();

        return ApiResponse.ofSuccess(responseDto);
    }

    @Tag(name = "멀티모드 퀴즈")
    @Operation(summary = "퀴즈방 생성")
    @PostMapping("/rooms")
    public ApiResponse<Object> createMultiRoom(@Valid @RequestBody RequestCreateMultiRoomDto requestDto) {
        gameRoomService.createMultiRoom(requestDto);
        return ApiResponse.ofSuccess();
    }
}