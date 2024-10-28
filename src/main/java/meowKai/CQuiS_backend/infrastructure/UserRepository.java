package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUuid(UUID uuid);
    Optional<User> findByRefreshToken(String refreshToken);
    Boolean existsUserByEmail(String email);
    Boolean existsUserByUsername(String username);
}