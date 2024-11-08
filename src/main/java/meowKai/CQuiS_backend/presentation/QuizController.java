package meowKai.CQuiS_backend.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import meowKai.CQuiS_backend.application.QuizService;
import meowKai.CQuiS_backend.dto.request.RequestDownvoteDto;
import meowKai.CQuiS_backend.dto.request.RequestGradeDto;
import meowKai.CQuiS_backend.dto.response.ResponseGradeDto;
import meowKai.CQuiS_backend.global.base.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @Tag(name = "채점")
    @Operation(summary = "유사도 검사 바탕 문제 채점")
    @PostMapping("/grade")
    public ApiResponse<Object> checkGrade(@RequestBody RequestGradeDto requestGradeDto) {
        ResponseGradeDto responseDto = quizService.checkGrade(requestGradeDto);
        return ApiResponse.ofSuccess(responseDto);
    }

    @Tag(name = "문제 평가하기")
    @Operation(summary = "문제 비추천 하기")
    @PostMapping("/downvote")
    public ApiResponse<Object> downvoteQuiz(@Valid @RequestBody RequestDownvoteDto requestDto) {
        try {
            quizService.downvoteQuiz(requestDto);
            return ApiResponse.ofSuccess("문제 비추천 완료");
        }
        catch (Exception e) {
            return ApiResponse.ofFail("문제 비추천 실패");
        }
    }
}
