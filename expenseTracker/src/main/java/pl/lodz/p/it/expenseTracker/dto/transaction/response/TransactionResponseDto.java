package pl.lodz.p.it.expenseTracker.dto.transaction.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class TransactionResponseDto {

    private String id;

    private String name;

    private String categoryName;

    private String categoryColor;

    private String categoryId;

    private String groupName;

    private Boolean isCyclic;

    private Integer period;

    private String periodUnit;

    private LocalDate date;

    private LocalDate endDate;

    private BigDecimal amount;

    private String accountId;

    private String type;

    private String sign;

    private String version;
}