package pl.lodz.p.it.expenseTracker.repository.administration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.expenseTracker.domain.entity.Account;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findAccountById(Long id);

    Optional<Account> findAccountByEmail(String email);

    @Transactional(transactionManager = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    Account save(Account account);

    @Modifying
    @Transactional(transactionManager = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM Account e WHERE e.id = :id")
    void deleteById(@Param("id") Long id);

    @Modifying
    @Transactional(transactionManager = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE Account a SET a.isActive = :isActive WHERE a.id = :id")
    void updateIsActiveById(Long id, boolean isActive);

    @Modifying
    @Transactional(transactionManager = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE Account a SET a.registerDate = :registerDate WHERE a.id = :id")
    void updateRegisterDateByLong(Long id, LocalDateTime registerDate);

    @Modifying
    @Transactional(transactionManager = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE Account a SET a.isArchived = :isArchived WHERE a.id = :id")
    void updateIsArchivedById(Long id, boolean isArchived);

    @Modifying
    @Transactional(transactionManager = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE Account a SET a.isEnabled = :isEnabled WHERE a.id = :id")
    void updateIsEnabledById(Long id, boolean isEnabled);

    @Modifying
    @Transactional(transactionManager = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE Account a SET a.language_ = :language WHERE a.id = :id")
    void updateLanguageById(Long id, String language);

    @Modifying
    @Transactional(transactionManager = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE Account a SET a.password = :password WHERE a.id = :id")
    void updatePasswordById(Long id, String password);

    @Modifying
    @Transactional(transactionManager = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE Account a SET a.email = :email WHERE a.id = :id")
    void updateEmailById(Long id, String email);

    @Modifying
    @Transactional(transactionManager = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM AccountGroupRole agr WHERE agr.account.id = :accountId AND agr.group.id = :groupId")
    void deleteAccountGroupRoleByGroupIdAndAccountId(Long groupId, Long accountId);

}