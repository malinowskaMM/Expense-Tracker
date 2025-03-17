package pl.lodz.p.it.expenseTracker.utils.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.expenseTracker.domain.events.AccountResetPasswordEvent;
import pl.lodz.p.it.expenseTracker.utils.CustomMailSenderListener;
import pl.lodz.p.it.expenseTracker.utils.Internationalization;

@Component
public class AccountResetPasswordMailSenderListener extends CustomMailSenderListener<AccountResetPasswordEvent> {
    private Internationalization internationalization;

    private final MessageSource messageSource;

    private final String appUrl;

    public AccountResetPasswordMailSenderListener(JavaMailSender mailSender, @Value("${spring.mail.username}") String sendFrom,
                                                   @Value("${app.url}") String appUrl,
                                                   Internationalization internationalization,
                                                   MessageSource messageSource) {
        super(mailSender, sendFrom);
        this.appUrl = appUrl;
        this.internationalization = internationalization;
        this.messageSource = messageSource;
    }

    @Override
    public String getSendTo(AccountResetPasswordEvent event) {
        return event.getEmail();
    }

    @Override
    public String getMailSubject(AccountResetPasswordEvent event) {
        return internationalization.getMessage("mail.account.reset.password.subject", event.getLanguage_());
    }

    @Override
    public String getMailMessage(AccountResetPasswordEvent event) {
        return internationalization.getMessage("mail.account.reset.password.message1", event.getLanguage_())
                .concat("\n")
                .concat(internationalization.getMessage("mail.account.reset.password.message2", event.getLanguage_()))
                .concat(" ")
                .concat(event.getCallbackRoute())+ "/" +event.getToken();
    }
}
