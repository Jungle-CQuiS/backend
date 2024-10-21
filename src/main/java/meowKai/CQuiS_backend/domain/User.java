package meowKai.CQuiS_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import meowKai.CQuiS_backend.global.base.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Builder
@Table(name = "quiz_user")
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(fetch = LAZY, mappedBy = "user")
    private RoomUser roomUser;

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


    /**
     * 엔티티 비즈니스 로직
     */

    // 유저 생성
    public static User createUser(String email, String username, String password) {
        return User.builder()
                .email(email)
                .username(username)
                .password(password)
                .uuid(java.util.UUID.randomUUID())
                .lastAccessed(LocalDateTime.now())
                .build();
    }

    // 유저의 마지막 접속시간 업데이트
    public void updateLastAccessed() {
        this.lastAccessed = LocalDateTime.now();
    }
}
