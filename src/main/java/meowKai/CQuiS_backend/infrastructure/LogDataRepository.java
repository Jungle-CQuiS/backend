package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.LogData;
import meowKai.CQuiS_backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LogDataRepository extends JpaRepository<LogData, Long> {
    Optional<LogData> findByUser(User user);
}
