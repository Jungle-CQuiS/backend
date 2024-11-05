package meowKai.CQuiS_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import meowKai.CQuiS_backend.global.base.BaseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Builder
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "user")
    private RoomUser roomUser;

    @OneToOne(mappedBy = "user", cascade = ALL)
    private UserStatistics userStatistics;

    @OneToMany(mappedBy = "user")
    private List<UserQuizLog> userQuizLogs;

    @OneToMany(mappedBy = "user", cascade = ALL)
    private List<UserCategoryLevel> userCategoryLevels;

    @OneToMany(mappedBy = "user")
    private List<QuizWrong> quizWrongs;

    // 유저의 email
    @Column
    private String email;

    // 유저의 닉네임(username)
    @Column
    private String username;

    // 유저의 마지막 접속시간
    @Column
    private LocalDateTime lastAccessed;

    // 유저의 비밀번호
    @Column
    private String password;

    // 유저의 uuid
    @Column
    private UUID uuid;

    @Column(length = 1000)
    private String refreshToken;

    /**
     * 엔티티 비즈니스 로직
     */

    // 유저 생성
    public static User createUser(String email, String username, String password) {
        User user = User.builder()
                .email(email)
                .username(username)
                .password(password)
                .uuid(java.util.UUID.randomUUID())
                .lastAccessed(LocalDateTime.now())
                .build();

        // UserStatistics 생성 및 연관관계 설정
        user.userStatistics = UserStatistics.createUserStatistics(user);

        return user;
    }

    // 유저의 마지막 접속시간 업데이트
    public void updateLastAccessed() {
        this.lastAccessed = LocalDateTime.now();
    }

    // 유저의 refresh token 업데이트
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // 유저의 refresh token 삭제
    public void removeRefreshToken() {
        this.refreshToken = null;
    }


    // 비밀번호 암호화
    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(password);
    }
}
