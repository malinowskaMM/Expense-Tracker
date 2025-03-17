package pl.lodz.p.it.expenseTracker.dto.analysis.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class ExpensesPerCategoryResponseDto {

    private String categoryName;

    private String categoryColor;

    private String categoryDescription;

    private String percentage;
}
