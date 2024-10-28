package meowKai.CQuiS_backend.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import meowKai.CQuiS_backend.application.AuthService;
import meowKai.CQuiS_backend.dto.request.RequestSignUpDto;
import meowKai.CQuiS_backend.dto.response.ResponseDuplicateCheckEmailDto;
import meowKai.CQuiS_backend.dto.response.ResponseDuplicateCheckUsernameDto;
import meowKai.CQuiS_backend.dto.response.ResponseSignUpDto;
import meowKai.CQuiS_backend.global.base.ApiResponse;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.bind.annotation.*;

@RestController
@EnableWebSecurity
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Tag(name = "보안")
    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ApiResponse<Object> signUp(@Valid @RequestBody RequestSignUpDto requestDto) {
        ResponseSignUpDto responseDto = authService.signUp(requestDto);
        return ApiResponse.ofSuccess(responseDto);
    }

    @Tag(name = "보안")
    @Operation(summary = "이메일 중복 체크")
    @GetMapping("/email/{email}/duplicate-check")
    public ApiResponse<Object> duplicateCheckEmail(@PathVariable String email) {
        ResponseDuplicateCheckEmailDto responseDto = authService.duplicateCheckEmail(email);
        return ApiResponse.ofSuccess(responseDto);
    }

    @Tag(name = "보안")
    @Operation(summary = "유저네임 중복 체크")
    @GetMapping("/username/{username}/duplicate-check")
    public ApiResponse<Object> duplicateCheckUsername(@PathVariable String username) {
        ResponseDuplicateCheckUsernameDto responseDto = authService.duplicateCheckUsername(username);
        return ApiResponse.ofSuccess(responseDto);
    }

    // TODO: 로그인 시 User 엔티티의 lastAccessed 필드 업데이트 하기
    // TODO: 로그아웃 시 accessToken destroy 처리해야함.
    @Tag(name = "보안")
    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponse<Object> logout() {
        return ApiResponse.ofSuccess();
    }
}
