package pl.lodz.p.it.expenseTracker.dto.account.request;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class AccountResetPasswordRequestDto {

    @NonNull
    String accountId;

    @NonNull
    private String callbackRoute;
}
