package pl.lodz.p.it.expenseTracker.repository.tracking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.expenseTracker.domain.entity.AccountGroupRole;

import java.util.List;

public interface AccountGroupRoleRepository extends ListCrudRepository<AccountGroupRole, Long> {

    List<AccountGroupRole> findAccountGroupRoleByAccountId(Long accountId);

    List<AccountGroupRole> findAccountGroupRoleByGroupId(Long groupId);

    @Transactional(transactionManager = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    <S extends AccountGroupRole> List<S> saveAll(Iterable<S> entities);

    @Modifying
    @Transactional(transactionManager = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM AccountGroupRole agr WHERE agr.id = :id")
    void deleteAccountGroupRoleById(Long id);

    @Modifying
    @Transactional(transactionManager = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM AccountGroupRole agr WHERE agr.account.id = :id")
    void deleteAccountGroupRoleByAccountId(Long id);

    @Modifying
    @Transactional(transactionManager = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM AccountGroupRole agr WHERE agr.group.id = :groupId")
    void deleteAllByGroupId(Long groupId);

    @Modifying
    @Transactional(transactionManager = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM AccountGroupRole")
    void deleteAll();
}
