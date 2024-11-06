package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.Quiz;
import meowKai.CQuiS_backend.domain.ShortAnsQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortAnsQuizRepository extends JpaRepository<ShortAnsQuiz, Long> {
    ShortAnsQuiz findByQuiz(Quiz quiz);
}