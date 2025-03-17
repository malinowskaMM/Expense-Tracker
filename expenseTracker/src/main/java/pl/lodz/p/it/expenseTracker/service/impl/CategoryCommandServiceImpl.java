package pl.lodz.p.it.expenseTracker.service.impl;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.expenseTracker.domain.entity.Category;
import pl.lodz.p.it.expenseTracker.exceptions.account.AccountNotBelongToGroupWhichIsAttachedToGivenCategoryException;
import pl.lodz.p.it.expenseTracker.exceptions.account.AccountNotFoundException;
import pl.lodz.p.it.expenseTracker.exceptions.category.CategoryCannotChangeDefaultCategoryName;
import pl.lodz.p.it.expenseTracker.exceptions.category.CategoryNotFoundException;
import pl.lodz.p.it.expenseTracker.exceptions.category.CategoryNameAlreadyException;
import pl.lodz.p.it.expenseTracker.exceptions.group.GroupNotFoundException;
import pl.lodz.p.it.expenseTracker.exceptions.utils.ETagVerificationFailedException;
import pl.lodz.p.it.expenseTracker.exceptions.utils.OptimisticLockException;
import pl.lodz.p.it.expenseTracker.repository.administration.AccountRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.CategoryRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.GroupRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.TransactionRepository;
import pl.lodz.p.it.expenseTracker.service.CategoryCommandService;
import pl.lodz.p.it.expenseTracker.utils.Internationalization;
import pl.lodz.p.it.expenseTracker.utils.LoggerService;
import pl.lodz.p.it.expenseTracker.utils.etag.MessageSigner;

