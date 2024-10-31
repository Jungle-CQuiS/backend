package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.GameRoom;
import meowKai.CQuiS_backend.domain.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {
    void removeGameRoomById(Long id);
    List<GameRoom> findByGameStatus(GameStatus gameStatus);
}