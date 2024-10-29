package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.MultiAnsQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MultiAnsQuizRepository extends JpaRepository<MultiAnsQuiz, Long> {
}
