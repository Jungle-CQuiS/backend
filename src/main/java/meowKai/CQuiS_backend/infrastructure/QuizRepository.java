package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.Quiz;
import meowKai.CQuiS_backend.domain.QuizType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findAllByCategoryIdAndType(Long categoryId, QuizType type);
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
