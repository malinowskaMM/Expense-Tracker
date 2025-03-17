package pl.lodz.p.it.expenseTracker.repository.tracking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.expenseTracker.domain.entity.Category;
import pl.lodz.p.it.expenseTracker.domain.entity.Group;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findCategoryById(Long id);

    @Query("SELECT c FROM Category c WHERE c.id = :groupId AND  c.isDefault = :defaultIs")
    Optional<Category> findCategoryByGroupIdAndDefault(Long groupId, boolean defaultIs);

    List<Category> findCategoriesByGroup(Group group);

    @Modifying
    @Transactional(transactionManager = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM Category e WHERE e.id = :id")
    void deleteById(@Param("id") Long id);

    @Modifying
    @Transactional(transactionManager = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE Category c SET c.name = :name WHERE c.id = :id")
    void updateCategoryNameById(Long id, String name);

    @Modifying
    @Transactional(transactionManager = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE Category c SET c.color = :color WHERE c.id = :id")
    void updateCategoryColorById(Long id, String color);

    @Modifying
    @Transactional(transactionManager = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE Category c SET c.description = :description WHERE c.id = :id")
    void updateCategoryDescriptionById(Long id, String description);

    @Modifying
    @Transactional(transactionManager = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE Category c SET c.group = :group WHERE c.id = :id")
    void updateCategoryGroupById(Long id, Group group);

    @Modifying
    @Transactional(transactionManager = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM Category")
    void deleteAll();
}
