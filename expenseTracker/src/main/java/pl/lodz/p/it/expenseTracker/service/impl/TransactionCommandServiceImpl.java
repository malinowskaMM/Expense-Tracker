package pl.lodz.p.it.expenseTracker.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.expenseTracker.domain.entity.*;
import pl.lodz.p.it.expenseTracker.domain.enums.PeriodUnitEnum;
import pl.lodz.p.it.expenseTracker.dto.transaction.request.TransactionCyclicEnumDto;
import pl.lodz.p.it.expenseTracker.dto.transaction.request.TransactionPeriodTypeDto;
import pl.lodz.p.it.expenseTracker.dto.transaction.request.TransactionTypeEnumDto;
import pl.lodz.p.it.expenseTracker.exceptions.account.AccountNotBelongToGroupException;
import pl.lodz.p.it.expenseTracker.exceptions.account.AccountNotFoundException;
import pl.lodz.p.it.expenseTracker.exceptions.category.CategoryNotFoundException;
import pl.lodz.p.it.expenseTracker.exceptions.transaction.*;
import pl.lodz.p.it.expenseTracker.exceptions.utils.ETagVerificationFailedException;
import pl.lodz.p.it.expenseTracker.exceptions.utils.OptimisticLockException;
import pl.lodz.p.it.expenseTracker.repository.administration.AccountRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.CategoryRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.GroupRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.TransactionRepository;
import pl.lodz.p.it.expenseTracker.service.TransactionCommandService;
import pl.lodz.p.it.expenseTracker.utils.Internationalization;
import pl.lodz.p.it.expenseTracker.utils.LoggerService;
import pl.lodz.p.it.expenseTracker.utils.etag.MessageSigner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TransactionCommandServiceImpl implements TransactionCommandService {

    private final TransactionRepository repository;
    private final GroupRepository groupRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final LoggerService logger = new LoggerService();

    private final MessageSigner messageSigner;
    private final Internationalization internationalization;

    @Override
    @Transactional(value = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public Void createTransaction(String name, TransactionCyclicEnumDto cycle, String categoryId,
                                  TransactionTypeEnumDto type, LocalDate date, BigDecimal value, Integer period,
                                  TransactionPeriodTypeDto periodType, String creatorId) {


        var account = accountRepository.findAccountById(Long.parseLong(creatorId));
        if (account.isEmpty()) {
            logger.log("AccountNotFoundException occurred: Account with id: " + creatorId + " has not been found.",
                    "TransactionCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountNotFoundException(internationalization.getMessage("account.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }
        var category = categoryRepository.findCategoryById(Long.parseLong(categoryId));
        if (category.isEmpty()) {
            logger.log("CategoryNotFoundException occurred: Category with id: " + categoryId + " has not been found.",
                    "TransactionCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new CategoryNotFoundException(internationalization.getMessage("category.notFound", account.get().getLanguage_()));
        }

        var group = category.get().getGroup();
        if (!group.getAccountGroupRoles().stream()
                .map(AccountGroupRole::getAccount)
                .map(Account::getId)
                .toList().contains(account.get().getId())) {
            logger.log("AccountNotBelongToGroupException occurred: Account with id: " + creatorId + " not belonged to group with id: " + group.getId() + ".",
                    "GroupCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountNotBelongToGroupException(internationalization.getMessage("account.notBelongToGroup", account.get().getLanguage_()));
        }

        if(("CYCLE".equals(cycle.name()) && period == null) || ("CYCLE".equals(cycle.name()) && periodType == null)) {
            logger.log("CycleTransactionHasNoCyclicAttributesException occurred: Cycle transaction has no cyclic attribute like period or period unit.",
                    "TransactionCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new CycleTransactionHasNoCyclicAttributesException(internationalization.getMessage("transaction.cycleHasNoCyclicAttributes", account.get().getLanguage_()));
        }

        switch(type.name()) {
            case "INCOME": {
                createIncome(name, cycle, category.get(), date, value, period, periodType, account.get());
                break;
            }
            case "EXPENSE": {
                createExpense(name, cycle, category.get(), date, value, period, periodType, account.get());
                break;
            }
        }
        return null;
    }

    private void createExpense(String name, TransactionCyclicEnumDto cycle, Category category,
                               LocalDate date, BigDecimal value, Integer period,
                               TransactionPeriodTypeDto periodType, Account creatorId) {



        Expense expense = new Expense(name, category, cycle.equals(TransactionCyclicEnumDto.CYCLE),
                period,
                periodType == null ? null : PeriodUnitEnum.valueOf(periodType.name()),
                date, value, "EXPENSE", creatorId);

        repository.save(expense);
    }

    private void createIncome(String name, TransactionCyclicEnumDto cycle, Category category,
                              LocalDate date, BigDecimal value, Integer period,
                              TransactionPeriodTypeDto periodType, Account creatorId) {

        Income income = new Income(name, category, cycle.equals(TransactionCyclicEnumDto.CYCLE),
                period,
                periodType == null ? null : PeriodUnitEnum.valueOf(periodType.name()),
                date, value, creatorId);

        repository.save(income);
    }

    @Retryable
    @Transactional(value = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Override
    public Void deleteTransaction(String id) {
        var transaction = repository.findById(Long.parseLong(id));
        if (transaction.isEmpty()) {
            logger.log("TransactionNotFoundException occurred: Transaction with id: " + id + " has not been found.",
                    "TransactionCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TransactionNotFoundException(internationalization.getMessage("transaction.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        repository.deleteById(Long.parseLong(id));
        return null;
    }

    @Retryable
    @Transactional(value = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Override
    public Void changeTransaction(String id, String name, TransactionCyclicEnumDto cycle, BigDecimal period,
                                  TransactionPeriodTypeDto periodType, TransactionTypeEnumDto type,
                                  String categoryId, LocalDate date, BigDecimal value, String version,
                                  String ifMatchHeader) {
        var transaction = repository.findById(Long.parseLong(id));
        if (transaction.isEmpty()) {
            logger.log("TransactionNotFoundException occurred: Transaction with id: " + id + " has not been found.",
                    "TransactionCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TransactionNotFoundException(internationalization.getMessage("transaction.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        var category = categoryRepository.findCategoryById(Long.parseLong(categoryId));
        if (category.isEmpty()) {
            logger.log("CategoryNotFoundException occurred: Category with id: " + categoryId + " has not been found.",
                    "TransactionCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new CategoryNotFoundException(internationalization.getMessage("category.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (ifMatchHeader == null  || ifMatchHeader.isEmpty() || !ifMatchHeader.equals(messageSigner.sign(transaction.get()))) {
            logger.log("ETagVerificationFailedException occurred for account id: " + id,
                    "TransactionCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new ETagVerificationFailedException(internationalization.getMessage("utils.etagVerificationFailed", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (!Objects.equals(version, transaction.get().getVersion().toString())) {
            logger.log("OptimisticLockException occurred for account id: " + id,
                    "TransactionCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new OptimisticLockException(internationalization.getMessage("utils.optimisticLock", LocaleContextHolder.getLocale().getLanguage()));
        }

        Transaction modifiedTransaction = transaction.get();

        if (!Objects.equals(name, transaction.get().getName())) {
            modifiedTransaction.setName(name);
        }
        if (!Objects.equals(category.get().getId(), transaction.get().getCategory().getId())) {
            modifiedTransaction.setCategory(category.get());
        }

        if (cycle.name().equals("CYCLE") != transaction.get().getIsCyclic()) {
            modifiedTransaction.setIsCyclic(cycle.name().equals("CYCLE"));
        }

        if (cycle.name().equals("CYCLE")) {
            if (transaction.get().getPeriod() != null && period.intValue() != transaction.get().getPeriod()) {
                modifiedTransaction.setPeriod(period.intValue());
            } else {
                modifiedTransaction.setPeriod(period.intValue());
            }
        }

        if (cycle.name().equals("CYCLE")) {
            if (PeriodUnitEnum.valueOf(periodType.name()) != transaction.get().getPeriodUnit()) {
                modifiedTransaction.setPeriodUnit(PeriodUnitEnum.valueOf(periodType.name()));
            }
        }

        if(!date.isEqual(transaction.get().getStartDate())) {
            modifiedTransaction.setStartDate(date);
        }

        if(value.compareTo(transaction.get().getAmount()) != 0) {
            modifiedTransaction.setAmount(value);
        }

        if (!Objects.equals(type.name(), transaction.get().getTransactionType())) {
            modifiedTransaction.setTransactionType(type.name());
        }

        repository.save(modifiedTransaction);
        return null;
    }

    @Retryable
    @Transactional(value = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Override
    public Void stopRecurringTransaction(String id) {
        var transaction = repository.findById(Long.parseLong(id));
        if (transaction.isEmpty()) {
            logger.log("TransactionNotFoundException occurred: Transaction with id: " + id + " has not been found.",
                    "TransactionCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TransactionNotFoundException(internationalization.getMessage("transaction.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        Transaction modifiedTransaction = transaction.get();

        if (!modifiedTransaction.getIsCyclic()) {
            logger.log("TransactionNotCyclicException occurred: Transaction with id: " + id + " has not been found as cyclic.",
                    "TransactionCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TransactionNotCyclicException(internationalization.getMessage("transaction.notCyclic", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (modifiedTransaction.getEndDate() != null) {
            throw new TransactionAlreadyStoppedFromRecurring(internationalization.getMessage("transaction.alreadyStoppedFromRecurring", LocaleContextHolder.getLocale().getLanguage()));
        }

        modifiedTransaction.setEndDate(LocalDate.now());
        repository.save(modifiedTransaction);
        return null;
    }

    @Retryable
    @Transactional(value = "trackingTransactionManager", propagation = Propagation.REQUIRES_NEW)
    @Override
    public Void renewRecurringTransaction(String id) {
        var transaction = repository.findById(Long.parseLong(id));
        if (transaction.isEmpty()) {
            logger.log("TransactionNotFoundException occurred: Transaction with id: " + id + " has not been found.",
                    "TransactionCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TransactionNotFoundException(internationalization.getMessage("transaction.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        Transaction modifiedTransaction = transaction.get();

        if (!modifiedTransaction.getIsCyclic()) {
            logger.log("TransactionNotCyclicException occurred: Transaction with id: " + id + " has not been found as cyclic.",
                    "TransactionCommandService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TransactionNotCyclicException(internationalization.getMessage("transaction.notCyclic", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (modifiedTransaction.getEndDate() == null) {
            throw new TransactionAlreadyRenewedFromRecurring(internationalization.getMessage("transaction.alreadyRenewedFromRecurring", LocaleContextHolder.getLocale().getLanguage()));
        }

        modifiedTransaction.setEndDate(null);
        repository.save(modifiedTransaction);
        return null;
    }
}
