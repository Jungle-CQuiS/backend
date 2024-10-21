package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUuid(UUID uuid);
    Boolean existsUserByEmail(String email);
    Boolean existsUserByUsername(String username);

    //TODO: 테스트용 지우기!
    User findByEmailAndPassword(String email, String password);
}