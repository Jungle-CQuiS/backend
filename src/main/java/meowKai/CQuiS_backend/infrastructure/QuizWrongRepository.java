package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.QuizWrong;
import meowKai.CQuiS_backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizWrongRepository extends JpaRepository<QuizWrong, Long> {
    List<QuizWrong> findByUser(User user);
}
