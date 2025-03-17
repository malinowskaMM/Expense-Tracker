package pl.lodz.p.it.expenseTracker.dto.authentication.request;

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
public class AccountRegisterRequestDto {

    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{1,10}$", message = "Invalid email format")
    @NotNull
    private String email;

    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).{9,}$", message = "New password must have at least one uppercase letter, one digit, and be at least 9 characters long")
    @NotNull
    private String password;

    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).{9,}$", message = "New password must have at least one uppercase letter, one digit, and be at least 9 characters long")
    @NotNull
    private String repeatPassword;

    @Pattern(regexp = "^(plPL|enUS)$", message = "Language must be 'pl' or 'en'")
    @NotNull
    private String language_;

    private String callbackRoute;
}