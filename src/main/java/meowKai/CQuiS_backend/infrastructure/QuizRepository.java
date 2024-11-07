package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query(value = "SELECT q.* " +
            "FROM quiz q " +
            "LEFT JOIN quiz_user_votedown quv " +
            "ON q.id = quv.quiz_id " +
            "AND quv.user_id = :userId " +  // 유저 ID를 변수로 사용
            "WHERE q.category_id = :categoryId " +  // 실제 categoryId 변수
            "AND q.type = :quizType " +  // 실제 quizType 변수
            "AND quv.user_id IS NULL " +  // 유저가 비추천한 퀴즈는 제외
            "ORDER BY RAND() " +
            "LIMIT :count", nativeQuery = true)
    List<Quiz> findAllByCategoryIdAndQuizTypeExcludingDownvote(
            @Param("categoryId") Long categoryId,
            @Param("userId") Long userId,
            @Param("count") int count,
            @Param("quizType") String quizType
    );

    @Query(value = "SELECT * FROM quiz WHERE category_id = :categoryId ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<Quiz> findRandomQuizByCategoryId(@Param("categoryId") Long categoryId, @Param("count") int count);
    List<Quiz> findAllByCategoryId(Long categoryId);

    @Query ("SELECT q FROM Quiz q " +
            "LEFT JOIN FETCH q.choiceAnsQuiz " +
            "LEFT JOIN FETCH q.shortAnsQuiz " +
            "LEFT JOIN FETCH q.category " +
            "WHERE q.category.id = :categoryId " +
            "ORDER BY RAND() " +
            "LIMIT :count")
    List<Quiz> findRandomQuizByCategoryId(@Param("categoryId") Long categoryId, @Param("count") int count);
}
