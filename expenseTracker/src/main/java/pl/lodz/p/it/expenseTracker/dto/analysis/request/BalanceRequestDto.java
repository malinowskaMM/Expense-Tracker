package pl.lodz.p.it.expenseTracker.dto.analysis.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BalanceRequestDto {

    @NotNull(message = "Id of group cannot be null")
    private String groupId;

    private String startDate;

    private String endDate;
}
