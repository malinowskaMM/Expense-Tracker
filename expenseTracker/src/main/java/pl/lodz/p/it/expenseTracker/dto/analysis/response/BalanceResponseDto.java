package pl.lodz.p.it.expenseTracker.dto.analysis.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class BalanceResponseDto {

    private BigDecimal balance;

    private String startDate;

    private String endDate;
}
