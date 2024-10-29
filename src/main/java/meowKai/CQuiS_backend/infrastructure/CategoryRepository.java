package meowKai.CQuiS_backend.infrastructure;


import meowKai.CQuiS_backend.domain.Category;
import meowKai.CQuiS_backend.domain.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategory(CategoryType categoryType);
}
