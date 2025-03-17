package pl.lodz.p.it.expenseTracker.service;

import pl.lodz.p.it.expenseTracker.dto.account.request.TokenEmailRequestDto;
import pl.lodz.p.it.expenseTracker.dto.account.response.TokenEmailResponseDto;

public interface AccountCommandService {

    TokenEmailResponseDto changeAccountEmail(String id, String email, String repeatedEmail, String version, String ifMatchHeader,
                                             String callbackRoute);

    Void confirmChangeAccountEmail(TokenEmailRequestDto request);

    Void changeAccountActiveness(String id, boolean activenessValue);

    Void changeAccountEnable(String id, boolean enableValue);

    Void changeAccountPassword(String id, String lastPassword, String newPassword, String repeatedNewPassword,
            String version, String ifMatchHeader);

    Void changeAccountLanguage(String id, String language, String version, String ifMatchHeader);
}
