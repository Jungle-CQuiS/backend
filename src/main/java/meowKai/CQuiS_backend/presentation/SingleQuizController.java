package meowKai.CQuiS_backend.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import meowKai.CQuiS_backend.application.QuizService;
import meowKai.CQuiS_backend.dto.request.RequestGetChoiceAnswerQuizzesDto;
import meowKai.CQuiS_backend.dto.request.RequestGetMixAnswerQuizzesDto;
import meowKai.CQuiS_backend.dto.request.RequestGetShortAnswerQuizzesDto;
import meowKai.CQuiS_backend.dto.request.RequestSaveSingleGameStatisticsDto;
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
    @Operation(summary = "퀴즈 설정 조건에 따른 주관식 + 객관식 문제 요청하기") // TODO: 이동희: 이건 타임어택용인가요?
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
}