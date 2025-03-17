package pl.lodz.p.it.expenseTracker.service.impl;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import pl.lodz.p.it.expenseTracker.domain.entity.Account;
import pl.lodz.p.it.expenseTracker.domain.entity.AccountPrincipal;
import pl.lodz.p.it.expenseTracker.domain.entity.Token;
import pl.lodz.p.it.expenseTracker.domain.enums.TokenTypeEnum;
import pl.lodz.p.it.expenseTracker.domain.events.AccountChangeEmailEvent;
import pl.lodz.p.it.expenseTracker.domain.events.infomativeEvents.AccountDisableEmailEvent;
import pl.lodz.p.it.expenseTracker.domain.events.infomativeEvents.AccountEnableEmailEvent;
import pl.lodz.p.it.expenseTracker.dto.account.request.TokenEmailRequestDto;
import pl.lodz.p.it.expenseTracker.dto.account.response.TokenEmailResponseDto;
import pl.lodz.p.it.expenseTracker.exceptions.account.*;
import pl.lodz.p.it.expenseTracker.exceptions.token.TokenExpiredException;
import pl.lodz.p.it.expenseTracker.exceptions.token.TokenNotFoundException;
import pl.lodz.p.it.expenseTracker.exceptions.token.TokenRevokedException;
import pl.lodz.p.it.expenseTracker.exceptions.utils.ETagVerificationFailedException;
import pl.lodz.p.it.expenseTracker.exceptions.utils.OptimisticLockException;
import pl.lodz.p.it.expenseTracker.repository.administration.AccountRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.TokenRepository;
import pl.lodz.p.it.expenseTracker.security.JwtAuthenticationService;
import pl.lodz.p.it.expenseTracker.service.AccountCommandService;
import pl.lodz.p.it.expenseTracker.utils.Internationalization;
import pl.lodz.p.it.expenseTracker.utils.LoggerService;
import pl.lodz.p.it.expenseTracker.utils.etag.MessageSigner;

import java.util.Objects;

@Service
@EnableTransactionManagement
@RequiredArgsConstructor
public class  AccountCommandServiceImpl implements AccountCommandService {

    private final AccountRepository repository;

    private final PasswordEncoder encoder;

    private final LoggerService logger = new LoggerService();

    private final MessageSigner messageSigner;

    private final AuthenticationManager authenticationManager;

