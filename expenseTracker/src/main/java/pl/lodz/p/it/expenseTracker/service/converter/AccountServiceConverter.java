package pl.lodz.p.it.expenseTracker.service.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.expenseTracker.domain.entity.Account;
import pl.lodz.p.it.expenseTracker.dto.account.response.AccountListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.account.response.AccountResponseDto;
import pl.lodz.p.it.expenseTracker.utils.etag.MessageSigner;

import java.util.List;

@RequiredArgsConstructor
@Component
public class AccountServiceConverter {

    private final MessageSigner messageSigner;

    public AccountResponseDto accountResponseDto(Account account) {
        return AccountResponseDto.builder()
                .id(account.getId())
                .email(account.getEmail())
                .language(account.getLanguage_())
                .isActive(account.isActive())
                .isEnable(account.isEnabled())
                .registerData(account.getRegisterDate())
                .lastInvalidLoginDate(account.getLoginEntity().getLastInvalidLoginDate())
                .lastValidLoginDate(account.getLoginEntity().getLastValidLoginDate())
                .role(account.getRole().name())
                .sign(messageSigner.sign(account))
                .version(account.getVersion())
                .build();
    }

    public AccountListResponseDto accountListResponseDto(List<Account> accounts) {
        return AccountListResponseDto.builder()
                .accounts(accounts.stream().map(this::accountResponseDto).toList())
                .build();
    }
}
