package pl.lodz.p.it.expenseTracker.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.expenseTracker.domain.entity.Category;
import pl.lodz.p.it.expenseTracker.domain.entity.TransactionUnit;
import pl.lodz.p.it.expenseTracker.domain.entity.Group;
import pl.lodz.p.it.expenseTracker.domain.entity.Transaction;
import pl.lodz.p.it.expenseTracker.domain.enums.PeriodUnitEnum;
import pl.lodz.p.it.expenseTracker.dto.analysis.response.*;
import pl.lodz.p.it.expenseTracker.dto.transaction.response.TransactionListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.transaction.response.TransactionResponseDto;
import pl.lodz.p.it.expenseTracker.exceptions.account.AnalysisCannotBeProcessed;
import pl.lodz.p.it.expenseTracker.repository.tracking.CategoryRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.GroupRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.TransactionRepository;
import pl.lodz.p.it.expenseTracker.service.AnalysisQueryService;
import pl.lodz.p.it.expenseTracker.service.converter.TransactionServiceConverter;
import pl.lodz.p.it.expenseTracker.utils.Internationalization;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class AnalysisQueryServiceImpl implements AnalysisQueryService {

    public static final String EXPENSE = "EXPENSE";
    public static final String INCOME = "INCOME";
    private final TransactionRepository transactionRepository;
    private final GroupRepository groupRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionServiceConverter transactionServiceConverter;
    private final Internationalization internationalization;

    @Override
    public ExpensesPerCategoryListResponseDto getExpensesPerCategory(String groupId, String startDate, String endDate) {
        BigDecimal balance = calculateOnlyExpenses(getLocalDateFromString(startDate), getLocalDateFromString(endDate),
                Long.parseLong(groupId));

        if(balance.compareTo(BigDecimal.ZERO) == 0) {
            throw new AnalysisCannotBeProcessed(internationalization.getMessage("analysis.cannotBeProcessed", LocaleContextHolder.getLocale().getLanguage()));
        }

        var categoriesIdsInGroup = getAllCategoriesIdsInGroup(Long.parseLong(groupId));

        Map<String, List<String>> percentageWithCategory = new HashMap<>();
        categoriesIdsInGroup.forEach(category ->
                percentageWithCategory.put(getCategoryNameByCategoryId(category.getId()), List.of(calculateOnlyExpensesInCategory(
                        getLocalDateFromString(startDate),
                        getLocalDateFromString(endDate),
                        Long.parseLong(groupId),
                        category.getId()).divide(balance, 2, RoundingMode.CEILING).toString(),
                        category.getName(),
                        category.getColor(),
                        category.getDescription())));

        List<ExpensesPerCategoryResponseDto> expensesPerCategoryWithPercentage = new ArrayList<>();
        percentageWithCategory.keySet().forEach(
                percentageWithCategoryItem ->
                    expensesPerCategoryWithPercentage.add(ExpensesPerCategoryResponseDto.builder()
                            .percentage(percentageWithCategory.get(percentageWithCategoryItem).get(0))
                            .categoryName(percentageWithCategory.get(percentageWithCategoryItem).get(1))
                            .categoryColor(percentageWithCategory.get(percentageWithCategoryItem).get(2))
                            .categoryDescription(percentageWithCategory.get(percentageWithCategoryItem).get(3))
                            .build())
        );

        return ExpensesPerCategoryListResponseDto.builder()
                .balance(balance)
                .startDate(startDate)
                .endDate(endDate)
                .transactions(toTransactionListResponseDto(getAllTransactionInPeriodInGroup(getLocalDateFromString(startDate),
                        getLocalDateFromString(endDate),
                        Long.parseLong(groupId))))
                .expensesPerCategoryWithPercentage(expensesPerCategoryWithPercentage)
                .build();
    }

    private TransactionListResponseDto toTransactionListResponseDto(List<TransactionUnit> units) {
        return TransactionListResponseDto.builder()
                .transactions(units.stream().map(this::toTransactionResponseDto).toList())
                .build();
    }

    private TransactionResponseDto toTransactionResponseDto(TransactionUnit unit) {
        return TransactionResponseDto.builder()
                .id("")
                .name(unit.getName())
                .categoryName(unit.getCategory().getName())
                .categoryColor(unit.getCategory().getColor())
                .categoryId(unit.getCategory().getId().toString())
                .groupName(unit.getCategory().getGroup().getName())
                .isCyclic(unit.getIsCyclic())
                .period(unit.getPeriod())
                .periodUnit(unit.getPeriodUnit() != null ? unit.getPeriodUnit().name() : null)
                .date(unit.getDate())
                .endDate(unit.getEndDate())
                .amount(unit.getAmount())
                .accountId("")
                .type(unit.getTransactionType())
                .sign("")
                .version("")
                .build();
    }

    @Override
    public BalanceResponseDto getBalance(String groupId, String startDate, String endDate) {
        var balance = calculateBalance(getLocalDateFromString(startDate), getLocalDateFromString(endDate),
                Long.parseLong(groupId));
        return BalanceResponseDto.builder()
                .balance(balance)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Override
    public ExpensesVsIncomesResponseDto getExpensesVsIncomesBalance(String groupId, String startDate, String endDate) {
        return ExpensesVsIncomesResponseDto.builder()
                .balance(calculateBalance(getLocalDateFromString(startDate),
                        getLocalDateFromString(endDate),
                        Long.parseLong(groupId)))
                .expensesValue(calculateOnlyExpenses(getLocalDateFromString(startDate),
                        getLocalDateFromString(endDate),
                        Long.parseLong(groupId)))
                .incomesValue(calculateOnlyIncomes(getLocalDateFromString(startDate),
                        getLocalDateFromString(endDate),
                        Long.parseLong(groupId)))
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Override
    public ExpensesUnitsListResponseDto getExpenses(String groupId, String startDate, String endDate) {
        var startLocalDate = getLocalDateFromString(startDate);
        var endLocalDate = getLocalDateFromString(endDate);
        List<TransactionUnit> transactionUnits = getAllTransactionInPeriodInGroup(startLocalDate,
                endLocalDate, Long.parseLong(groupId)).stream()
                .filter(transactionUnit -> transactionUnit.getTransactionType().equals(EXPENSE)).toList();
        return ExpensesUnitsListResponseDto.builder()
                .expensesPerDay(iterateExpensesOverDates(startLocalDate, endLocalDate, transactionUnits))
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    @Override
    public ExpensesPerTypeListResponseDto getExpensesPerType(String groupId, String startDate, String endDate) {
        var startLocalDate = getLocalDateFromString(startDate);
        var endLocalDate = getLocalDateFromString(endDate);
        List<TransactionUnit> transactionUnits = getAllTransactionInPeriodInGroup(startLocalDate,
                endLocalDate, Long.parseLong(groupId)).stream()
                .filter(transactionUnit -> transactionUnit.getTransactionType().equals(EXPENSE)).toList();

        var balance = calculateOnlyExpenses(startLocalDate, endLocalDate, Long.parseLong(groupId));

        return ExpensesPerTypeListResponseDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .balance(balance)
                .oneTimeTransactions(transactionUnits.stream().filter(transactionUnit -> !transactionUnit.getIsCyclic())
                        .map(this::toExpenseUnitResponseDto)
                        .toList())
                .cyclicTransactions(transactionUnits.stream().filter(TransactionUnit::getIsCyclic)
                        .map(this::toExpenseUnitResponseDto)
                        .toList())
                .build();
    }

    @Override
    public TransactionsUnitsListResponseDto getTransactions(String groupId, String startDate, String endDate) {
        var startLocalDate = getLocalDateFromString(startDate);
        var endLocalDate = getLocalDateFromString(endDate);
        var transactions = getAllTransactionInPeriodInGroup(startLocalDate, endLocalDate, Long.parseLong(groupId));

        List<TransactionUnit> transactionExpensesUnits = transactions.stream()
                .filter(transactionUnit -> transactionUnit.getTransactionType().equals(EXPENSE)).toList();
        List<TransactionUnit> transactionIncomesUnits = transactions.stream()
                .filter(transactionUnit -> transactionUnit.getTransactionType().equals(INCOME)).toList();

        var transactionExpensesUnitsPerDay = iterateTransactionsOverDates(startLocalDate, endLocalDate, transactionExpensesUnits);
        var transactionIncomesUnitsPerDay = iterateTransactionsOverDates(startLocalDate, endLocalDate, transactionIncomesUnits);

        List<TransactionUnit> transactionsList = new ArrayList<>(transactionExpensesUnits);
        transactionsList.addAll(transactionIncomesUnits);

        var datesWithBiggestExpenseRate = getDatesWithBiggestExpenseRate(transactionExpensesUnitsPerDay);
        var datesStringWithBiggestExpenseRate = datesWithBiggestExpenseRate.stream().map(date -> date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).toList();

        return TransactionsUnitsListResponseDto.builder()
                .transactionsExpensesPerDay(transactionExpensesUnitsPerDay)
                .transactionsIncomesPerDay(transactionIncomesUnitsPerDay)
                .startDate(startDate)
                .endDate(endDate)
                .datesWithBiggestExpenseRate(datesStringWithBiggestExpenseRate)
                .transactions(transactionsList.stream().map(this::toTransactionUnitResponseDto).toList())
                .build();
    }

    private List<LocalDate> getDatesWithBiggestExpenseRate(Map<LocalDate, List<TransactionUnitResponseDto>> expensesPerDay) {
        Map<LocalDate, BigDecimal> sumOfExpensesPerDay = new HashMap<>();
        expensesPerDay.keySet().forEach(keyDate -> sumOfExpensesPerDay.put(keyDate, getSumOfValuesInList(expensesPerDay.get(keyDate))));
        var maxValue = sumOfExpensesPerDay.values().stream().max(BigDecimal::compareTo);
        return sumOfExpensesPerDay.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(maxValue.get()) == 0)
                .map(Map.Entry::getKey)
                .toList();
    }

    private BigDecimal getSumOfValuesInList(List<TransactionUnitResponseDto> list) {
        BigDecimal value = BigDecimal.ZERO;
        for (TransactionUnitResponseDto element : list) {
            value = value.add(element.getAmount());
        }
        return value;
    }

    private Map<LocalDate, List<TransactionUnitResponseDto>> iterateTransactionsOverDates(LocalDate startDate, LocalDate endDate,
                                                                                  List<TransactionUnit> transactions) {
        Map<LocalDate, List<TransactionUnitResponseDto>> expensesPerDay = new HashMap<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            LocalDate finalCurrentDate = currentDate;
            expensesPerDay.put(currentDate, transactions.stream()
                    .filter(transaction -> transaction.getDate().isEqual(finalCurrentDate))
                    .map(this::toTransactionUnitResponseDto).toList());
            currentDate = currentDate.plusDays(1);
        }
        return expensesPerDay;
    }

    private Map<LocalDate, List<ExpenseUnitResponseDto>> iterateExpensesOverDates(LocalDate startDate, LocalDate endDate,
                                                                                  List<TransactionUnit> transactions) {
        Map<LocalDate, List<ExpenseUnitResponseDto>> expensesPerDay = new HashMap<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            LocalDate finalCurrentDate = currentDate;
            expensesPerDay.put(currentDate, transactions.stream()
                    .filter(transaction -> transaction.getDate().isEqual(finalCurrentDate))
                    .map(this::toExpenseUnitResponseDto).toList());
            currentDate = currentDate.plusDays(1);
        }
        return expensesPerDay;
    }

    private BigDecimal calculateBalance(LocalDate startDate, LocalDate endDate, Long groupId) {
        var transactions  = getAllTransactionInPeriodInGroup(startDate, endDate, groupId);

        BigDecimal balance = BigDecimal.valueOf(0.0);
        for (TransactionUnit transaction : transactions) {
            switch (transaction.getTransactionType()) {
                case INCOME -> balance = balance.add(transaction.getAmount());
                case EXPENSE -> balance = balance.subtract(transaction.getAmount());
            }
        }
        return balance;
    }

    private BigDecimal calculateOnlyIncomes(LocalDate startDate, LocalDate endDate, Long groupId) {
        var transactions  = getAllTransactionInPeriodInGroup(startDate, endDate, groupId);

        BigDecimal balance = BigDecimal.valueOf(0.0);
        for (TransactionUnit transaction : transactions) {
            switch (transaction.getTransactionType()) {
                case INCOME -> balance = balance.add(transaction.getAmount());
            }
        }
        return balance;
    }

    private BigDecimal calculateOnlyExpenses(LocalDate startDate, LocalDate endDate, Long groupId) {
        var transactions  = getAllTransactionInPeriodInGroup(startDate, endDate, groupId);

        BigDecimal balance = BigDecimal.valueOf(0.0);
        for (TransactionUnit transaction : transactions) {
            switch (transaction.getTransactionType()) {
                case EXPENSE -> balance = balance.add(transaction.getAmount());
            }
        }
        return balance;
    }

    private BigDecimal calculateOnlyExpensesInCategory(LocalDate startDate, LocalDate endDate, Long groupId,
                                                       Long categoryId) {
        var transactions  = getAllTransactionInPeriodInGroup(startDate, endDate, groupId).stream()
                .filter(transaction -> transaction.getCategory().getId().equals(categoryId)).toList();

        BigDecimal balance = BigDecimal.valueOf(0.0);
        for (TransactionUnit transaction : transactions) {
            switch (transaction.getTransactionType()) {
                case EXPENSE -> balance = balance.add(transaction.getAmount());
            }
        }
        return balance;
    }

    private List<TransactionUnit> getAllTransactionInPeriodInGroup(LocalDate startDate, LocalDate endDate, Long groupId) {
        var filteredOneTimeTransactions = StreamSupport.stream(transactionRepository.findAll().spliterator(), false)
                .filter(transaction -> !(transaction.getIsCyclic()))
                .filter(transaction -> isTransactionInPeriod(transaction, startDate, endDate))
                .filter(transaction -> transaction.getCategory().getGroup().getId().equals(groupId))
                .map(transaction -> toTransactionUnit(transaction, transaction.getStartDate()))
                .toList();

        var filteredCyclicTransactions = StreamSupport.stream(transactionRepository.findAll().spliterator(), false)
                .filter(Transaction::getIsCyclic)
                .filter(transaction -> transaction.getCategory().getGroup().getId().equals(groupId))
                .toList();


        List<TransactionUnit> calculatedCyclicTransactionsUnits = new ArrayList<>();
        filteredCyclicTransactions.forEach(transaction -> calculatedCyclicTransactionsUnits.addAll(
                calculateCyclicTransactions(transaction.getStartDate(),
                transaction.getPeriod(), transaction.getPeriodUnit().name(), startDate, endDate, transaction.getName(),
                transaction.getCategory(), transaction.getTransactionType(), transaction.getIsCyclic(),
                        transaction.getEndDate(), transaction.getAmount())));

        calculatedCyclicTransactionsUnits.addAll(filteredOneTimeTransactions);

        return calculatedCyclicTransactionsUnits;
    }

    private boolean isTransactionInPeriod(Transaction transaction, LocalDate startDate, LocalDate endDate) {
        LocalDate transactionDate = transaction.getStartDate();
        return !transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate);
    }

    private LocalDate getLocalDateFromString(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, formatter);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDate.atStartOfDay(), ZoneId.of("UTC"));
        Instant instant = zonedDateTime.toInstant();
        return instant.atZone(ZoneId.of("UTC")).toLocalDate();
    }

    private List<Category> getAllCategoriesIdsInGroup(Long id) {
        return groupRepository.findGroupById(id).map(Group::getCategories).orElse(List.of());
    }

    private String getCategoryNameByCategoryId(Long id) {
        return categoryRepository.findCategoryById(id).map(Category::getName).orElse(null);
    }

    private List<TransactionResponseDto> getTransactionInGroup(Long id) {
        return StreamSupport.stream(transactionRepository.findAll().spliterator(), false)
                .filter(transaction -> transaction.getCategory().getGroup().getId().equals(id))
                .map(transactionServiceConverter::toTransactionResponseDto).toList();
    }

    private List<TransactionResponseDto> getExpensesInGroup(Long id) {
        return getTransactionInGroup(id).stream().filter(transaction -> transaction.getType().equals(EXPENSE)).toList();
    }

    public List<TransactionUnit> calculateCyclicTransactions(LocalDate startDate, Integer period, String periodUnit,
                                                             LocalDate periodStartDate, LocalDate periodEndDate,
                                                             String name, Category category, String transactionType,
                                                             Boolean isCyclic, LocalDate endDate, BigDecimal amount) {
        switch(periodUnit) {
            case "DAY" -> {
                return calculateCyclicDayTransactions(startDate, period, periodStartDate, periodEndDate,
                        name, category, transactionType, isCyclic, endDate, amount);
            }
            case "MONTH" -> {
                return calculateCyclicMonthTransactions(startDate, period, periodStartDate, periodEndDate,
                        name, category, transactionType, isCyclic, endDate, amount);
            }
            case "YEAR" -> {
                return calculateCyclicYearTransactions(startDate, period, periodStartDate, periodEndDate,
                        name, category, transactionType, isCyclic, endDate, amount);
            }
        }
        return List.of();
    }

    public List<TransactionUnit> calculateCyclicYearTransactions(LocalDate startDate, Integer period,
                                                                 LocalDate periodStartDate, LocalDate periodEndDate,
                                                                 String name, Category category, String transactionType,
                                                                 Boolean isCyclic, LocalDate endDate, BigDecimal amount) {
        List<TransactionUnit> transactions = new ArrayList<>();
        var firstEntered = calculateFirstEnterInPeriod(startDate, period, PeriodUnitEnum.MONTH, periodStartDate,
                periodEndDate);
        if (firstEntered == null) {
            return transactions;
        }

        if(firstEntered.isEqual(periodEndDate)){
            transactions.add(new TransactionUnit(name, category, periodEndDate, transactionType, isCyclic,
                    period, PeriodUnitEnum.YEAR, firstEntered, null, amount));
            return transactions;
        }

        while((firstEntered.isAfter(periodStartDate) || firstEntered.isEqual(periodStartDate)) &&
                (firstEntered.isBefore(periodEndDate) || firstEntered.isEqual(periodEndDate))) {
            transactions.add(new TransactionUnit(name, category, firstEntered, transactionType, isCyclic,
                    period, PeriodUnitEnum.YEAR, startDate, null, amount));
            firstEntered = firstEntered.plusYears(period);
        }
        return transactions;
    }

    public List<TransactionUnit> calculateCyclicMonthTransactions(LocalDate startDate, Integer period,
                                                                  LocalDate periodStartDate, LocalDate periodEndDate,
                                                                  String name, Category category, String transactionType,
                                                                  Boolean isCyclic, LocalDate endDate, BigDecimal amount) {
        List<TransactionUnit> transactions = new ArrayList<>();
        var firstEntered = calculateFirstEnterInPeriod(startDate, period, PeriodUnitEnum.MONTH, periodStartDate,
                periodEndDate);
        if (firstEntered == null) {
            return transactions;
        }

        if(firstEntered.isEqual(periodEndDate)){
            transactions.add(new TransactionUnit(name, category, periodEndDate, transactionType, isCyclic,
                    period, PeriodUnitEnum.MONTH, firstEntered, null, amount));
            return transactions;
        }

        while((firstEntered.isAfter(periodStartDate) || firstEntered.isEqual(periodStartDate)) &&
                (firstEntered.isBefore(periodEndDate) || firstEntered.isEqual(periodEndDate))) {
            transactions.add(new TransactionUnit(name, category, firstEntered, transactionType, isCyclic,
                    period, PeriodUnitEnum.MONTH, startDate, null, amount));
            firstEntered = firstEntered.plusMonths(period);
        }
        return transactions;
    }

    public List<TransactionUnit> calculateCyclicDayTransactions(LocalDate startDate, Integer period,
                                                                LocalDate periodStartDate, LocalDate periodEndDate,
                                                                String name, Category category, String transactionType,
                                                                Boolean isCyclic, LocalDate endDate, BigDecimal amount) {
        List<TransactionUnit> transactions = new ArrayList<>();
        var firstEntered = calculateFirstEnterInPeriod(startDate, period, PeriodUnitEnum.DAY, periodStartDate,
                periodEndDate);

        if (firstEntered == null) {
            return transactions;
        }

        if(firstEntered.isEqual(periodEndDate)){
            transactions.add(new TransactionUnit(name, category, periodEndDate, transactionType, isCyclic,
                    period, PeriodUnitEnum.DAY, firstEntered, null, amount));
            return transactions;
        }

        while((firstEntered.isAfter(periodStartDate) || firstEntered.isEqual(periodStartDate)) &&
                (firstEntered.isBefore(periodEndDate) || firstEntered.isEqual(periodEndDate))) {
            transactions.add(new TransactionUnit(name, category, firstEntered, transactionType, isCyclic,
                    period, PeriodUnitEnum.DAY, startDate, null, amount));
            firstEntered = firstEntered.plusDays(period);
        }

        return transactions;
    }

    private LocalDate calculateFirstEnterInPeriod(LocalDate startDate, Integer period, PeriodUnitEnum periodUnit,
                                                  LocalDate periodStartDate, LocalDate periodEndDate) {
        LocalDate firstEnterDate = null;

        switch (periodUnit) {
            case DAY:
                firstEnterDate = calculateFirstEnterInDay(startDate, period, periodStartDate, periodEndDate);
                break;
            case MONTH:
                firstEnterDate = calculateFirstEnterInMonth(startDate, period, periodStartDate, periodEndDate);
                break;
            case YEAR:
                firstEnterDate = calculateFirstEnterInYear(startDate, period, periodStartDate, periodEndDate);
                break;
            default:
                throw new IllegalArgumentException("Unsupported period unit: " + periodUnit);
        }

        return firstEnterDate;
    }

    private LocalDate calculateFirstEnterInDay(LocalDate startDate, Integer period, LocalDate periodStartDate, LocalDate periodEndDate) {
        LocalDate resultDate = startDate;

        if(resultDate.isEqual(periodStartDate)) {
            return resultDate;
        }

        while (!resultDate.isAfter(periodEndDate)) {
            if (!resultDate.isBefore(periodStartDate)) {
                return resultDate;
            }
            resultDate = resultDate.plusDays(period);
        }

        return null; // No valid date found within the period
    }

    private LocalDate calculateFirstEnterInMonth(LocalDate startDate, Integer period, LocalDate periodStartDate, LocalDate periodEndDate) {
        LocalDate resultDate = startDate;

        while (!resultDate.isAfter(periodEndDate)) {
            if (!resultDate.isBefore(periodStartDate)) {
                return resultDate;
            }
            resultDate = resultDate.plusMonths(period);
        }

        return null; // No valid date found within the period
    }

    private LocalDate calculateFirstEnterInYear(LocalDate startDate, Integer period, LocalDate periodStartDate, LocalDate periodEndDate) {
        LocalDate resultDate = startDate;

        while (!resultDate.isAfter(periodEndDate)) {
            if (!resultDate.isBefore(periodStartDate)) {
                return resultDate;
            }
            resultDate = resultDate.plusYears(period);
        }

        return null; // No valid date found within the period
    }


    private TransactionUnit toTransactionUnit(Transaction transaction, LocalDate date) {
        return new TransactionUnit(transaction.getName(),
                transaction.getCategory(),
                date,
                transaction.getTransactionType(),
                transaction.getIsCyclic(),
                transaction.getPeriod(),
                transaction.getPeriodUnit(),
                transaction.getStartDate(),
                transaction.getEndDate(),
                transaction.getAmount());
    }

    private ExpenseUnitResponseDto toExpenseUnitResponseDto(TransactionUnit transactionUnit) {
        return new ExpenseUnitResponseDto(
                transactionUnit.getName(),
                transactionUnit.getCategory().getName(),
                transactionUnit.getCategory().getId().toString(),
                transactionUnit.getCategory().getGroup().getName(),
                transactionUnit.getCategory().getGroup().getId().toString(),
                transactionUnit.getDate(),
                transactionUnit.getTransactionType(),
                transactionUnit.getIsCyclic(),
                transactionUnit.getPeriod(),
                transactionUnit.getPeriodUnit() != null ? transactionUnit.getPeriodUnit().name() : null,
                transactionUnit.getStartDate(),
                transactionUnit.getEndDate(),
                transactionUnit.getAmount()
        );
    }

    private TransactionUnitResponseDto toTransactionUnitResponseDto(TransactionUnit transactionUnit) {
        return new TransactionUnitResponseDto(
                transactionUnit.getName(),
                transactionUnit.getCategory().getName(),
                transactionUnit.getCategory().getId().toString(),
                transactionUnit.getCategory().getGroup().getName(),
                transactionUnit.getCategory().getGroup().getId().toString(),
                transactionUnit.getDate(),
                transactionUnit.getTransactionType(),
                transactionUnit.getIsCyclic(),
                transactionUnit.getPeriod(),
                transactionUnit.getPeriodUnit() != null ? transactionUnit.getPeriodUnit().name() : null,
                transactionUnit.getStartDate(),
                transactionUnit.getEndDate(),
                transactionUnit.getAmount()
        );
    }
}
