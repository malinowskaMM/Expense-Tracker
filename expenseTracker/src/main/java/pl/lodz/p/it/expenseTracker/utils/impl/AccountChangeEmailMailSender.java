package pl.lodz.p.it.expenseTracker.utils.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.expenseTracker.domain.events.AccountChangeEmailEvent;
import pl.lodz.p.it.expenseTracker.utils.CustomMailSenderListener;
import pl.lodz.p.it.expenseTracker.utils.Internationalization;

@Component
public class AccountChangeEmailMailSender extends CustomMailSenderListener<AccountChangeEmailEvent> {

    private Internationalization internationalization;

    private final MessageSource messageSource;

    private final String appUrl;

    public AccountChangeEmailMailSender(JavaMailSender mailSender, @Value("${spring.mail.username}") String sendFrom,
                                                   @Value("${app.url}") String appUrl,
                                                   Internationalization internationalization,
                                                   MessageSource messageSource) {
        super(mailSender, sendFrom);
        this.appUrl = appUrl;
        this.internationalization = internationalization;
        this.messageSource = messageSource;
    }


    @Override
    public String getSendTo(AccountChangeEmailEvent event) {
        return event.getEmail();
    }

    @Override
    public String getMailSubject(AccountChangeEmailEvent event) {
        return internationalization.getMessage("mail.account.change.email.subject", event.getLanguage_());
    }

    @Override
    public String getMailMessage(AccountChangeEmailEvent event) {
        return internationalization.getMessage("mail.account.change.email.message1", event.getLanguage_())
                .concat("\n")
                .concat(internationalization.getMessage("mail.account.change.email.message2", event.getLanguage_()))
                .concat(" ")
                .concat(event.getCallbackRoute())+ "/" + event.getAccountId() + "/" +event.getToken()+ "/" +event.getEmail();
    }
}
