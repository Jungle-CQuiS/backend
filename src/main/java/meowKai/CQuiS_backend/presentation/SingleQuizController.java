package meowKai.CQuiS_backend.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import meowKai.CQuiS_backend.application.QuizService;
import meowKai.CQuiS_backend.domain.QuizType;
import meowKai.CQuiS_backend.dto.request.*;
import meowKai.CQuiS_backend.dto.response.*;
import meowKai.CQuiS_backend.global.base.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz/single")
public class SingleQuizController {

    private final QuizService quizService;

    @Tag(name = "싱글모드 퀴즈")
    @Operation(summary = "퀴즈 카테고리 종류 받기")
    @GetMapping("/categories")
    public ApiResponse<Object> getCategories() {
        ResponseGetCategoriesDto responseDto = quizService.getCategories();
        return ApiResponse.ofSuccess(responseDto);
    }

    @Tag(name = "싱글모드 퀴즈")
    @Operation(summary = "퀴즈 설정 조건에 따른 주관식 문제 요청하기")
    @PostMapping("/short")
    public ApiResponse<Object> getShortQuizzesByConditions(@Valid @RequestBody RequestGetShortAnswerQuizzesDto requestDto) {
        try {
            ResponseGetShortAnswerQuizzesDto responseDto = quizService.getShortAnswerQuizzesByConditions(requestDto);
            return ApiResponse.ofSuccess(responseDto);
        }
        catch (Exception e) {
            return ApiResponse.ofFail(e.getMessage());
        }
    }

    @Tag(name = "싱글모드 퀴즈")
    @Operation(summary = "퀴즈 설정 조건에 따른 객관식 문제 요청하기")
    @PostMapping("/choice")
    public ApiResponse<Object> getChoiceQuizzesByConditions(@Valid @RequestBody RequestGetChoiceAnswerQuizzesDto requestDto) {
        try {
            ResponseGetChoiceAnswerQuizzesDto responseDto = quizService.getChoiceAnswerQuizzesByConditions(requestDto);
            return ApiResponse.ofSuccess(responseDto);
        }
        catch (Exception e) {
            return ApiResponse.ofFail(e.getMessage());
        }
    }

    @Tag(name = "싱글모드 퀴즈")
    @Operation(summary = "퀴즈 설정 조건에 따른 주관식 + 객관식 문제 요청하기")
    @PostMapping("/mix")
    public ApiResponse<Object> getMixQuizzesByConditions(@Valid @RequestBody RequestGetMixAnswerQuizzesDto requestDto) {
        try {
            ResponseGetMixAnswerQuizzesDto responseDto = quizService.getMixAnswerQuizzesByConditions(requestDto);
            return ApiResponse.ofSuccess(responseDto);
        }
        catch (Exception e) {
            return ApiResponse.ofFail(e.getMessage());
        }
    }

    @Tag(name = "싱글모드 퀴즈")
    @Operation(summary = "카테고리 설정에 따라 내가 만든 문제 중 주관식 문제 요청하기")
    @PostMapping("/my-quiz/short")
    public ApiResponse<Object> getMyShortQuizzes(@Valid @RequestBody RequestGetMyQuizzesDto requestDto) {
        try {
            ResponseGetMyQuizzesDto responseDto = quizService.getMyQuizzes(requestDto, QuizType.SHORT);
            return ApiResponse.ofSuccess(responseDto);
        } catch (Exception e) {
            return ApiResponse.ofFail(e.getMessage());
        }
    }

    @Tag(name = "싱글모드 퀴즈")
    @Operation(summary = "카테고리 설정에 따라 내가 만든 문제 중 객관식 문제 요청하기")
    @PostMapping("/my-quiz/choice")
    public ApiResponse<Object> getMyChoiceQuizzes(@Valid @RequestBody RequestGetMyQuizzesDto requestDto) {
        try {
            ResponseGetMyQuizzesDto responseDto = quizService.getMyQuizzes(requestDto, QuizType.CHOICE);
            return ApiResponse.ofSuccess(responseDto);
        } catch (Exception e) {
            return ApiResponse.ofFail(e.getMessage());
        }
    }

    @Tag(name = "싱글모드 퀴즈")
    @Operation(summary = "카테고리 설정에 따라 내가 만든 문제 모두 요청하기")
    @PostMapping("/my-quiz/mix")
    public ApiResponse<Object> getMyMixQuizzes(@Valid @RequestBody RequestGetMyQuizzesDto requestDto) {
        try {
            ResponseGetMyQuizzesDto responseDto = quizService.getMyQuizzes(requestDto, null);
            return ApiResponse.ofSuccess(responseDto);
        } catch (Exception e) {
            return ApiResponse.ofFail(e.getMessage());
        }
    }
}