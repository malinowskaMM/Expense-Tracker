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
public class ExpensesUnitsListResponseDto {

    private Map<LocalDate, List<ExpenseUnitResponseDto>> expensesPerDay;

    private String startDate;

    private String endDate;
}
