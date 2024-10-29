package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.ChoiceAnsQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChoiceAnsQuizRepository extends JpaRepository<ChoiceAnsQuiz, Long> {
}
