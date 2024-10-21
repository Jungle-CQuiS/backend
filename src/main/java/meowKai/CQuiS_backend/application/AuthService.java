package meowKai.CQuiS_backend.application;

import meowKai.CQuiS_backend.dto.request.RequestSignUpDto;
import meowKai.CQuiS_backend.dto.response.ResponseDuplicateCheckEmailDto;
import meowKai.CQuiS_backend.dto.response.ResponseDuplicateCheckUsernameDto;

public interface AuthService {
    void signUp(RequestSignUpDto requestSignUpDto); // 회원가입
    ResponseDuplicateCheckEmailDto duplicateCheckEmail(String email); // 이메일 중복 체크
    ResponseDuplicateCheckUsernameDto duplicateCheckUsername(String username); // 유저네임 중복 체크
}
