package meowKai.CQuiS_backend.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import meowKai.CQuiS_backend.application.QuizService;
import meowKai.CQuiS_backend.dto.request.RequestGetChoiceAnswerQuizzesDto;
import meowKai.CQuiS_backend.dto.request.RequestGetShortAnswerQuizzesDto;
import meowKai.CQuiS_backend.dto.request.RequestSaveSingleGameStatisticsDto;
import meowKai.CQuiS_backend.dto.response.ResponseGetCategoriesDto;
import meowKai.CQuiS_backend.dto.response.ResponseGetChoiceAnswerQuizzesDto;
import meowKai.CQuiS_backend.dto.response.ResponseGetShortAnswerQuizzesDto;
import meowKai.CQuiS_backend.dto.response.ResponseSaveSingleGameStatisticsDto;
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
        ResponseGetShortAnswerQuizzesDto responseDto = quizService.getShortAnswerQuizzesByConditions(requestDto);
        return ApiResponse.ofSuccess(responseDto);
    }

    @Tag(name = "싱글모드 퀴즈")
    @Operation(summary = "퀴즈 설정 조건에 따른 객관식 문제 요청하기")
    @PostMapping("/choice")
    public ApiResponse<Object> getChoiceQuizzesByConditions(@Valid @RequestBody RequestGetChoiceAnswerQuizzesDto requestDto) {
        ResponseGetChoiceAnswerQuizzesDto responseDto = quizService.getChoiceAnswerQuizzesByConditions(requestDto);
        return ApiResponse.ofSuccess(responseDto);
    }

    @Tag(name = "싱글모드 퀴즈")
    @Operation(summary = "진행한 게임에 대한 통계 정보 저장")
    @PostMapping("/statistics")
    public ApiResponse<Object> saveStatisticsForGame(@Valid @RequestBody RequestSaveSingleGameStatisticsDto requestDto) {
        try {
            ResponseSaveSingleGameStatisticsDto responseDto = quizService.saveStatisticsForSingleGame(requestDto);
            return ApiResponse.ofSuccess(responseDto);
        }
        catch (Exception e) {
            return ApiResponse.ofFail(e.getMessage());
        }
    }
}