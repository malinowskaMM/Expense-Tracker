package pl.lodz.p.it.expenseTracker.dto.category.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCreateRequestDto {
    @Size(min = 3, message = "Name must have at least 3 characters")
    private String name;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Color must be in hex format starting with #")
    private String color;

    @Size(min = 3, max = 256, message = "Description must have at most 256 characters")
    private String description;

    @NotNull(message = "GroupId cannot be null")
    private String groupId;

    @NotNull(message = "OwnerId cannot be null")
    private String accountId;
}
