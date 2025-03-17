package pl.lodz.p.it.expenseTracker.dto.group.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupChangeHeadRequestDto {

    @NotNull(message = "OwnerId cannot be null")
    private List<String> newOwnerIds;

    private String sign;

    private String version;
}