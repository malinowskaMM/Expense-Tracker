package pl.lodz.p.it.expenseTracker.dto.analysis.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class ExpensesVsIncomesResponseDto {

    private BigDecimal balance;

    private BigDecimal expensesValue;

    private BigDecimal incomesValue;

    private String startDate;

    private String endDate;
}