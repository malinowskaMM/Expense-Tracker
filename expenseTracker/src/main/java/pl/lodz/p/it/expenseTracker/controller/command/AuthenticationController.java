package pl.lodz.p.it.expenseTracker.controller.command;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.expenseTracker.dto.account.request.AccountResetPasswordRequestDto;
import pl.lodz.p.it.expenseTracker.dto.account.request.TokenRequestDto;
import pl.lodz.p.it.expenseTracker.dto.authentication.request.*;
import pl.lodz.p.it.expenseTracker.dto.authentication.response.AccountAuthenticationResponseDto;
import pl.lodz.p.it.expenseTracker.service.AuthenticationService;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @SecurityRequirement(name = "Authorization")
    @PostMapping("/account/admin/register")
    public ResponseEntity<AccountAuthenticationResponseDto> registerAdmin
            (@RequestBody @Valid AccountAdminRegisterRequestDto request) {
        return ResponseEntity.ok(service.registerAdminAccount(request));
    }

    @PostMapping("/account/admin/register/confirm")
    public ResponseEntity<Void> confirmAdminRegistration(@RequestBody AccountRegisterConfirmRequestDto request) {
        return ResponseEntity.ok(service.confirmAdminRegistration(request));
    }

    @PostMapping("/account/register")
    public ResponseEntity<AccountAuthenticationResponseDto> register(@RequestBody @Valid AccountRegisterRequestDto request) {
        return ResponseEntity.ok(service.registerAccount(request));
    }

    @PostMapping("/account/register/confirm")
    public ResponseEntity<Void> confirmRegistration(@RequestBody TokenRequestDto request) {
        return ResponseEntity.ok(service.confirmRegistration(request));
    }

    @PostMapping("/account/authenticate")
    public ResponseEntity<AccountAuthenticationResponseDto> authenticate(@RequestBody AccountAuthenticationRequestDto request) {
        var response = service.authenticate(request);
        return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + response.getAuthenticationToken())
                .body(AccountAuthenticationResponseDto.builder()
                        .authenticationToken(response.getAuthenticationToken())
                        .refreshToken(response.getRefreshToken())
                        .role(response.getRole())
                        .language(response.getLanguage())
                        .id(response.getId())
                        .build());
    }

    @PostMapping("/account/authenticate/forgot-password")
    public ResponseEntity<AccountAuthenticationResponseDto> forgotPassword(@RequestBody @Valid AccountForgotPasswordRequestDto request) {
        return ResponseEntity.ok(service.forgotPassword(request));
    }

    @PostMapping("/account/authenticate/forgot-password/confirm")
    public ResponseEntity<Void> confirmForgotPassword(@RequestBody AccountConfirmPasswordRequestDto request) {
        return ResponseEntity.ok(service.confirmForgotPassword(request));
    }

    @PostMapping("/account/authenticate/reset-password")
    public ResponseEntity<AccountAuthenticationResponseDto> resetPassword(@RequestBody @Valid AccountResetPasswordRequestDto request) {
        return ResponseEntity.ok(service.resetPassword(request));
    }

    @PostMapping("/account/authenticate/reset-password/confirm")
    public ResponseEntity<Void> confirmResetPassword(@RequestBody AccountConfirmPasswordRequestDto request) {
        return ResponseEntity.ok(service.confirmResetPassword(request));
    }

    @SecurityRequirement(name = "Authorization")
    @PostMapping("/account/refresh-token")
    public ResponseEntity<AccountAuthenticationResponseDto> refreshToken(HttpServletRequest request,
                             HttpServletResponse response,
                             @RequestHeader(value = "Authorization", required = false) String authorizationHeader) throws IOException {
        var result = service.refreshToken(request, response);
        return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + result.getAuthenticationToken())
                .body(result);
    }
}