package meowKai.CQuiS_backend.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.application.UtilService;
import meowKai.CQuiS_backend.dto.request.RequestCreateChoiceQuizzesFromTextDto;
import meowKai.CQuiS_backend.dto.request.RequestCreateNewChoiceAnswerQuiz;
import meowKai.CQuiS_backend.dto.request.RequestCreateNewShortAnswerQuizDto;
import meowKai.CQuiS_backend.dto.request.RequestCreateShortQuizzesFromTextDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateChoiceQuizFromTextDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateNewChoiceAnswerQuizDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateNewShortAnswerQuizDto;
import meowKai.CQuiS_backend.dto.response.ResponseCreateShortQuizzesFromTextDto;
import meowKai.CQuiS_backend.global.base.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/util") // TODO: 추후에 admin 권한 가진 계정만 접근 가능하도록 수정
public class UtilController {

    private final UtilService utilService;

    @Tag(name = "유틸리티")
    @Operation(summary = "서버 상태 체크")
    @GetMapping("/health-check")
    public ApiResponse<Object> healthCheck() {
        return ApiResponse.ofSuccess();
    }

    @Tag(name = "유틸리티")
    @Operation(summary = "유저로부터 문제 생성을 위한 텍스트 받아서 주관식 퀴즈 생성")
    @PostMapping("/quiz-creation/short-answer")
    public ApiResponse<Object> createShortAnsQuizFromText(@Valid @RequestBody RequestCreateShortQuizzesFromTextDto requestDto) {
        try {
            ResponseCreateShortQuizzesFromTextDto responseDto = utilService.generateShortAnswerQuizzesFromText(requestDto);
            return ApiResponse.ofSuccess(responseDto);
        }
        catch (Exception e) {
            return ApiResponse.ofFail("입력한 텍스트가 너무 길어요! 텍스트를 줄여서 다시 시도해주세요.");
        }
    }

    @Tag(name = "유틸리티")
    @Operation(summary = "유저로부터 문제 생성을 위한 텍스트 받아서 객관식 퀴즈 생성")
    @PostMapping("/quiz-creation/choice-answer")
    public ApiResponse<Object> createChoiceAnsQuizFromText(@Valid @RequestBody RequestCreateChoiceQuizzesFromTextDto requestDto) {
        try {
            log.info("generatedChoiceAnswerQuizzesFromText 메서드 호출됨");
            List<ResponseCreateChoiceQuizFromTextDto> response = utilService.generateChoiceAnswerQuizzesFromText(requestDto);
            return ApiResponse.ofSuccess(response);
        }
        catch (Exception e) {
            log.info("에러 발생: " + e.getMessage());
            return ApiResponse.ofFail(e.getMessage());
        }
    }

    @Tag(name = "유틸리티")
    @Operation(summary = "주관식 퀴즈 생성")
    @PostMapping("/quiz-insertion/short-answer")
    public ApiResponse<Object> createNewShortAnswerQuiz(@Valid @RequestBody RequestCreateNewShortAnswerQuizDto requestDto) {
        ResponseCreateNewShortAnswerQuizDto responseDto = utilService.createNewShortQuiz(requestDto);
        return ApiResponse.ofSuccess(responseDto);
    }

    @Tag(name = "유틸리티")
    @Operation(summary = "객관식 퀴즈 생성")
    @PostMapping("/quiz-insertion/multiple-choice")
    public ApiResponse<Object> createNewChoiceAnswerQuiz(@Valid @RequestBody RequestCreateNewChoiceAnswerQuiz requestDto) {
        ResponseCreateNewChoiceAnswerQuizDto responseDto = utilService.createNewChoiceQuiz(requestDto);
        return ApiResponse.ofSuccess(responseDto);
    }

    @Tag(name = "유틸리티")
    @Operation(summary = "주관식 퀴즈 여러개 생성하기")
    @PostMapping("/quiz-insertion/short-answer/multiple")
    public ApiResponse<Object> createNewMultipleShortAnswerQuiz(@Valid @RequestBody List<RequestCreateNewShortAnswerQuizDto> requestList) {
        utilService.createNewMultipleShortQuiz(requestList);
        return ApiResponse.ofSuccess();
    }

    @Tag(name = "유틸리티")
    @Operation(summary = "객관식 퀴즈 여러개 생성하기")
    @PostMapping("/quiz-insertion/multiple-choice/multiple")
    public ApiResponse<Object> createNewMultipleChoiceAnswerQuiz(@Valid @RequestBody List<RequestCreateNewChoiceAnswerQuiz> requestList) {
        utilService.createNewMultipleChoiceQuiz(requestList);
        return ApiResponse.ofSuccess();
    }
}