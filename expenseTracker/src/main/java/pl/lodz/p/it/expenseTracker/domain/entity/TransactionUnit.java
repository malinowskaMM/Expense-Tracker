package pl.lodz.p.it.expenseTracker.domain.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.expenseTracker.domain.enums.PeriodUnitEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TransactionUnit {

    private String name;

    private Category category;

    private LocalDate date;

    private String transactionType;

    private Boolean isCyclic;

    private Integer period;

    private PeriodUnitEnum periodUnit;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal amount;
}