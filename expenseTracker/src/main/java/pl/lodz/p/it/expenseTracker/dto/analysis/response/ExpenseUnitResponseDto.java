package pl.lodz.p.it.expenseTracker.dto.analysis.response;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ExpenseUnitResponseDto {
    private String name;

    private String categoryName;

    private String categoryId;

    private String groupName;

    private String groupId;

    private LocalDate date;

    private String transactionType;

    private Boolean isCyclic;

    private Integer period;

    private String periodUnit;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal amount;
}