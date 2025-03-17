package pl.lodz.p.it.expenseTracker.dto.transaction.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCreateRequestDto {

    @Size(min = 3, message = "Name must have at least 3 characters")
    private String name;

    private String cycle;

    private Integer period;

    private String periodType;

    private String type;

    @NotNull(message = "CategoryId cannot be null")
    private String categoryId;

    private String date;

    @DecimalMin(value = "0", inclusive = false, message = "Value must be greater than 0")
    private BigDecimal value;

    private String creatorId;
}
