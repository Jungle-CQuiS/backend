package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.Quiz;
import meowKai.CQuiS_backend.domain.QuizUserVotedown;
import meowKai.CQuiS_backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizUserVotedownRepository extends JpaRepository<QuizUserVotedown, Long> {
    Optional<QuizUserVotedown> findByUserAndQuiz(User user, Quiz quiz);
}
