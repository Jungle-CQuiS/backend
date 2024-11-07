package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.UserQuizLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserQuizLogRepository extends JpaRepository<UserQuizLog, Long> {
}
