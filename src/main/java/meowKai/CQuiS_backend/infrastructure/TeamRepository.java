package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
}
