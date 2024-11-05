package meowKai.CQuiS_backend.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import meowKai.CQuiS_backend.domain.Category;
import meowKai.CQuiS_backend.domain.User;
import meowKai.CQuiS_backend.domain.UserCategoryLevel;
import meowKai.CQuiS_backend.dto.request.RequestSignUpDto;
import meowKai.CQuiS_backend.dto.response.ResponseDuplicateCheckEmailDto;
import meowKai.CQuiS_backend.dto.response.ResponseDuplicateCheckUsernameDto;
import meowKai.CQuiS_backend.dto.response.ResponseSignUpDto;
import meowKai.CQuiS_backend.infrastructure.CategoryRepository;
import meowKai.CQuiS_backend.infrastructure.UserCategoryLevelRepository;
import meowKai.CQuiS_backend.infrastructure.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final UserCategoryLevelRepository userCategoryLevelRepository;

    // 회원가입
    @Override
    @Transactional
    public ResponseSignUpDto signUp(RequestSignUpDto requestSignUpDto) {
        log.info("회원가입 요청 : {}", requestSignUpDto);
        User createdUser = User.createUser(
                requestSignUpDto.getEmail(),
                requestSignUpDto.getUsername(),
                requestSignUpDto.getPassword()
        );
        createdUser.encodePassword(passwordEncoder);
        User savedUser = userRepository.save(createdUser);

        // 존재하는 모든 카테고리 가져오기
        List<Category> categories = categoryRepository.findAll();

        // 유저 카테고리 레벨 데이터 생성하기
        categories.forEach(
                category -> userCategoryLevelRepository.save(UserCategoryLevel.createUserCategoryLevel(savedUser, category))
        );

        ResponseSignUpDto responseDto = ResponseSignUpDto
                .builder()
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .uuid(savedUser.getUuid())
                .build();
        log.info("회원가입 결과 : {}", responseDto);
        return responseDto;
    }

    // 이메일 중복 체크
    @Override
    public ResponseDuplicateCheckEmailDto duplicateCheckEmail(String email) {
        log.info("이메일 중복 체크 요청 : {}", email);
        Boolean exists = userRepository.existsUserByEmail(email);
        ResponseDuplicateCheckEmailDto responseDto =
                ResponseDuplicateCheckEmailDto.builder().emailIsDuplicate(exists).build();
        log.info("이메일 중복 체크 결과 : {}", responseDto);
        return responseDto;
    }

    // 유저네임 중복 체크
    @Override
    public ResponseDuplicateCheckUsernameDto duplicateCheckUsername(String username) {
        log.info("유저네임 중복 체크 요청 : {}", username);
        Boolean exists = userRepository.existsUserByUsername(username);
        ResponseDuplicateCheckUsernameDto responseDto =
                ResponseDuplicateCheckUsernameDto.builder().usernameIsDuplicate(exists).build();
        log.info("유저네임 중복 체크 결과 : {}", responseDto);
        return responseDto;
    }
}