package meowKai.CQuiS_backend.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import meowKai.CQuiS_backend.application.AdminService;
import meowKai.CQuiS_backend.dto.request.RequestCreateNewMultipleAnswerQuiz;
import meowKai.CQuiS_backend.dto.request.RequestCreateNewShortAnswerQuizDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateNewMultipleAnswerQuizDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateNewShortAnswerQuizDto;
import meowKai.CQuiS_backend.global.base.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin") // TODO: 추후에 admin 권한 가진 계정만 접근 가능하도록 수정
public class AdminController {

    private final AdminService adminService;

    @Tag(name = "관리자")
    @Operation(summary = "주관식 퀴즈 생성")
    @PostMapping("/quiz-creation/short-answer")
    public ApiResponse<Object> createNewShortAnswerQuiz(@Valid @RequestBody RequestCreateNewShortAnswerQuizDto requestDto) {
        ResponseCreateNewShortAnswerQuizDto responseDto = adminService.createNewShortQuiz(requestDto);
        return ApiResponse.ofSuccess(responseDto);
    }

    @Tag(name = "관리자")
    @Operation(summary = "객관식 퀴즈 생성")
    @PostMapping("/quiz-creation/multiple-choice")
    public ApiResponse<Object> createNewMultipleQuiz(@Valid @RequestBody RequestCreateNewMultipleAnswerQuiz requestDto) {
        ResponseCreateNewMultipleAnswerQuizDto responseDto = adminService.createNewMultipleQuiz(requestDto);
        return ApiResponse.ofSuccess(responseDto);
    }

    @Tag(name = "관리자")
    @Operation(summary = "주관식 퀴즈 여러개 생성하기")
    @PostMapping("/quiz-creation/short-answer/multiple")
    public ApiResponse<Object> createNewMultipleShortAnswerQuiz(@Valid @RequestBody List<RequestCreateNewShortAnswerQuizDto> requestList) {
        adminService.createNewMultipleShortQuiz(requestList);
        return ApiResponse.ofSuccess();
    }

    @Tag(name = "관리자")
    @Operation(summary = "객관식 퀴즈 여러개 생성하기")
    @PostMapping("/quiz-creation/multiple-choice/multiple")
    public ApiResponse<Object> createNewMultipleMultipleAnswerQuiz(@Valid @RequestBody List<RequestCreateNewMultipleAnswerQuiz> requestList) {
        adminService.createNewMultipleMultipleQuiz(requestList);
        return ApiResponse.ofSuccess();
    }
}