    private final Internationalization internationalization;
    private final TokenRepository tokenRepository;
    private final JwtAuthenticationService service;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Retryable
    @Transactional(value = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public TokenEmailResponseDto changeAccountEmail(String id, String email, String repeatedEmail, String version, String ifMatchHeader,
                                                    String callbackRoute) {
        Account account = getAccountIfExistsById(id);

        if (repository.findAccountByEmail(email).isPresent()) {
            throw new AccountAlreadyExistsException(internationalization.getMessage("account.alreadyExists", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (ifMatchHeader == null  || ifMatchHeader.isEmpty() || !ifMatchHeader.equals(messageSigner.sign(account))) {
            logger.log("ETagVerificationFailedException occurred for account id: " + id, "AccountCommandService",
                    LoggerService.LoggerServiceLevel.ERROR);
            throw new ETagVerificationFailedException(internationalization.getMessage("utils.etagVerificationFailed", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (!Objects.equals(version, account.getVersion().toString())) {
            logger.log("OptimisticLockException occurred for account id: " + id, "AccountCommandService",
                    LoggerService.LoggerServiceLevel.ERROR);
            throw new OptimisticLockException(internationalization.getMessage("utils.optimisticLock", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (Objects.equals(account.getEmail(), email)) {
            logger.log("AccountNewEmailMatchesLastEmailException occurred: Email for account id: " + id + " matches last used email:  " + email + ".",
                    "AccountCommandService",
                    LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountNewEmailMatchesLastEmailException(internationalization.getMessage("account.newEmailMatchesLastEmail", LocaleContextHolder.getLocale().getLanguage()));
        }

        var token = service.generateToken(new AccountPrincipal(account));
        saveAccountToken(account, token);

        eventPublisher.publishEvent(new AccountChangeEmailEvent(account, email, token, callbackRoute));

        logger.log("Account with id: " + account.getId() + " has been successfully entered change email process",
                "AccountCommandService", LoggerService.LoggerServiceLevel.INFO);

        return TokenEmailResponseDto.builder()
                .newEmail(email)
                .language(account.getLanguage_() )
                .token(token)
                .id(account.getId().toString())
                .build();
    }

    @Override
    @Retryable
    @Transactional(value = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public Void confirmChangeAccountEmail(TokenEmailRequestDto request) {
        var token = tokenRepository.findTokenByToken(request.token());

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
            logger.log("TokenRevokedException occurred: Token: " + request.token() + " not revoked.",
                    "AuthenticationService", LoggerService.LoggerServiceLevel.ERROR);
            throw new TokenRevokedException(internationalization.getMessage("token.revoked", LocaleContextHolder.getLocale().getLanguage()));
        }

        Account account = repository.findAccountById(token.get().getAccount().getId()).orElseThrow(() -> new AccountNotFoundException(internationalization.getMessage("account.notFound", LocaleContextHolder.getLocale().getLanguage())));

        var userDetails = new AccountPrincipal(account);



        repository.updateEmailById(account.getId(), request.newEmail());

        logger.log("Email for account id: " + account.getId() + " has been updated to " + account.getEmail() + ".", "AccountCommandService",
                LoggerService.LoggerServiceLevel.INFO);
        return null;

    }


    @Override
    @Retryable
    @Transactional(value = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public Void changeAccountActiveness(String id, boolean activenessValue) {
        Account account = getAccountIfExistsById(id);

        if (account.isActive() == activenessValue) {
            if (activenessValue) {
                logger.log("AccountAlreadyActiveException occurred: Account id: " + id + " has been already activated.",
                        "AccountCommandService",
                        LoggerService.LoggerServiceLevel.ERROR);
                throw new AccountAlreadyActiveException(internationalization.getMessage("account.alreadyActive", LocaleContextHolder.getLocale().getLanguage()));
            } else {
                logger.log("AccountAlreadyInactiveException occurred: Account id: " + id + " has been already inactivated.",
                        "AccountCommandService",
                        LoggerService.LoggerServiceLevel.ERROR);
                throw new AccountAlreadyInactiveException(internationalization.getMessage("account.alreadyInactive", LocaleContextHolder.getLocale().getLanguage()));
            }
        }
        repository.updateIsActiveById(Long.parseLong(id), activenessValue);
        logger.log("Account id: " + id + " has been activated successfully.",
                "AccountCommandService",
                LoggerService.LoggerServiceLevel.INFO);
        return null;
    }

    @Override
    @Transactional(value = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public Void changeAccountEnable(String id, boolean enableValue) {
        Account account = getAccountIfExistsById(id);

        if (account.isEnabled() == enableValue) {
            if (enableValue) {
                logger.log("AccountAlreadyEnabledException occurred: Account id: " + id + " has been already enabled.",
                        "AccountCommandService",
                        LoggerService.LoggerServiceLevel.ERROR);
                throw new AccountAlreadyEnabledException(internationalization.getMessage("account.alreadyEnabled", LocaleContextHolder.getLocale().getLanguage()));
            } else {
                logger.log("AccountAlreadyDisabledException occurred: Account id: " + id + " has been already disabled.",
                        "AccountCommandService",
                        LoggerService.LoggerServiceLevel.ERROR);
                throw new AccountAlreadyDisabledException(internationalization.getMessage("account.alreadyDisabled", LocaleContextHolder.getLocale().getLanguage()));
            }
        }
        repository.updateIsEnabledById(Long.parseLong(id), enableValue);

        if (enableValue) {
            eventPublisher.publishEvent(new AccountEnableEmailEvent(account, account.getEmail(), account.getLanguage_()));
        } else {
            eventPublisher.publishEvent(new AccountDisableEmailEvent(account, account.getEmail(), account.getLanguage_()));
        }

        logger.log("Account id: " + id + " status has been changed for enable:"+ enableValue +".",
                "AccountCommandService",
                LoggerService.LoggerServiceLevel.INFO);
        return null;
    }

    @Override
    @Retryable
    @Transactional(value = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public Void changeAccountPassword(String id, String lastPassword, String newPassword, String repeatedNewPassword,
                                      String version, String ifMatchHeader) {
        Account account = getAccountIfExistsById(id);

        if (ifMatchHeader == null  || ifMatchHeader.isEmpty() || !ifMatchHeader.equals(messageSigner.sign(account))) {
            logger.log("ETagVerificationFailedException occurred for account id: " + id,
                    "AccountCommandService",
                    LoggerService.LoggerServiceLevel.ERROR);
            throw new ETagVerificationFailedException(internationalization.getMessage("utils.etagVerificationFailed", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (!Objects.equals(version, account.getVersion().toString())) {
            logger.log("OptimisticLockException occurred for account id: " + id,
                    "AccountCommandService",
                    LoggerService.LoggerServiceLevel.ERROR);
            throw new OptimisticLockException(internationalization.getMessage("utils.optimisticLock", LocaleContextHolder.getLocale().getLanguage()));
        }

        try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(account.getEmail(), lastPassword));
        } catch (BadCredentialsException e) {
            logger.log("AccountInvalidCredentialsException occurred: Account id: " + id + " - entered invalid credentials.",
                    "AccountCommandService",
                    LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountInvalidCredentialsException(internationalization.getMessage("account.invalidCredentials", LocaleContextHolder.getLocale().getLanguage()));
        } catch (Exception e) {
            throw new AccountAuthenticationFailedException(internationalization.getMessage("account.authenticationFailed", LocaleContextHolder.getLocale().getLanguage()));
        }

        repository.updatePasswordById(Long.parseLong(id), encoder.encode(newPassword));
        logger.log("Account id: " + id + " - password has been updated.",
                "AccountCommandService",
                LoggerService.LoggerServiceLevel.ERROR);
        return null;
    }

    @Override
    @Retryable
    @Transactional(value = "administrationTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public Void changeAccountLanguage(String id, String language, String version, String ifMatchHeader) {
        Account account = getAccountIfExistsById(id);

        if (ifMatchHeader == null  || ifMatchHeader.isEmpty() || !ifMatchHeader.equals(messageSigner.sign(account))) {
            logger.log("ETagVerificationFailedException occurred for account id: " + id,
                    "AccountCommandService",
                    LoggerService.LoggerServiceLevel.ERROR);
            throw new ETagVerificationFailedException(internationalization.getMessage("utils.etagVerificationFailed", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (!Objects.equals(version, account.getVersion().toString())) {
            logger.log("OptimisticLockException occurred for account id: " + id,
                    "AccountCommandService",
                    LoggerService.LoggerServiceLevel.ERROR);
            throw new OptimisticLockException(internationalization.getMessage("utils.optimisticLock", LocaleContextHolder.getLocale().getLanguage()));
        }

        repository.updateLanguageById(Long.parseLong(id), language);
        logger.log("Account id: " + id + " - language has been updated to " + language + " .",
                "AccountCommandService",
                LoggerService.LoggerServiceLevel.INFO);
        return null;
    }

    private Account getAccountIfExistsById(String id) {
        try {
            return repository.findAccountById(Long.parseLong(id))
                    .orElseThrow(() -> new AccountNotFoundException(internationalization.getMessage("account.notFound", LocaleContextHolder.getLocale().getLanguage())));
        } catch (AccountNotFoundException e) {
            logger.log("AccountNotFoundException occurred: Account id: " + id + " was not found.",
                    "AccountCommandService",
                    LoggerService.LoggerServiceLevel.ERROR);
            throw  e;
        }
    }

    private void saveAccountToken(Account account, String token) {
        Token tokenEntity = new Token(token, TokenTypeEnum.BEARER, false, false, account);
        tokenRepository.save(tokenEntity);
    }
}
