package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.GameRoom;
import meowKai.CQuiS_backend.domain.GameStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {
    Page<GameRoom> findByGameStatus(GameStatus gameStatus, Pageable pageable);
    Page<GameRoom> findByNameContaining(String roomName, Pageable pageable);
}