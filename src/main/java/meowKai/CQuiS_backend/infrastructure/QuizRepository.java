package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

}
