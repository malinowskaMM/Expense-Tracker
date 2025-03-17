package pl.lodz.p.it.expenseTracker.controller.command;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.expenseTracker.dto.account.request.*;
import pl.lodz.p.it.expenseTracker.dto.account.response.TokenEmailResponseDto;
import pl.lodz.p.it.expenseTracker.exceptions.account.AccountNewEmailAndRepeatedNewEmailDoNotMatchException;
import pl.lodz.p.it.expenseTracker.exceptions.account.AccountNewPasswordAndRepeatedNewPasswordDoNotMatchException;
import pl.lodz.p.it.expenseTracker.exceptions.account.AccountNewPasswordMatchesLastPasswordException;
import pl.lodz.p.it.expenseTracker.service.AccountCommandService;
import pl.lodz.p.it.expenseTracker.utils.Internationalization;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@CrossOrigin
@SecurityRequirement(name = "Authorization")
public class AccountCommandController {

    private final AccountCommandService service;

    private final Internationalization internationalization;

    @PatchMapping("/account/{id}/enable")
    public ResponseEntity<Void> enableAccountById(@PathVariable String id) {
        return ResponseEntity.ok(service.changeAccountEnable(id, true));
    }

    @PatchMapping("/account/{id}/disable")
    public ResponseEntity<Void> disableAccountById(@PathVariable String id) {
        return ResponseEntity.ok(service.changeAccountEnable(id, false));
    }

    @PatchMapping("/account/{id}/activate")
    public ResponseEntity<Void> activateAccountById(@PathVariable String id) {
        return ResponseEntity.ok(service.changeAccountActiveness(id, true));
    }

    @PatchMapping("/account/{id}/inactivate")
    public ResponseEntity<Void> inactivateAccountById(@PathVariable String id) {
        return ResponseEntity.ok(service.changeAccountActiveness(id, false));
    }

    @PatchMapping("/account/{id}/language")
    public ResponseEntity<Void> setAccountLanguageById(@PathVariable String id, @RequestBody @Valid AccountLanguageChangeRequestDto request,
                                                       @RequestHeader(value = "If-Match", required = false) String ifMatchHeader) {
        return ResponseEntity.ok(service.changeAccountLanguage(id, request.getLanguage(), request.getVersion(), ifMatchHeader));
    }

    @PatchMapping("/account/{id}/email")
    public ResponseEntity<TokenEmailResponseDto> changeAccountEmailById(@PathVariable String id,
                                                                        @RequestBody @Valid AccountEmailChangeRequestDto request,
                                                                        @RequestHeader(value = "If-Match", required = false) String ifMatchHeader) {
        var email = request.getNewEmail();
        var repeatedEmail = request.getRepeatedNewEmail();

        if (!email.equals(repeatedEmail)) {
            throw new AccountNewEmailAndRepeatedNewEmailDoNotMatchException(internationalization.getMessage("account.newPasswordAndRepeatedNewPasswordDoNotMatch", LocaleContextHolder.getLocale().getLanguage()));
        }
        return ResponseEntity.ok(service.changeAccountEmail(id, email, repeatedEmail, request.getVersion(),
                ifMatchHeader, request.getCallbackRoute()));
    }

    @PostMapping("/account/{id}/email/confirm")
    public ResponseEntity<Void> confirmChangeAccountEmailById(@RequestBody TokenEmailRequestDto request) {
        return ResponseEntity.ok(service.confirmChangeAccountEmail(request));
    }

    @PatchMapping("/account/{id}/password")
    public ResponseEntity<Void> changeAccountPasswordById(@PathVariable String id,
                                                          @RequestBody @Valid AccountPasswordChangeRequestDto request,
                                                          @RequestHeader(value = "If-Match", required = false) String ifMatchHeader) {
        var lastPassword = request.getLastPassword();
        var newPassword = request.getNewPassword();
        var repeatedNewPassword = request.getRepeatedNewPassword();

        if (lastPassword.equals(newPassword)) {
            throw new AccountNewPasswordMatchesLastPasswordException(internationalization.getMessage("account.newPasswordMatchesLastPassword", LocaleContextHolder.getLocale().getLanguage()));
        }

        if (!newPassword.equals(repeatedNewPassword)) {
            throw new AccountNewPasswordAndRepeatedNewPasswordDoNotMatchException(internationalization.getMessage("account.newEmailAndRepeatedNewEmailDoNotMatch", LocaleContextHolder.getLocale().getLanguage()));
        }
        return ResponseEntity.ok(service.changeAccountPassword(id, lastPassword, newPassword, repeatedNewPassword,
                request.getVersion(), ifMatchHeader));
    }
}
