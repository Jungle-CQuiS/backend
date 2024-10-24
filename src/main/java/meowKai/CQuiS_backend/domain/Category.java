package meowKai.CQuiS_backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToMany(fetch = LAZY, mappedBy = "category")
    private List<UserCategoryLevel> userCategoryLevels;

    @OneToMany(fetch = LAZY, mappedBy = "category")
    private List<QuizWrong> quizWrongs;

    @OneToMany(fetch = LAZY, mappedBy = "category")
    private List<Quiz> quizzes;

    // 문제 카테고리명
    @Column
    @Enumerated(STRING)
    private CategoryType category;
}
