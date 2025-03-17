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
public class AccountPasswordChangeRequestDto {

    @NotNull
    private String lastPassword;

    @NotNull
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).{9,}$", message = "New password must have at least one uppercase letter, one digit, and be at least 9 characters long")
    private String newPassword;

    @NotNull
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).{9,}$", message = "New password must have at least one uppercase letter, one digit, and be at least 9 characters long")
    private String repeatedNewPassword;

    @NotNull
    private String version;
}
