package pl.lodz.p.it.expenseTracker.dto.group.request;

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
public class GroupCreateRequestDto {

    @Size(min = 3, message = "Name must have at least 3 characters")
    private String name;

    @NotNull(message = "OwnerId cannot be null")
    private List<String> accountsIds;

    private String ownerId;
}
