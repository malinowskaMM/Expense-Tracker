package pl.lodz.p.it.expenseTracker.domain.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import pl.lodz.p.it.expenseTracker.domain.enums.PeriodUnitEnum;
import pl.lodz.p.it.expenseTracker.utils.etag.Signable;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "transaction_type")
@Entity
@Table(name = "transaction")
public abstract class Transaction extends AbstractEntity implements Signable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @Column(name = "name")
    @Size(min = 3)
    private String name;

    @Setter
    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    @Column(name = "is_cyclic")
    @Setter
    @NotNull
    private Boolean isCyclic;

    @Column(name = "period")
    @Nullable
    @Setter
    private Integer period;

    @Enumerated(EnumType.STRING)
    @Setter
    @Column(name = "period_unit")
    private PeriodUnitEnum periodUnit;

    @Setter
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    @Setter
    private LocalDate endDate;

    @Column(name = "amount")
    @Setter
    @Min(0/100)
    private BigDecimal amount;

    @Column(name = "transaction_type", insertable = false, updatable = false)
    @Setter
    private String transactionType; // New field for the discriminator value

    @Setter
    @ManyToOne
    @JoinColumn(name = "account_id", updatable = false, nullable = false, referencedColumnName = "id")
    private Account account;

    @PrePersist
    @PreUpdate
    private void validateDates() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new PersistenceException("Start date must be before end date");
        }
    }

    public Transaction(String name, Category category, boolean isCyclic, int period, PeriodUnitEnum periodUnit, LocalDate startDate, BigDecimal amount, String transactionType, Account account) {
        this.name = name;
        this.category = category;
        this.isCyclic = isCyclic;
        this.period = period;
        this.periodUnit = periodUnit;
        this.startDate = startDate;
        this.amount = amount;
        this.transactionType = transactionType;
        this.account = account;
    }

    @Override
    public String messageToSign() {
        return id.toString()
                .concat(name)
                .concat(category.getId().toString())
                .concat(isCyclic ? "Y" : "N")
                .concat(startDate.toString())
                .concat(amount.toString())
                .concat(transactionType)
                .concat(account.getId().toString())
                .concat(getVersion().toString());
    }
}