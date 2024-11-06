package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.ChoiceAnsQuiz;
import meowKai.CQuiS_backend.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChoiceAnsQuizRepository extends JpaRepository<ChoiceAnsQuiz, Long> {
    ChoiceAnsQuiz findByQuiz(Quiz quiz);
}