import java.util.Objects;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class CategoryCommandServiceImpl implements CategoryCommandService {

    private final CategoryRepository repository;

    private final GroupRepository groupRepository;

    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    private final MessageSigner messageSigner;

    private final Internationalization internationalization;

    @Autowired
    private EntityManager entityManager;

    private final LoggerService logger = new LoggerService();


    @Override
    @Transactional(value = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public Void createCategory(String name, String color, String description, String groupId, String accountId) {
        var group = groupRepository.findGroupById(Long.parseLong(groupId));
        var account = accountRepository.findAccountById(Long.parseLong(accountId));


        if (account.isEmpty()) {
            logger.log("AccountNotFoundException occurred: Account with id: " + accountId + " has not been found.",
                    "CategoryCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountNotFoundException(internationalization.getMessage("account.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (group.isEmpty()) {
            logger.log("GroupNotFoundException occurred: Group with id: " + groupId + " has not been found.",
                    "CategoryCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new GroupNotFoundException(internationalization.getMessage("group.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        var accountGroups = account.get().getGroupRoles().stream()
                .filter(accountGroupRole -> accountGroupRole.getGroup().getId().equals(group.get().getId())).toList();

        if (accountGroups.isEmpty()) {
            logger.log("AccountNotBelongToGroupWhichIsAttachedToGivenCategoryException occurred: Account with name: " + accountId + " has not been belonged to required group, which has requested category.",
                    "CategoryCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountNotBelongToGroupWhichIsAttachedToGivenCategoryException(internationalization.getMessage("account.notBelongToGroupWhichIsAttachedToGivenCategory", LocaleContextHolder.getLocale().getLanguage()));
        }

        var categories = StreamSupport.stream(group.get().getCategories().spliterator(), false);
        if(categories
                .map(Category::getName)
                .map(String::toUpperCase)
                .anyMatch(groupName -> groupName.equals(name.toUpperCase()))) {
            logger.log("CategoryNameAlreadyException occurred: Category with that name already exists.",
                    "CategoryCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new CategoryNameAlreadyException(internationalization.getMessage("category.nameExists", LocaleContextHolder.getLocale().getLanguage()));
        }

        Category category = new Category(name, color, description, false, group.get());
        repository.save(category);

        logger.log("Category with name: " + category.getName() + ",  color:"+ category.getColor() + ", and description "+ category.getDescription() + " has been created successfully.",
                "CategoryCommandService", LoggerService.LoggerServiceLevel.INFO);
        return null;
    }

    @Override
    @Transactional(value = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public Void deleteCategory(String id, String accountId) {
        var category = repository.findCategoryById(Long.parseLong(id));
        var account = accountRepository.findAccountById(Long.parseLong(accountId));

        if (category.isEmpty()) {
            logger.log("CategoryNotFoundException occurred: Category with id: " + id + " has not been found.",
                    "CategoryCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new CategoryNotFoundException(internationalization.getMessage("category.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        var getCategory = category.get();

        if (getCategory.isDefault()) {
            logger.log("CategoryCannotChangeDefaultCategoryName occurred: Category with id: " + id + " cannot be deleted as it is default category.",
                    "CategoryCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new CategoryCannotChangeDefaultCategoryName(internationalization.getMessage("category.cannotChangeDefaultCategoryName", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (account.isEmpty()) {
            logger.log("AccountNotFoundException occurred: Account with id: " + accountId + " has not been found.",
                    "CategoryCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountNotFoundException(internationalization.getMessage("account.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        var group = getCategory.getGroup();

        var accountGroups = account.get().getGroupRoles().stream()
                .filter(accountGroupRole -> accountGroupRole.getGroup().getId().equals(group.getId())).toList();

        if (accountGroups.isEmpty()) {
            logger.log("AccountNotBelongToGroupWhichIsAttachedToGivenCategoryException occurred: Account with name: " + accountId + " has not been belonged to required group, which has requested category.",
                    "CategoryCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountNotBelongToGroupWhichIsAttachedToGivenCategoryException(internationalization.getMessage("account.notBelongToGroupWhichIsAttachedToGivenCategory", LocaleContextHolder.getLocale().getLanguage()));
        }

        var defaultCategory = repository.findCategoryByGroupIdAndDefault(group.getId(), true);

        if (defaultCategory.isEmpty()) {
            throw  new CategoryNotFoundException(internationalization.getMessage("category.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        var getDefaultCategory = defaultCategory.get();

        getCategory.getTransactions().forEach(transaction ->
        {
            transaction.setCategory(getDefaultCategory);
            getDefaultCategory.getTransactions().add(transaction);
            transactionRepository.save(transaction);
            repository.save(getDefaultCategory);
        });

        repository.save(getDefaultCategory);
        getCategory.getTransactions().clear();
        repository.save(getCategory);

        repository.deleteById(getCategory.getId());

        logger.log("Category with id: " + id + " has been deleted successfully.",
                "CategoryCommandService", LoggerService.LoggerServiceLevel.INFO);
        return null;
    }

    @Override
    @Retryable()
    @Transactional(value = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public Void changeCategory(String id, String name, String color, String description, String accountId,
                               String version, String ifMatchHeader) {
        var category = repository.findCategoryById(Long.parseLong(id));
        var account = accountRepository.findAccountById(Long.parseLong(accountId));


        if (account.isEmpty()) {
            logger.log("AccountNotFoundException occurred: Account with id: " + accountId + " has not been found.",
                    "CategoryCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountNotFoundException(internationalization.getMessage("account.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }
        if (category.isEmpty()) {
            logger.log("CategoryNotFoundException occurred: Category with id: " + id + " has not been found.",
                    "CategoryCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new CategoryNotFoundException(internationalization.getMessage("category.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (ifMatchHeader == null  || ifMatchHeader.isEmpty() || !ifMatchHeader.equals(messageSigner.sign(category.get()))) {
            logger.log("ETagVerificationFailedException occurred for account id: " + id,
                    "CategoryCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new ETagVerificationFailedException(internationalization.getMessage("utils.etagVerificationFailed", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (!Objects.equals(version, category.get().getVersion().toString())) {
            logger.log("OptimisticLockException occurred for account id: " + id,
                    "CategoryCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new OptimisticLockException(internationalization.getMessage("utils.optimisticLock", LocaleContextHolder.getLocale().getLanguage()));
        }

        var group = category.get().getGroup();

        var accountGroups = account.get().getGroupRoles().stream()
                .filter(accountGroupRole -> accountGroupRole.getGroup().getId().equals(group.getId())).toList();

        if (accountGroups.isEmpty()) {
            logger.log("AccountNotBelongToGroupWhichIsAttachedToGivenCategoryException occurred: Account with name: " + accountId + " has not been belonged to required group, which has requested category.",
                    "CategoryCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountNotBelongToGroupWhichIsAttachedToGivenCategoryException(internationalization.getMessage("account.notBelongToGroupWhichIsAttachedToGivenCategory", LocaleContextHolder.getLocale().getLanguage()));
        }

        Category changedCategory = category.get();
        if(Objects.equals(changedCategory.getName(), "Default") && !changedCategory.getName().equals(name)) {
            throw new CategoryCannotChangeDefaultCategoryName(internationalization.getMessage("category.cannotChangeDefaultCategoryName", LocaleContextHolder.getLocale().getLanguage()));
        } else {
            changedCategory.setName(name);
        }
        changedCategory.setColor(color);
        changedCategory.setDescription(description);

        repository.save(changedCategory);
        logger.log("Category with id: " + id + " has been changed successfully.",
                "CategoryCommandService", LoggerService.LoggerServiceLevel.INFO);
        return null;
    }
}
