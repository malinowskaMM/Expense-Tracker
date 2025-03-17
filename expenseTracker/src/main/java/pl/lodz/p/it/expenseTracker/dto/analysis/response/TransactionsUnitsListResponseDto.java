package pl.lodz.p.it.expenseTracker.dto.analysis.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class TransactionsUnitsListResponseDto {

    private Map<LocalDate, List<TransactionUnitResponseDto>> transactionsExpensesPerDay;
    private Map<LocalDate, List<TransactionUnitResponseDto>> transactionsIncomesPerDay;

    private String startDate;

    private String endDate;

    private List<String> datesWithBiggestExpenseRate;

    private List<TransactionUnitResponseDto> transactions;
}