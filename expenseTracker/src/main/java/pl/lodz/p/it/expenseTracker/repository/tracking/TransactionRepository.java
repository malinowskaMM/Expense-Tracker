package pl.lodz.p.it.expenseTracker.repository.tracking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.expenseTracker.domain.entity.Account;
import pl.lodz.p.it.expenseTracker.domain.entity.Category;
import pl.lodz.p.it.expenseTracker.domain.entity.Transaction;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findTransactionByAccount(Account account);

    List<Transaction> findTransactionByCategory(Category category);

    Optional<Transaction> findTransactionByName(String name);

    @Modifying
    @Transactional(transactionManager = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM Transaction t WHERE t.id = :id")
    void deleteById(@Param("id") Long id);

    @Modifying
    @Transactional(transactionManager = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM Transaction")
    void deleteAll();
}
