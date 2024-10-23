package meowKai.CQuiS_backend.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import meowKai.CQuiS_backend.application.AuthService;
import meowKai.CQuiS_backend.domain.User;
import meowKai.CQuiS_backend.dto.request.RequestSignUpDto;
import meowKai.CQuiS_backend.dto.response.ResponseDuplicateCheckEmailDto;
import meowKai.CQuiS_backend.dto.response.ResponseDuplicateCheckUsernameDto;
import meowKai.CQuiS_backend.global.base.ApiResponse;
import meowKai.CQuiS_backend.infrastructure.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository; // TODO: 테스트용 지우기!

    @Tag(name = "보안")
    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ApiResponse<Object> signUp(@Valid @RequestBody RequestSignUpDto requestDto) {
        authService.signUp(requestDto);
        return ApiResponse.ofSuccess();
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

    // TODO: 로그아웃, 로그인 Spring Security로 구현하기
    // TODO: 로그인 시 User 엔티티의 lastAccessed 필드 업데이트 하기
    @Tag(name = "보안")
    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponse<Object> logout() {
        return ApiResponse.ofSuccess();
    }

    // TODO: 임시로그인(테스트용 수정하기!)
    @Tag(name = "보안")
    @Operation(summary = "로그인")
    @PostMapping("/login/{email}/{password}")
    public ApiResponse<Object> login(@PathVariable String email, @PathVariable String password) {
        User testUser = userRepository.findByEmailAndPassword(email, password);
        return ApiResponse.ofSuccess(testUser.getUuid());
    }
}
