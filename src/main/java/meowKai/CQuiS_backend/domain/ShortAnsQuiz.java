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
public class ShortAnsQuiz {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    // quiz와 매핑
    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    // 문제 답
    @Column
    private String answer;
}
