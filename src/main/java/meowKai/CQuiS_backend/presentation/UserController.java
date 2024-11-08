package meowKai.CQuiS_backend.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import meowKai.CQuiS_backend.application.UserService;
import meowKai.CQuiS_backend.dto.request.*;
import meowKai.CQuiS_backend.dto.response.*;
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
    @Operation(summary = "진행한 게임에 대한 통계 정보 저장")
    @PostMapping("/statistics")
    public ApiResponse<Object> saveStatisticsForGame(@Valid @RequestBody RequestSaveSingleGameStatisticsDto requestDto) {
        try {
            ResponseSaveSingleGameStatisticsDto responseDto = userService.saveStatisticsForSingleGame(requestDto);
            return ApiResponse.ofSuccess(responseDto);
        }
        catch (Exception e) {
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
    @Operation(summary = "싱글모드 게임이 끝난 후 유저의 카테고리 별 레벨 데이터 업데이트")
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

    @Tag(name = "유저 정보")
    @Operation(summary = "유저의 게임 로그 저장")
    @PostMapping("/log-data/quiz")
    public ApiResponse<Object> saveUserQuizLog(@Valid @RequestBody RequestSaveUserQuizLogDto requestDto) {
        try {
            userService.saveUserQuizLog(requestDto);
            return ApiResponse.ofSuccess("유저의 게임 로그 저장 완료");
        }
        catch (Exception e) {
            return ApiResponse.ofFail(e.getMessage());
        }
    }

    @Tag(name = "유저 정보")
    @Operation(summary = "유저의 오답 퀴즈 데이터 업데이트")
    @PostMapping("/quiz-wrong/after-game")
    public ApiResponse<Object> updateUserWrongQuiz(@Valid @RequestBody RequestUpdateUserWrongQuizzesDto requestDto) {
        try {
            ResponseUpdateUserWrongQuizzesDto responseDto = userService.updateUserWrongQuizzes(requestDto);
            return ApiResponse.ofSuccess(responseDto);
        }
        catch (Exception e) {
            return ApiResponse.ofFail(e.getMessage());
        }
    }

    @Tag(name = "유저 정보")
    @Operation(summary = "유저의 카테고리 별 오답 퀴즈 데이터 받기")
    @PostMapping("/quiz-wrong")
    public ApiResponse<Object> getUserWrongQuiz(@Valid @RequestBody RequestGetUserWrongQuizzesDto requestDto) {
        try {
            ResponseGetUserWrongQuizzesDto responseDto = userService.getUserWrongQuizzes(requestDto);
            return ApiResponse.ofSuccess(responseDto);
        }
        catch (Exception e) {
            return ApiResponse.ofFail(e.getMessage());
        }
    }
}