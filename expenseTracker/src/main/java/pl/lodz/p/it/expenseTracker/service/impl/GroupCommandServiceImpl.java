package pl.lodz.p.it.expenseTracker.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.expenseTracker.domain.entity.Account;
import pl.lodz.p.it.expenseTracker.domain.entity.AccountGroupRole;
import pl.lodz.p.it.expenseTracker.domain.entity.Category;
import pl.lodz.p.it.expenseTracker.domain.entity.Group;
import pl.lodz.p.it.expenseTracker.domain.enums.GroupRoleEnum;
import pl.lodz.p.it.expenseTracker.domain.events.infomativeEvents.AccountHasBeenUpgradedToAdminOfGroupEmailEvent;
import pl.lodz.p.it.expenseTracker.exceptions.account.AccountIsNotOwnerOfGroup;
import pl.lodz.p.it.expenseTracker.exceptions.account.AccountNotBelongToGroupException;
import pl.lodz.p.it.expenseTracker.exceptions.account.AccountNotFoundException;
import pl.lodz.p.it.expenseTracker.exceptions.group.GroupNotFoundException;
import pl.lodz.p.it.expenseTracker.exceptions.utils.ETagVerificationFailedException;
import pl.lodz.p.it.expenseTracker.exceptions.utils.OptimisticLockException;
import pl.lodz.p.it.expenseTracker.repository.tracking.AccountGroupRoleRepository;
import pl.lodz.p.it.expenseTracker.repository.administration.AccountRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.CategoryRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.GroupRepository;
import pl.lodz.p.it.expenseTracker.service.GroupCommandService;
import pl.lodz.p.it.expenseTracker.utils.Internationalization;
import pl.lodz.p.it.expenseTracker.utils.LoggerService;
import pl.lodz.p.it.expenseTracker.utils.etag.MessageSigner;
import pl.lodz.p.it.expenseTracker.domain.events.infomativeEvents.AccountLeftGroupEmailEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GroupCommandServiceImpl implements GroupCommandService {

    private final GroupRepository repository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final AccountGroupRoleRepository accountGroupRoleRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final LoggerService logger = new LoggerService();

    private final MessageSigner messageSigner;

    private final Internationalization internationalization;

    @Override
    @Transactional(value = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public Void createGroup(String name, List<String> accountsIds, String ownerId) {
        var account = accountRepository.findAccountById(Long.parseLong(ownerId));

        if (account.isEmpty()) {
            logger.log("AccountNotFoundException occurred: Account with id: " + ownerId + " has not been found.",
                    "GroupCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountNotFoundException(internationalization.getMessage("account.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        Group group = new Group(name);

        List<AccountGroupRole> roles = new ArrayList<>();
        roles.add(new AccountGroupRole(account.get(), group, GroupRoleEnum.ADMIN));

        accountsIds.forEach(accountId -> {
                    if(accountRepository.findAccountById(Long.parseLong(accountId)).isPresent()) {
                        roles.add(new AccountGroupRole(accountRepository.findAccountById(Long.parseLong(accountId)).get(), group, GroupRoleEnum.USER));
                    }
                }
        );
        group.setAccountGroupRoles(roles);
        repository.save(group);

        categoryRepository.save(new Category("Default category", "#d0d0d0", "Default category", true, group));

        logger.log(" Account with id: " + ownerId + " created successfully group with name: " + name + ".",
                "GroupCommandService", LoggerService.LoggerServiceLevel.INFO);
        return null;
    }

    @Retryable
    @Transactional(value = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Override
    public Void changeGroup(String id, String ownerAccountId, String name, List<String> emails, String version,
                            String ifMatchHeader) {
        var account = accountRepository.findAccountById(Long.parseLong(ownerAccountId));

        if (account.isEmpty()) {
            logger.log("AccountNotFoundException occurred: Account with id: " + ownerAccountId + " has not been found.",
                    "GroupCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountNotFoundException(internationalization.getMessage("account.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        emails.add(account.get().getEmail());

        var group = repository.findGroupById(Long.parseLong(id));

        if (group.isEmpty()) {
            logger.log("GroupNotFoundException occurred: Group with id: " + id + " has not been found.",
                    "GroupCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new GroupNotFoundException(internationalization.getMessage("group.notFound", account.get().getLanguage_()));
        }

        var modifiedGroup = group.get();

        if (ifMatchHeader == null  || ifMatchHeader.isEmpty() || !ifMatchHeader.equals(messageSigner.sign(group.get()))) {
            logger.log("ETagVerificationFailedException occurred for account id: " + id,
                    "GroupCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new ETagVerificationFailedException(internationalization.getMessage("utils.etagVerificationFailed", account.get().getLanguage_()));
        }

        if (!Objects.equals(version, modifiedGroup.getVersion().toString())) {
            logger.log("OptimisticLockException occurred for account id: " + id,
                    "GroupCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new OptimisticLockException(internationalization.getMessage("utils.optimisticLock", account.get().getLanguage_()));
        }

        var groupAccounts = modifiedGroup.getAccountGroupRoles();                                   //wszytskie relacje dotychczasowe

        var groupAccountsEmails = groupAccounts.stream().map(AccountGroupRole::getAccount)          //wszytskie emaile dotychczasowe
                .map(Account::getEmail).toList();

        var groupOwners = groupAccounts.stream()                                                    // wszytkie relacje ownerów dotychczasowe
                .filter(accountGroupRole -> accountGroupRole.getRole().equals(GroupRoleEnum.ADMIN))
                .toList();

        var groupOwnersAccount = groupOwners.stream().map(AccountGroupRole::getAccount).toList();   //wszytskie konta ownerów dotychczasowe

        var groupOwnersAccountId = groupOwnersAccount.stream().map(Account::getId).toList();        //wszytskie id kont ownerów dotychczasowe


        if (!groupOwnersAccountId.contains(account.get().getId())) {
            logger.log("AccountIsNotOwnerOfGroup occurred: Account with id: " + ownerAccountId + " is not owner of the group with id: " + id + ".",
                    "GroupCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountIsNotOwnerOfGroup(internationalization.getMessage("account.isNotOwnerOfGroup", account.get().getLanguage_()));
        }


        var changedAccounts = new ArrayList<Account>();
        emails.forEach(email -> {
            // dla kazdego emaila z requesta znajdz czy istnieje konto
            // jesli istnieje i adresu email nie ma w dotychczasowych wszytskie emaile, dodaj je
            var accountAfterEmail = accountRepository.findAccountByEmail(email).orElse(null);
            if (accountAfterEmail != null && !groupAccountsEmails.contains(email)) {
                changedAccounts.add(accountAfterEmail);
            }
        }
        );

        //dotychczasowe emaile - sprawdz czy dla każdego z nich istnieje emial z request
        groupAccountsEmails.forEach(email -> {
            //jesli email z requesta nie zawieraja istniejacego maila - usun relacjee z takim kontem
            if(!emails.contains(email))  {
                var deletedAccount = accountRepository.findAccountByEmail(email).orElse(null);
                var deletedAccountId = deletedAccount != null
                        ? deletedAccount.getId()
                        : null;

                if (deletedAccountId != null) {
                    accountGroupRoleRepository.deleteAccountGroupRoleByAccountId(deletedAccountId);
                }
            }
        });

        var changedAccountGroupRoles = new ArrayList<AccountGroupRole>(groupAccounts); // wszytkie dotychczasowe relacje
        changedAccounts.forEach(account1 -> {           // dla kazdego nowego konta dodaj do nowych relacji
                changedAccountGroupRoles.add(new AccountGroupRole(account1, modifiedGroup, GroupRoleEnum.USER));
        });


        modifiedGroup.setName(name);
        modifiedGroup.setAccountGroupRoles(changedAccountGroupRoles);
        repository.save(modifiedGroup);

        logger.log(" Account with id: " + ownerAccountId + " changed details of group with id: " + id + ".",
                "GroupCommandService", LoggerService.LoggerServiceLevel.INFO);
        return null;
    }

    @Override
    @Retryable
    @Transactional("administrationTransactionManager")
    public void leaveGroup(String id, String accountId) {
        var account = accountRepository.findAccountById(Long.parseLong(accountId));

        if (account.isEmpty()) {
            logger.log("AccountNotFoundException occurred: Account with id: " + accountId + " has not been found.",
                    "GroupCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountNotFoundException(internationalization.getMessage("account.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        var group = repository.findGroupById(Long.parseLong(id));

        if (group.isEmpty()) {
            logger.log("GroupNotFoundException occurred: Group with id: " + id + " has not been found.",
                    "GroupCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new GroupNotFoundException(internationalization.getMessage("group.notFound", account.get().getLanguage_())); }

        if (!account.get().getGroupRoles().stream()
                .map(AccountGroupRole::getGroup)
                .map(Group::getId).toList()
                .contains(group.get().getId())) {
            logger.log("AccountNotBelongToGroupException occurred: Account with id: " + accountId + " not belonged to group with id: " + id + ".",
                    "GroupCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountNotBelongToGroupException(internationalization.getMessage("account.notBelongToGroup", account.get().getLanguage_()));
        }

        var groupRoles = account.get().getGroupRoles().stream()
                .filter(groupRole -> groupRole.getGroup().getId().equals(group.get().getId())).toList();
        groupRoles.forEach(role -> accountGroupRoleRepository.deleteAccountGroupRoleById(role.getId()));


        eventPublisher.publishEvent(new AccountLeftGroupEmailEvent(account.get(), account.get().getEmail(),
                account.get().getLanguage_(), group.get().getId(), group.get().getName()));

        logger.log(" Account with id: " + accountId + " left group with id: " + id + ".",
                "GroupCommandService", LoggerService.LoggerServiceLevel.INFO);
    }

    @Override
    public Void changeHeadOfGroup(String id, List<String> newOwnerIds, String version, String ifMatchHeader) {
        newOwnerIds.forEach(newOwnerId -> changeSingleHeadOfGroup(id, newOwnerId) );
        return null;
    }

    private void changeSingleHeadOfGroup(String id, String newOwnerId) {
        var account = accountRepository.findAccountById(Long.parseLong(newOwnerId));

        if (account.isEmpty()) {
            logger.log("AccountNotFoundException occurred: Account with id: " + newOwnerId + " has not been found.",
                    "GroupCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountNotFoundException(internationalization.getMessage("account.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        var group = repository.findGroupById(Long.parseLong(id));

        if (group.isEmpty()) {
            logger.log("GroupNotFoundException occurred: Group with id: " + id + " has not been found.",
                    "GroupCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new GroupNotFoundException(internationalization.getMessage("group.notFound", account.get().getLanguage_())); }

        var changedGroup = group.get();

        var notChangingRelations = new ArrayList<>(changedGroup.getAccountGroupRoles().stream()
                .filter(accountGroupRole -> accountGroupRole.getAccount().getId() != Long.parseLong(newOwnerId)).toList());

        var newOwnerRelation = changedGroup.getAccountGroupRoles().stream()
                .filter(accountGroupRole -> accountGroupRole.getAccount().getId() == Long.parseLong(newOwnerId)).findFirst();

        if (newOwnerRelation.isPresent()) {
            newOwnerRelation.get().setRole(GroupRoleEnum.ADMIN);
            notChangingRelations.add(newOwnerRelation.get());
        }

        changedGroup.setAccountGroupRoles(notChangingRelations);
        repository.save(changedGroup);

//        if(!changedGroup.getAccountGroupRoles().stream()
//                .filter(accountGroupRole -> accountGroupRole.getAccount().getId().equals(account.get().getId())
//                        && accountGroupRole.getRole().equals(GroupRoleEnum.USER)).toList().isEmpty()) {
//            var role = changedGroup.getAccountGroupRoles().stream().filter(accountGroupRole ->
//                    accountGroupRole.getAccount().equals(account.get())
//                            && accountGroupRole.getRole().equals(GroupRoleEnum.USER)).findFirst();
//            role.ifPresent(accountGroupRole -> changedGroup.getAccountGroupRoles().remove(accountGroupRole));
//        }
//        changedGroup.getAccountGroupRoles().add(new AccountGroupRole(account.get(), changedGroup, GroupRoleEnum.ADMIN));
//        repository.save(changedGroup);

        eventPublisher.publishEvent(new AccountHasBeenUpgradedToAdminOfGroupEmailEvent(account.get(), account.get().getEmail(),
                account.get().getLanguage_(), group.get().getId(), group.get().getName()));

        logger.log(" Account with id: " + newOwnerId + " has been upgraded to admin of group with id: " + id + ".",
                "GroupCommandService", LoggerService.LoggerServiceLevel.INFO);
    }

}
