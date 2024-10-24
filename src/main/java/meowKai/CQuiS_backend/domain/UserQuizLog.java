package meowKai.CQuiS_backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class UserQuizLog {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    // user와 매핑
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 전체 문제 갯수
    @Column
    private Integer quizCount;

    // 틀린 문제 갯수
    @Column
    private Integer wrongCount;

    // 퀴즈 진행 날짜
    @Column
    private LocalDateTime createdDate;

    // 멀티 모드 여부
    @Column
    private Boolean multimode;

    // 받은 명예 수
    @Column
    private Integer honorCount;
}
