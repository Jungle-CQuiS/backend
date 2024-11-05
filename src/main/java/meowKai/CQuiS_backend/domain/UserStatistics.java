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
    @OneToOne(fetch = LAZY)
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

    public static UserStatistics createUserStatistics(User user) {
        return UserStatistics.builder()
                .user(user)
                .solvedCount(0)
                .wrongCount(0)
                .honorCount(0)
                .correctRate("0.00%")
                .build();
    }

    public void addHonorCount() {
        this.honorCount++;
    }

    // 유저가 푼 문제 수를 업데이트
    public void updateSolvedCount(int solvedCount) {
        this.solvedCount += solvedCount;
    }

    // 유저의 정답률 업데이트
    public void updateCorrectRate() {
        this.correctRate = String.format("%.2f", (double)((solvedCount - wrongCount) / solvedCount * 100)).concat("%");
    }

    // 유저가 틀린 문제 수를 업데이트
    public void updateWrongCount(int wrongCount) {
        this.wrongCount += wrongCount;
    }
}
