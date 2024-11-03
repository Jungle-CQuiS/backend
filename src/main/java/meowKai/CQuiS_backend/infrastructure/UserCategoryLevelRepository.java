package meowKai.CQuiS_backend.infrastructure;

import meowKai.CQuiS_backend.domain.User;
import meowKai.CQuiS_backend.domain.UserCategoryLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCategoryLevelRepository extends JpaRepository<UserCategoryLevel, Long> {
    List<UserCategoryLevel> findByUser(User user);
}