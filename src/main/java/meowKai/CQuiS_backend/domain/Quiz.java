package meowKai.CQuiS_backend.domain;

import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class Quiz {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    // category와 매핑
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // 객관식 퀴즈와 매핑
    @OneToOne(fetch = LAZY, mappedBy = "quiz")
    private MultiAnsQuiz multiAnsQuiz;

    // 주관식 퀴즈와 매핑
    @OneToOne(fetch = LAZY, mappedBy = "quiz")
    private ShortAnsQuiz shortAnsQuiz;

    // 문제 질문
    @Column
    private String name;

    // 문제 타입
    @Column
    @Enumerated(STRING)
    private QuizType type;

}