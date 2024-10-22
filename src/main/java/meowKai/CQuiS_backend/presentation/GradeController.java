package meowKai.CQuiS_backend.presentation;

import lombok.RequiredArgsConstructor;
import meowKai.CQuiS_backend.application.GradeService;
import meowKai.CQuiS_backend.dto.request.RequestGradeDto;
import meowKai.CQuiS_backend.dto.response.ResponseGradeDto;
import meowKai.CQuiS_backend.global.base.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quiz/multi")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @PostMapping("/grade")
    public ApiResponse<Object> checkGrade(@RequestBody RequestGradeDto requestGradeDto) {
        ResponseGradeDto responseDto = gradeService.checkGrade(requestGradeDto);
        return ApiResponse.ofSuccess(responseDto);
    }
}
