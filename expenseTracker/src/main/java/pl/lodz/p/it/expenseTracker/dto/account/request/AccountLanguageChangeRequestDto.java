package pl.lodz.p.it.expenseTracker.dto.account.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountLanguageChangeRequestDto {

    @Pattern(regexp = "^(plPL|enUS)$", message = "Language must be 'pl' or 'en'")
    @NotNull
    private String language;

    @NotNull
    private String version;
}