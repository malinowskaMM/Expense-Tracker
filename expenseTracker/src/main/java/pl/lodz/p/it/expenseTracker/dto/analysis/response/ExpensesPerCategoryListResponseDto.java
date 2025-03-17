package pl.lodz.p.it.expenseTracker.dto.analysis.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import pl.lodz.p.it.expenseTracker.dto.transaction.response.TransactionListResponseDto;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ExpensesPerCategoryListResponseDto {

    private List<ExpensesPerCategoryResponseDto> expensesPerCategoryWithPercentage;

    private TransactionListResponseDto transactions;

    private BigDecimal balance;

    private String startDate;

    private String endDate;
}
