package pl.lodz.p.it.expenseTracker.service;

import pl.lodz.p.it.expenseTracker.dto.account.response.AccountListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.account.response.AccountResponseDto;


public interface AccountQueryService {

    AccountListResponseDto getAccounts();

    AccountResponseDto getAccountById(String id);

    AccountResponseDto getAccountByEmail(String email);

    AccountListResponseDto getUsersAccounts();
}
