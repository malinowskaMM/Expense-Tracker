package pl.lodz.p.it.expenseTracker.dto.authentication.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AccountForgotPasswordRequestDto {

    @Email
    private String email;

    private String callbackRoute;
}