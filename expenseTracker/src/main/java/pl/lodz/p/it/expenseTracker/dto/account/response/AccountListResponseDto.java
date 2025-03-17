package pl.lodz.p.it.expenseTracker.dto.account.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountListResponseDto {

    private List<AccountResponseDto> accounts;
}