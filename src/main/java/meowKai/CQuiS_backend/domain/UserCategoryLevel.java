package meowKai.CQuiS_backend.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import meowKai.CQuiS_backend.global.base.BaseEntity;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class UserCategoryLevel extends BaseEntity {

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

    // 레벨
    @Column
    @NotNull
    private Integer level;

    // 맞춘 문제 수
    @Column
    @NotNull
    private Integer correctCount;

    /**
     * 도메인 비즈니스 로직
     */

    public static UserCategoryLevel createUserCategoryLevel(User user, Category category) {
        return UserCategoryLevel.builder()
                .user(user)
                .category(category)
                .level(1)
                .correctCount(0)
                .build();
    }

    // 유저의 레벨 업데이트
    public void updateLevel(int levelToAdd) {
        this.level += levelToAdd;
    }

    // 맞춘 문제 수 업데이트(업데이트 하면서 레벨도 같이 업데이트)
    public void updateCorrectCount(int correctCountToAdd) {
        int previousCorrect = this.correctCount;
        this.correctCount += correctCountToAdd;
        int levelToAdd = ((previousCorrect % 20) + correctCountToAdd) / 20;
        updateLevel(levelToAdd);
    }


}
