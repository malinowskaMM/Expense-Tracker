package pl.lodz.p.it.expenseTracker.service;

import pl.lodz.p.it.expenseTracker.dto.analysis.response.*;


public interface AnalysisQueryService {
    ExpensesPerCategoryListResponseDto getExpensesPerCategory(String groupId, String startDate, String endDate);

    BalanceResponseDto getBalance(String groupId, String startDate, String endDate);

    ExpensesVsIncomesResponseDto getExpensesVsIncomesBalance(String groupId, String startDate, String endDate);

    ExpensesUnitsListResponseDto getExpenses(String groupId, String startDate, String endDate);

    ExpensesPerTypeListResponseDto getExpensesPerType(String groupId, String startDate, String endDate);

    TransactionsUnitsListResponseDto getTransactions(String groupId, String startDate, String endDate);
}
