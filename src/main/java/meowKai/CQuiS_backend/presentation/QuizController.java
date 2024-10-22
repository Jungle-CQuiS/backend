package meowKai.CQuiS_backend.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import meowKai.CQuiS_backend.application.QuizService;
import meowKai.CQuiS_backend.dto.request.RequestGradeDto;
import meowKai.CQuiS_backend.dto.response.ResponseGradeDto;
import meowKai.CQuiS_backend.global.base.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService gradeService;

    @Tag(name = "채점")
    @Operation(summary = "유사도 검사 바탕 문제 채점")
    @PostMapping("/grade")
    public ApiResponse<Object> checkGrade(@RequestBody RequestGradeDto requestGradeDto) {
        ResponseGradeDto responseDto = gradeService.checkGrade(requestGradeDto);
        return ApiResponse.ofSuccess(responseDto);
    }
}
