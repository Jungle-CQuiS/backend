package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.RoomUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomUserRepository extends JpaRepository<RoomUser, Long> {
}
