package meowKai.CQuiS_backend.domain;

import jakarta.persistence.*;
import lombok.*;
import meowKai.CQuiS_backend.global.base.BaseEntity;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class QuizUserVotedown extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    // 퀴즈와 매핑
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    // 유저와 매핑
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public static QuizUserVotedown createEntity(Quiz quiz, User user) {
        return QuizUserVotedown.builder()
                .quiz(quiz)
                .user(user)
                .build();
    }
}
