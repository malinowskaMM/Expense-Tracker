package pl.lodz.p.it.expenseTracker.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.lodz.p.it.expenseTracker.dto.account.request.AccountResetPasswordRequestDto;
import pl.lodz.p.it.expenseTracker.dto.account.request.TokenRequestDto;
import pl.lodz.p.it.expenseTracker.dto.authentication.request.*;
import pl.lodz.p.it.expenseTracker.dto.authentication.response.AccountAuthenticationResponseDto;

import java.io.IOException;

public interface AuthenticationService {

    AccountAuthenticationResponseDto registerAccount(AccountRegisterRequestDto request);

    AccountAuthenticationResponseDto authenticate(AccountAuthenticationRequestDto request);

    Void confirmRegistration(TokenRequestDto request);

    AccountAuthenticationResponseDto forgotPassword(AccountForgotPasswordRequestDto request);

    AccountAuthenticationResponseDto resetPassword(AccountResetPasswordRequestDto request);

    Void confirmForgotPassword(AccountConfirmPasswordRequestDto request);

    AccountAuthenticationResponseDto registerAdminAccount(AccountAdminRegisterRequestDto request);

    AccountAuthenticationResponseDto refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

    Void confirmAdminRegistration(AccountRegisterConfirmRequestDto request);

    Void confirmResetPassword(AccountConfirmPasswordRequestDto request);
}
