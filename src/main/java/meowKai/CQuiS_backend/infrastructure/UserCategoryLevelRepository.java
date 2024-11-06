package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.User;
import meowKai.CQuiS_backend.domain.UserCategoryLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCategoryLevelRepository extends JpaRepository<UserCategoryLevel, Long> {
    List<UserCategoryLevel> findByUser(User user);
    Optional<UserCategoryLevel> findByUserAndCategoryId(User user, Long categoryId);
}