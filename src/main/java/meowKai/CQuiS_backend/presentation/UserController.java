package meowKai.CQuiS_backend.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import meowKai.CQuiS_backend.application.UserService;
import meowKai.CQuiS_backend.dto.request.RequestGetPersonalUserDataDto;
import meowKai.CQuiS_backend.dto.request.RequestGetUserCategoryLevelsDto;
import meowKai.CQuiS_backend.dto.request.RequestGetUserStatisticsDto;
import meowKai.CQuiS_backend.dto.request.RequestUpdateUserCategoryLevelsDto;
import meowKai.CQuiS_backend.dto.response.ResponseGetPersonalUserDataDto;
import meowKai.CQuiS_backend.dto.response.ResponseGetUserCategoryLevelsDto;
import meowKai.CQuiS_backend.dto.response.ResponseGetUserStatisticsDto;
import meowKai.CQuiS_backend.dto.response.ResponseUpdateUserCategoryLevelsDto;
import meowKai.CQuiS_backend.global.base.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Tag(name = "유저 정보")
    @Operation(summary = "유저 정보 받기")
    @PostMapping("/personal-data")
    public ApiResponse<Object> getPersonalData(@Valid @RequestBody RequestGetPersonalUserDataDto requestDto) {
        try {
            ResponseGetPersonalUserDataDto responseDto = userService.getPersonalData(requestDto);
            return ApiResponse.ofSuccess(responseDto);
        } catch (Exception e) {
            return ApiResponse.ofFail(e.getMessage());
        }
    }

    @Tag(name = "유저 정보")
    @Operation(summary = "유저의 퀴즈 통계 데이터 받기")
    @PostMapping("/quiz-statistics")
    public ApiResponse<Object> getUserQuizStatistics(@Valid @RequestBody RequestGetUserStatisticsDto requestDto) {
        try {
            ResponseGetUserStatisticsDto responseDto = userService.getUserQuizStatistics(requestDto);
            return ApiResponse.ofSuccess(responseDto);
        } catch (Exception e) {
            return ApiResponse.ofFail(e.getMessage());
        }
    }

    @Tag(name = "유저 정보")
    @Operation(summary = "유저의 카테고리 별 레벨 데이터 받기")
    @PostMapping("/category-levels")
    public ApiResponse<Object> getUserCategoryLevels(@Valid @RequestBody RequestGetUserCategoryLevelsDto requestDto) {
        try {
            ResponseGetUserCategoryLevelsDto responseDto = userService.getUserCategoryLevels(requestDto);
            return ApiResponse.ofSuccess(responseDto);
        } catch (Exception e) {
            return ApiResponse.ofFail(e.getMessage());
        }
    }

    @Tag(name = "유저 정보")
    @Operation(summary = "게임이 끝난 후 유저의 카테고리 별 레벨 데이터 업데이트")
    @PostMapping("/category-levels/after-game")
    public ApiResponse<Object> updateUserCategoryLevelsAfterGame(@Valid @RequestBody RequestUpdateUserCategoryLevelsDto requestDto) {
        try {
            ResponseUpdateUserCategoryLevelsDto responseDto = userService.updateUserCategoryLevelsAfterGame(requestDto);
            return ApiResponse.ofSuccess(responseDto);
        }
        catch (Exception e) {
            return ApiResponse.ofFail(e.getMessage());
        }
    }
}