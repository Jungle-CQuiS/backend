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
public class QuizWrong {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    // user와 매핑
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // category와 매핑
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
