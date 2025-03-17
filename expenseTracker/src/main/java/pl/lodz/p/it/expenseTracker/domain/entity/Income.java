package pl.lodz.p.it.expenseTracker.domain.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import pl.lodz.p.it.expenseTracker.domain.enums.PeriodUnitEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@Entity
@DiscriminatorValue("INCOME")
@Table(name = "income")
public class Income extends Transaction {

    public Income(String name, Category category, boolean isCyclic, Integer period, PeriodUnitEnum periodUnit, LocalDate date, BigDecimal amount, Account account) {
        super(name, category, isCyclic, period == null ? 0 : period, periodUnit, date, amount, "INCOME", account);
    }
}