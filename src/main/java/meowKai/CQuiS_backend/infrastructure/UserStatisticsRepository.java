package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.User;
import meowKai.CQuiS_backend.domain.UserStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long> {
    Optional<UserStatistics> findByUser(User user);
}
