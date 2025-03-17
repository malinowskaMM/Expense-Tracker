package pl.lodz.p.it.expenseTracker.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.expenseTracker.domain.entity.Account;
import pl.lodz.p.it.expenseTracker.domain.entity.AccountPrincipal;
import pl.lodz.p.it.expenseTracker.domain.entity.Token;
import pl.lodz.p.it.expenseTracker.domain.enums.AccountRoleEnum;
import pl.lodz.p.it.expenseTracker.domain.enums.TokenTypeEnum;
import pl.lodz.p.it.expenseTracker.domain.events.AccountAdminRegisterEvent;
import pl.lodz.p.it.expenseTracker.domain.events.AccountForgotPasswordEvent;
import pl.lodz.p.it.expenseTracker.domain.events.AccountRegisterEvent;
import pl.lodz.p.it.expenseTracker.domain.events.AccountResetPasswordEvent;
import pl.lodz.p.it.expenseTracker.dto.account.request.AccountResetPasswordRequestDto;
import pl.lodz.p.it.expenseTracker.dto.account.request.TokenRequestDto;
import pl.lodz.p.it.expenseTracker.dto.authentication.request.*;
import pl.lodz.p.it.expenseTracker.dto.authentication.response.AccountAuthenticationResponseDto;
import pl.lodz.p.it.expenseTracker.exceptions.account.*;
import pl.lodz.p.it.expenseTracker.exceptions.token.TokenExpiredException;
import pl.lodz.p.it.expenseTracker.exceptions.token.TokenNotFoundException;
import pl.lodz.p.it.expenseTracker.exceptions.token.TokenNotValidException;
import pl.lodz.p.it.expenseTracker.exceptions.token.TokenRevokedException;
import pl.lodz.p.it.expenseTracker.repository.administration.AccountRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.TokenRepository;
import pl.lodz.p.it.expenseTracker.security.JwtAuthenticationService;
import pl.lodz.p.it.expenseTracker.service.AuthenticationService;
import pl.lodz.p.it.expenseTracker.utils.Internationalization;
import pl.lodz.p.it.expenseTracker.utils.LoggerService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements LogoutHandler, AuthenticationService {

    private final TokenRepository repository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder encoder;
    private final JwtAuthenticationService service;
    private final AuthenticationManager manager;
    private final ApplicationEventPublisher eventPublisher;
    private final JwtAuthenticationService authService;
    private final Internationalization internationalization;
    private final LoggerService logger = new LoggerService();

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authenticationHeader = request.getHeader("Authorization");

        if (authenticationHeader == null || !authenticationHeader.startsWith("Bearer ")) {
            return;
        }

        final String jwtToken = authenticationHeader.substring(7);

        var storedToken = repository.findTokenByToken(jwtToken).orElse(null);

        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            repository.save(storedToken);
            SecurityContextHolder.clearContext();
        }
    }

    @Override
    @Transactional(value = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public Void confirmAdminRegistration(AccountRegisterConfirmRequestDto request) {
        if (!request.getPassword().equals(request.getRepeatPassword())) {
            logger.log("AccountRegisterPasswordsNotMatchException occurred: Account register passwords do not match.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountRegisterPasswordsNotMatchException(internationalization.getMessage("account.registerPasswordsNotMatch", LocaleContextHolder.getLocale().getLanguage()));
        }

        var token = repository.findTokenByToken(request.getToken());

        if (token.isEmpty()) {
            logger.log("TokenNotFoundException occurred: Token: " + request.getToken() + " not found.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TokenNotFoundException(internationalization.getMessage("token.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (token.get().isExpired()) {
            logger.log("TokenExpiredException occurred: Token: " + request.getToken() + " expired.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TokenExpiredException(internationalization.getMessage("token.expired", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (token.get().isRevoked()) {
            logger.log("TokenRevokedException occurred: Token: " + request.getToken() + " revoked.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TokenRevokedException(internationalization.getMessage("token.revoked", LocaleContextHolder.getLocale().getLanguage()));
        }

        Account account = token.get().getAccount();
        var userDetails = new AccountPrincipal(account);
        if (!authService.isTokenValid(token.get().token, userDetails)) {
            logger.log("TokenNotValidException occurred: Token: " + token.get().getToken() + " not valid.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TokenNotValidException(internationalization.getMessage("token.notValid", LocaleContextHolder.getLocale().getLanguage()));
        }

        account.setRegisterDate(LocalDateTime.now());
        account.setActive(true);
        account.setEnabled(true);
        account.setPassword(encoder.encode(request.getPassword()));
        accountRepository.save(account);

        logger.log("Admin account with id: " + account.getId() + " has been successfully activated with registration date " + account.getRegisterDate() + ".",
                "AuthenticationService", LoggerService.LoggerServiceLevel.INFO);
        token.get().setRevoked(true);
        repository.save(token.get());
        return null;
    }

    @Override
    @Transactional(value = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public Void confirmResetPassword(AccountConfirmPasswordRequestDto request) {
        if (!Objects.equals(request.password(), request.repeatPassword())) {
            logger.log("AccountResetPasswordsNotMatchException occurred: Account reset passwords do not match.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountRegisterPasswordsNotMatchException(internationalization.getMessage("account.registerPasswordsNotMatch", LocaleContextHolder.getLocale().getLanguage()));
        }

        var token = repository.findTokenByToken(request.token());

        if (token.isEmpty()) {
            logger.log("TokenNotFoundException occurred: Token: " + request.token() + " not found.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TokenNotFoundException(internationalization.getMessage("token.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (token.get().isExpired()) {
            logger.log("TokenExpiredException occurred: Token: " + request.token() + " expired.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TokenExpiredException(internationalization.getMessage("token.expired", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (token.get().isRevoked()) {
            logger.log("TokenRevokedException occurred: Token: " + request.token() + " revoked.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TokenRevokedException(internationalization.getMessage("token.revoked", LocaleContextHolder.getLocale().getLanguage()));
        }

        Account account = token.get().getAccount();
        var userDetails = new AccountPrincipal(account);
        if (!authService.isTokenValid(token.get().token, userDetails)) {
            logger.log("TokenNotValidException occurred: Token: " + token.get().getToken() + " not valid.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TokenNotValidException(internationalization.getMessage("token.notValid", LocaleContextHolder.getLocale().getLanguage()));
        }

        account.setPassword(encoder.encode(request.password()));
        accountRepository.save(account);

        logger.log("Account password for id: " + account.getId() + " has been successfully changed in reset process.",
                "AuthenticationService", LoggerService.LoggerServiceLevel.INFO);
        token.get().setRevoked(true);
        repository.save(token.get());
        return null;
    }

    @Override
    @Transactional(value = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public Void confirmRegistration(TokenRequestDto request) {
        var token = repository.findTokenByToken(request.token());

        if (token.isEmpty()) {
            logger.log("TokenNotFoundException occurred: Token: " + request.token() + " not found.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TokenNotFoundException(internationalization.getMessage("token.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (token.get().isExpired()) {
            logger.log("TokenExpiredException occurred: Token: " + request.token() + " expired.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TokenExpiredException(internationalization.getMessage("token.expired", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (token.get().isRevoked()) {
            logger.log("TokenRevokedException occurred: Token: " + request.token() + " revoked.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TokenRevokedException(internationalization.getMessage("token.revoked", LocaleContextHolder.getLocale().getLanguage()));
        }

        Account account = accountRepository.findAccountById(token.get().getAccount().getId()).orElseThrow(() -> new AccountNotFoundException(internationalization.getMessage("account.notFound", LocaleContextHolder.getLocale().getLanguage())));
        var userDetails = new AccountPrincipal(account);
        if (!authService.isTokenValid(token.get().token, userDetails)) {
            logger.log("TokenNotValidException occurred: Token: " + token.get().getToken() + " not valid.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TokenNotValidException(internationalization.getMessage("token.notValid", LocaleContextHolder.getLocale().getLanguage()));
        }

        accountRepository.updateIsActiveById(token.get().getAccount().getId(), true);
        accountRepository.updateRegisterDateByLong(token.get().getAccount().getId(), LocalDateTime.now());


        logger.log("Account with id: " + account.getId() + " has been successfully activated with registration date " + account.getRegisterDate() + ".",
                "AuthenticationService", LoggerService.LoggerServiceLevel.INFO);
        token.get().setRevoked(true);
        repository.save(token.get());
        return null;
    }

    @Override
    @Transactional(value = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public AccountAuthenticationResponseDto registerAccount(AccountRegisterRequestDto request) {
        if (!request.getPassword().equals(request.getRepeatPassword())) {
            logger.log("AccountRegisterPasswordsNotMatchException occurred: Account register passwords do not match.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountRegisterPasswordsNotMatchException(internationalization.getMessage("account.registerPasswordsNotMatch", LocaleContextHolder.getLocale().getLanguage()));
        }

        Account account = new Account(false, false, LocalDateTime.now(), request.getLanguage_(),
                request.getEmail(), encoder.encode(request.getPassword()));

        if (accountRepository.findAccountByEmail(request.getEmail()).isPresent()) {
            logger.log("AccountAlreadyExistsException occurred: Account with email already exists",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountAlreadyExistsException(internationalization.getMessage("account.alreadyExists", LocaleContextHolder.getLocale().getLanguage()));
        }

        var savedAccount = accountRepository.save(account);
        var token = service.generateToken(new AccountPrincipal(account));

        saveAccountToken(savedAccount, token);

        eventPublisher.publishEvent(new AccountRegisterEvent(account, token, request.getCallbackRoute()));

        logger.log("Account with id: " + account.getId() + " has been successfully registered.",
                "AuthenticationService", LoggerService.LoggerServiceLevel.INFO);
        return AccountAuthenticationResponseDto.builder().authenticationToken(token)
                .language(savedAccount.getLanguage_()).build();
    }

    @Override
    @Transactional(value = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public Void confirmForgotPassword(AccountConfirmPasswordRequestDto request) {
        if (!Objects.equals(request.password(), request.repeatPassword())) {
            logger.log("AccountForgotPasswordsNotMatchException occurred: Account forgot passwords do not match.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountRegisterPasswordsNotMatchException(internationalization.getMessage("account.registerPasswordsNotMatch", LocaleContextHolder.getLocale().getLanguage()));
        }

        var token = repository.findTokenByToken(request.token());

        if (token.isEmpty()) {
            logger.log("TokenNotFoundException occurred: Token: " + request.token() + " not found.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TokenNotFoundException(internationalization.getMessage("token.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (token.get().isExpired()) {
            logger.log("TokenExpiredException occurred: Token: " + request.token() + " expired.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TokenExpiredException(internationalization.getMessage("token.expired", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (token.get().isRevoked()) {
            logger.log("TokenRevokedException occurred: Token: " + request.token() + " revoked.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TokenRevokedException(internationalization.getMessage("token.revoked", LocaleContextHolder.getLocale().getLanguage()));
        }

        Account account = token.get().getAccount();
        var userDetails = new AccountPrincipal(account);
        if (!authService.isTokenValid(token.get().token, userDetails)) {
            logger.log("TokenNotValidException occurred: Token: " + token.get().getToken() + " not valid.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TokenNotValidException(internationalization.getMessage("token.notValid", LocaleContextHolder.getLocale().getLanguage()));
        }

        account.setPassword(encoder.encode(request.password()));
        accountRepository.save(account);

        logger.log("Account password for id: " + account.getId() + " has been successfully changed in forgot password process.",
                "AuthenticationService", LoggerService.LoggerServiceLevel.INFO);

        token.get().setRevoked(true);
        repository.save(token.get());
        return null;
    }

    @Override
    @Transactional(value = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public AccountAuthenticationResponseDto registerAdminAccount(AccountAdminRegisterRequestDto request) {
        if (!request.getEmail().equals(request.getRepeatEmail())) {
            logger.log("AdminAccountRegisterEmailsNotMatchException occurred: Admin account register emails do not match.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AdminAccountRegisterEmailsNotMatchException(internationalization.getMessage("adminAccount.registerEmailsNotMatch", LocaleContextHolder.getLocale().getLanguage()));
        }

        Account account = new Account(false, false, LocalDateTime.now(), request.getLanguage_(),
                request.getEmail(), encoder.encode(UUID.randomUUID().toString()), AccountRoleEnum.ADMIN);

        var savedAccount = accountRepository.save(account);
        var token = service.generateToken(new AccountPrincipal(account));

        saveAccountToken(savedAccount, token);

        eventPublisher.publishEvent(new AccountAdminRegisterEvent(account, token, request.getCallbackRoute()));

        logger.log("Admin account with id: " + account.getId() + " has been successfully registered.",
                "AuthenticationService", LoggerService.LoggerServiceLevel.INFO);
        return AccountAuthenticationResponseDto.builder().authenticationToken(token)
                .language(savedAccount.getLanguage_()).build();
    }

    @Override
    public AccountAuthenticationResponseDto refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        final String refreshToken = authorizationHeader.substring(7);

        final String userCredentialEmail = authService.extractUsername(refreshToken);

        if (userCredentialEmail != null) {
            var account = accountRepository.findAccountByEmail(userCredentialEmail).orElseThrow();
            var userDetails = new AccountPrincipal(account);

            if (authService.isTokenValid(refreshToken, userDetails)) {
                var accessToken = authService.generateToken(userDetails);
                revokeAllAccountsTokens(account);
                saveAccountToken(account, accessToken);
                return AccountAuthenticationResponseDto.builder()
                        .authenticationToken(accessToken)
                        .role(account.getRole().getRoleName())
                        .refreshToken(refreshToken)
                        .language(account.getLanguage_())
                        .build();
            }
        }

        return null;
    }

    @Override
    @Transactional(value = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public AccountAuthenticationResponseDto forgotPassword(AccountForgotPasswordRequestDto request) {
        var account = accountRepository.findAccountByEmail(request.getEmail()).orElseThrow(() -> new AccountNotFoundException(internationalization.getMessage("account.notFound", LocaleContextHolder.getLocale().getLanguage())));

        var token = service.generateToken(new AccountPrincipal(account));

        saveAccountToken(account, token);

        eventPublisher.publishEvent(new AccountForgotPasswordEvent(account, token, request.getCallbackRoute()));

        logger.log("Account with id: " + account.getId() + " has entered forgot password process.",
                "AuthenticationService", LoggerService.LoggerServiceLevel.INFO);

        return AccountAuthenticationResponseDto.builder().authenticationToken(token)
                .language(account.getLanguage_()).build();
    }

    @Override
    @Transactional(value = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public AccountAuthenticationResponseDto resetPassword(AccountResetPasswordRequestDto request) {
        var account = accountRepository.findAccountById(Long.parseLong(request.getAccountId())).orElseThrow(() -> new AccountNotFoundException(internationalization.getMessage("account.notFound", LocaleContextHolder.getLocale().getLanguage())));

        var token = service.generateToken(new AccountPrincipal(account));

        saveAccountToken(account, token);

        eventPublisher.publishEvent(new AccountResetPasswordEvent(account, token, request.getCallbackRoute()));

        logger.log("Account with id: " + account.getId() + " has entered reset password process.",
                "AuthenticationService", LoggerService.LoggerServiceLevel.INFO);

        return AccountAuthenticationResponseDto.builder().authenticationToken(token)
                .language(account.getLanguage_()).build();
    }

    @Override
    public AccountAuthenticationResponseDto authenticate(AccountAuthenticationRequestDto request) {

        manager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        var account = accountRepository.findAccountByEmail(request.getEmail()).orElseThrow(() -> new AccountNotFoundException(internationalization.getMessage("account.notFound", LocaleContextHolder.getLocale().getLanguage())));

        if (!account.isActive()) {
            logger.log("AccountNotActiveException occurred: Account with id: " + account.getId() + "  is not active.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            account.getLoginEntity().setLastInvalidLoginDate(LocalDateTime.now());
            account.getLoginEntity().setInvalidLoginCounter(account.getLoginEntity().getInvalidLoginCounter() + 1);
            accountRepository.save(account);
            throw new AccountNotActiveException(internationalization.getMessage("account.notActive", LocaleContextHolder.getLocale().getLanguage()));
        }
        if (account.isArchived()) {
            logger.log("AccountArchivedException occurred: Account with id: " + account.getId() + "  is archived.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            account.getLoginEntity().setLastInvalidLoginDate(LocalDateTime.now());
            account.getLoginEntity().setInvalidLoginCounter(account.getLoginEntity().getInvalidLoginCounter() + 1);
            accountRepository.save(account);
            throw new AccountArchivedException(internationalization.getMessage("account.archived", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (!account.isEnabled()) {
            logger.log("AccountNotEnabledException occurred: Account with id: " + account.getId() + "  is not enabled.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            account.getLoginEntity().setLastInvalidLoginDate(LocalDateTime.now());
            account.getLoginEntity().setInvalidLoginCounter(account.getLoginEntity().getInvalidLoginCounter() + 1);
            accountRepository.save(account);
            throw new AccountNotEnabledException(internationalization.getMessage("account.notEnabled", LocaleContextHolder.getLocale().getLanguage()));
        }

        var token = service.generateToken(new AccountPrincipal(account));
        var refreshToken = service.generateRefreshToken(new AccountPrincipal(account));

        revokeAllAccountsTokens(account);
        saveAccountToken(account, token);

        account.getLoginEntity().setLastValidLoginDate(LocalDateTime.now());
        accountRepository.save(account);

        logger.log("Account with id: " + account.getId() + " logged in.",
                "AuthenticationService", LoggerService.LoggerServiceLevel.INFO);

        return AccountAuthenticationResponseDto.builder().authenticationToken(token)
                .refreshToken(refreshToken)
                .language(account.getLanguage_())
                .role(account.getRole().getRoleName())
                .id(String.valueOf(account.getId())).build();
    }

    private void revokeAllAccountsTokens(Account account) {
        List<Token> validAccountsTokens = repository.findAllValidTokenByAccountId(account.getId());

        if (validAccountsTokens.isEmpty()) {
            return;
        }

        validAccountsTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });

        repository.saveAll(validAccountsTokens);
    }


    private void saveAccountToken(Account account, String token) {
        Token tokenEntity = new Token(token, TokenTypeEnum.BEARER, false, false, account);
        repository.save(tokenEntity);
    }
}