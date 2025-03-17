package pl.lodz.p.it.expenseTracker.dto.analysis.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpensesPerCategoryRequestDto {

    @NotNull(message = "Id of group cannot be null")
    private String groupId;

    private String startDate;

    private String endDate;
}
