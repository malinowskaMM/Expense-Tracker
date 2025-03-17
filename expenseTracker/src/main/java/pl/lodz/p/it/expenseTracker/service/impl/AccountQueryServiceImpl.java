package pl.lodz.p.it.expenseTracker.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.expenseTracker.domain.enums.AccountRoleEnum;
import pl.lodz.p.it.expenseTracker.dto.account.response.AccountListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.account.response.AccountResponseDto;
import pl.lodz.p.it.expenseTracker.exceptions.account.AccountNotFoundException;
import pl.lodz.p.it.expenseTracker.repository.administration.AccountRepository;
import pl.lodz.p.it.expenseTracker.service.AccountQueryService;
import pl.lodz.p.it.expenseTracker.service.converter.AccountServiceConverter;
import pl.lodz.p.it.expenseTracker.utils.Internationalization;
import pl.lodz.p.it.expenseTracker.utils.LoggerService;

import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class AccountQueryServiceImpl implements AccountQueryService {

    private final AccountRepository accountRepository;

    private final AccountServiceConverter converter;

    private final Internationalization internationalization;

    private final LoggerService logger = new LoggerService();

    @Override
    public AccountListResponseDto getAccounts() {
        logger.log("Query: getAccounts executed", "AccountQueryServiceImpl", LoggerService.LoggerServiceLevel.INFO);
        return converter.accountListResponseDto(StreamSupport.stream(accountRepository.findAll().spliterator(), false).toList());
    }

    @Override
    public AccountResponseDto getAccountById(String id) {
        logger.log("Query: getAccountById executed for id:" + id, "AccountQueryServiceImpl",LoggerService.LoggerServiceLevel.INFO);
        return converter.accountResponseDto(accountRepository.findAccountById(Long.parseLong(id))
                .orElseThrow(() -> new AccountNotFoundException(internationalization.getMessage("account.notFound", LocaleContextHolder.getLocale().getLanguage()))));
    }

    @Override
    public AccountResponseDto getAccountByEmail(String email) {
        logger.log("Query: getAccountByEmail executed for email:" + email,"AccountQueryServiceImpl", LoggerService.LoggerServiceLevel.INFO);
        return converter.accountResponseDto(accountRepository.findAccountByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException(internationalization.getMessage("account.notFound", LocaleContextHolder.getLocale().getLanguage()))));
    }

    @Override
    public AccountListResponseDto getUsersAccounts() {
        logger.log("Query: getUsersAccounts executed","AccountQueryServiceImpl", LoggerService.LoggerServiceLevel.INFO);
        return converter.accountListResponseDto(StreamSupport.stream(accountRepository.findAll().spliterator(), false).filter(
                account -> account.getRole().equals(AccountRoleEnum.USER)).toList());
    }
}
