package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.GameRoom;
import meowKai.CQuiS_backend.domain.RoomUser;
import meowKai.CQuiS_backend.domain.RoomUserTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomUserRepository extends JpaRepository<RoomUser, Long> {
    List<RoomUser> findAllByGameRoom(GameRoom gameRoom);
    List<RoomUser> findAllByGameRoomAndTeam(GameRoom gam, RoomUserTeam team);

    @Query("SELECT r FROM RoomUser r LEFT JOIN FETCH r.user WHERE r.id = :roomUserId")
    Optional<RoomUser> findByIdWithUser(@Param("roomUserId") Long roomUserId);
}
