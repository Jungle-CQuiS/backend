package meowKai.CQuiS_backend.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.domain.User;
import meowKai.CQuiS_backend.dto.request.RequestSignUpDto;
import meowKai.CQuiS_backend.dto.response.ResponseDuplicateCheckEmailDto;
import meowKai.CQuiS_backend.dto.response.ResponseDuplicateCheckUsernameDto;
import meowKai.CQuiS_backend.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    // 회원가입
    @Override
    @Transactional
    public void signUp(RequestSignUpDto requestSignUpDto) {
        log.info("회원가입 요청 : {}", requestSignUpDto);
        userRepository.save(
                User.createUser(
                        requestSignUpDto.getEmail(),
                        requestSignUpDto.getUsername(),
                        requestSignUpDto.getPassword()
                )
        );
    }

    // 이메일 중복 체크
    @Override
    public ResponseDuplicateCheckEmailDto duplicateCheckEmail(String email) {
        Boolean exists = userRepository.existsUserByEmail(email);
        return ResponseDuplicateCheckEmailDto.builder().emailIsDuplicate(exists).build();
    }

    // 유저네임 중복 체크
    @Override
    public ResponseDuplicateCheckUsernameDto duplicateCheckUsername(String username) {
        Boolean exists = userRepository.existsUserByUsername(username);
        return ResponseDuplicateCheckUsernameDto.builder().usernameIsDuplicate(exists).build();
    }


}