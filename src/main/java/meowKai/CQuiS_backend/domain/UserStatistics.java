package meowKai.CQuiS_backend.domain;

import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class UserStatistics {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    // user와 매핑
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 싱글 모드 정답률
    @Column
    private String correctRate;

    // 전체 푼 문제 수
    @Column
    private Integer solvedCount;

    // 틀린 문제 수
    @Column
    private Integer wrongCount;

    // 멀티 모드 받은 명예 수
    @Column
    private Integer honorCount;
}
