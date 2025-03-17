package pl.lodz.p.it.expenseTracker.dto.analysis.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ExpensesPerTypeListResponseDto {

    private BigDecimal balance;

    private String startDate;

    private String endDate;

    private List<ExpenseUnitResponseDto> oneTimeTransactions;

    private List<ExpenseUnitResponseDto> cyclicTransactions;
}
