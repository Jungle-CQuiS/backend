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
public class MultiAnsQuiz {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    // quiz와 매핑
    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    // 객관식 문제 선택지1
    @Column
    private String choice1;

    // 객관식 문제 선택지2
    @Column
    private String choice2;

    // 객관식 문제 선택지3
    @Column
    private String choice3;

    // 객관식 문제 선택지4
    @Column
    private String choice4;

    // 문제 답
    @Column
    private String answer;
}
