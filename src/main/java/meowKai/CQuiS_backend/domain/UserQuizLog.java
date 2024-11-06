package meowKai.CQuiS_backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import meowKai.CQuiS_backend.dto.request.RequestSaveUserQuizLogDto;
import meowKai.CQuiS_backend.global.base.BaseEntity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class UserQuizLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "log_data_id")
    private LogData logData;

    // 퀴즈의 id
    @Column
    private Long quizId;

    // 퀴즈의 질문
    @Column
    private String quizName;

    // 퀴즈의 타입(객관식, 주관식)
    @Column
    private String quizType;

    // 카테고리의 아이디
    @Column
    private Long categoryId;

    // 카테고리 명
    @Column
    private String categoryType;

    // 정답 여부
    @Column
    private Boolean isCorrect;

    /**
     * 엔티티 비즈니스 로직
     */
    public static UserQuizLog createUserQuizLog(RequestSaveUserQuizLogDto.QuizLogData dto, LogData logData) {
        UserQuizLog createdUserQuizLog = UserQuizLog.builder()
                .logData(logData)
                .quizId(dto.getQuizId())
                .quizName(dto.getQuizName())
                .quizType(dto.getQuizType())
                .categoryId(dto.getCategoryId())
                .categoryType(dto.getCategoryType())
                .isCorrect(dto.getIsCorrect())
                .build();
        return createdUserQuizLog;
    }
}