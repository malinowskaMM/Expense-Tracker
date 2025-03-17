package pl.lodz.p.it.expenseTracker.dto.authentication.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountRegisterConfirmRequestDto {

    private String token;

    private String password;

    private String repeatPassword;
}