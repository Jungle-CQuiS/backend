package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.QuizType;
import meowKai.CQuiS_backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUuid(UUID uuid);
    Optional<User> findByRefreshToken(String refreshToken);
    Boolean existsUserByEmail(String email);
    Boolean existsUserByUsername(String username);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.createdQuizzes q " +
            "LEFT JOIN FETCH q.category c " +
            "WHERE u.uuid = :uuid " +
            "AND c.id IN :categoryIds " +
            "AND (q.type = :quizType OR :quizType IS NULL)")
    Optional<User> findByUuidWithCreatedQuizzes(UUID uuid, List<Long> categoryIds, QuizType quizType);
}