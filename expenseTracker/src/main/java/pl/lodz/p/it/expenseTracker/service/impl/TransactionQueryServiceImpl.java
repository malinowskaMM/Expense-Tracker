package pl.lodz.p.it.expenseTracker.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.expenseTracker.domain.entity.Account;
import pl.lodz.p.it.expenseTracker.domain.entity.AccountGroupRole;
import pl.lodz.p.it.expenseTracker.domain.entity.Group;
import pl.lodz.p.it.expenseTracker.domain.entity.Transaction;
import pl.lodz.p.it.expenseTracker.domain.enums.PeriodUnitEnum;
import pl.lodz.p.it.expenseTracker.dto.transaction.response.TransactionListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.transaction.response.TransactionResponseDto;
import pl.lodz.p.it.expenseTracker.exceptions.account.AccountNotFoundException;
import pl.lodz.p.it.expenseTracker.exceptions.category.CategoryNotFoundException;
import pl.lodz.p.it.expenseTracker.exceptions.transaction.TransactionNotFoundException;
import pl.lodz.p.it.expenseTracker.repository.administration.AccountRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.CategoryRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.TransactionRepository;
import pl.lodz.p.it.expenseTracker.service.TransactionQueryService;
import pl.lodz.p.it.expenseTracker.service.converter.TransactionServiceConverter;
import pl.lodz.p.it.expenseTracker.utils.Internationalization;
import pl.lodz.p.it.expenseTracker.utils.LoggerService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionQueryServiceImpl implements TransactionQueryService {

    private final TransactionRepository repository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final TransactionServiceConverter converter;
    private final LoggerService logger = new LoggerService();

    private final Internationalization internationalization;

    @Override
    public TransactionListResponseDto getTransactionsInCategory(String categoryId) {
        var category = categoryRepository.findCategoryById(Long.valueOf(categoryId));

        if (category.isEmpty()) {
            logger.log("CategoryNotFoundException occurred: Category with id" + categoryId + " has not been found.",
                    "TransactionQueryService",
                    LoggerService.LoggerServiceLevel.ERROR);
            throw new CategoryNotFoundException(internationalization.getMessage("category.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }
        logger.log("Query: getTransactionsInCategory executed with category id: " + categoryId + ".",
                "TransactionQueryService", LoggerService.LoggerServiceLevel.INFO);
        return converter.toTransactionListResponseDto(repository.findTransactionByCategory(category.get()));
    }

    @Override
    public TransactionListResponseDto getTransactionsByAccountId(String accountId) {
        var account = accountRepository.findAccountById(Long.valueOf(accountId));

        if (account.isEmpty()) {
            logger.log("AccountNotFoundException occurred: Account with id: " + accountId + " has not been found.",
                    "TransactionQueryService",
                    LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountNotFoundException(internationalization.getMessage("account.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        logger.log("Query: getTransactionsByAccountId executed with account id: " + accountId + ".",
                "TransactionQueryService",LoggerService.LoggerServiceLevel.INFO);
        return converter.toTransactionListResponseDto(repository.findTransactionByAccount(account.get()));
    }

    @Override
    public TransactionResponseDto getTransactionById(String id) {
        try {
            return converter.toTransactionResponseDto(repository.findById(Long.parseLong(id)).orElseThrow(
                    () -> new TransactionNotFoundException(internationalization.getMessage("transaction.notFound", LocaleContextHolder.getLocale().getLanguage()))));
        } catch (TransactionNotFoundException e) {
            logger.log("TransactionNotFoundException occurred: Transaction with id: " + id + " has not been found.",
                    "TransactionQueryService",
                    LoggerService.LoggerServiceLevel.ERROR);
            throw e;
        }
    }

    @Override
    public TransactionListResponseDto getTransactionsByAccountIdAndDate(String accountId, LocalDate date) {
        var account = accountRepository.findAccountById(Long.valueOf(accountId));

        if (account.isEmpty()) {
            logger.log("AccountNotFoundException occurred: Account with id: " + accountId + " has not been found.",
                    "TransactionQueryService",
                    LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountNotFoundException(internationalization.getMessage("account.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        var accountCategories= account.get().getGroupRoles().stream()
                .map(AccountGroupRole::getGroup)
                .flatMap(group -> group.getCategories().stream())
                .toList();

        List<Transaction> transactionsByAccount = new ArrayList<>();

        accountCategories.forEach(category -> transactionsByAccount.addAll(category.getTransactions()));

        var filteredOneTimeTransactions = transactionsByAccount.stream().filter(transaction -> !(transaction.getIsCyclic()))
                .filter(transaction -> transaction.getStartDate().isEqual(date)).toList();

        List<Transaction> filteredTransactions = new ArrayList<>(filteredOneTimeTransactions);

        var filteredCyclicTransactions = transactionsByAccount.stream().filter(Transaction::getIsCyclic).toList();
        filteredTransactions.addAll(filteredCyclicTransactions.stream().filter(transaction ->
                        calculateCyclicTransactions(transaction.getStartDate(), transaction.getPeriod(),
                        transaction.getPeriodUnit(), date, transaction.getEndDate())).toList());

        var result = filteredTransactions.stream().filter(transaction ->
                isAccountInGroup(account.get(), transaction.getCategory().getGroup().getId())).toList();

        logger.log("Query: getTransactionsByAccountIdAndDate executed with account id: " + accountId + " and date: " + date,
                "TransactionQueryService",LoggerService.LoggerServiceLevel.INFO);
        return converter.toTransactionListResponseDto(result);
    }

    private boolean isAccountInGroup(Account account, Long groupId) {
        return account.getGroupRoles().stream()
                .map(AccountGroupRole::getGroup)
                .map(Group::getId).toList()
                .contains(groupId);
    }

    private boolean calculateCyclicTransactions(LocalDate startDate, Integer period, PeriodUnitEnum periodUnit,
                                             LocalDate currentDate, LocalDate endDate) {
        switch(periodUnit) {
            case DAY -> {
                return calculateCyclicDayTransactions(startDate, period, currentDate, endDate);
            }
            case MONTH -> {
                return calculateCyclicMonthTransactions(startDate, period, currentDate, endDate);
            }
            case YEAR -> {
                return calculateCyclicYearTransactions(startDate, period, currentDate, endDate);
                }
        }
        return false;
    }

    private boolean calculateCyclicYearTransactions(LocalDate startDate, Integer period, LocalDate currentDate,
                                                    LocalDate endDate) {
        var cyclicDate = startDate;
        while (cyclicDate.isBefore(currentDate) || cyclicDate.isEqual(currentDate)) {
            if (endDate != null && endDate.isEqual(currentDate)) return true;
            if (endDate != null && currentDate.isAfter(endDate)) return false;
            if (cyclicDate.isEqual(currentDate)) return true;
            cyclicDate = cyclicDate.plusYears(period);
        }
        return false;
    }

    private boolean calculateCyclicMonthTransactions(LocalDate startDate, Integer period, LocalDate currentDate,
                                                     LocalDate endDate) {
        var cyclicDate = startDate;
        while (cyclicDate.isBefore(currentDate) || cyclicDate.isEqual(currentDate)) {
            if (endDate != null && endDate.isEqual(currentDate)) return true;
            if (endDate != null && currentDate.isAfter(endDate)) return false;
            if (cyclicDate.isEqual(currentDate)) return true;
            cyclicDate = cyclicDate.plusMonths(period);
        }
        return false;
    }

    private boolean calculateCyclicDayTransactions(LocalDate startDate, Integer period,LocalDate currentDate,
                                                   LocalDate endDate) {
        var cyclicDate = startDate;
        while (cyclicDate.isBefore(currentDate) || cyclicDate.isEqual(currentDate)) {
            if (endDate != null && endDate.isEqual(currentDate)) return true;
            if (endDate != null && currentDate.isAfter(endDate)) return false;
            if (cyclicDate.isEqual(currentDate)) return true;
            cyclicDate = cyclicDate.plusDays(period);
        }
        return false;
    }
}