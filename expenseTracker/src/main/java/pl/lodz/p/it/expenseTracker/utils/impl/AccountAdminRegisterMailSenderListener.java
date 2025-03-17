package pl.lodz.p.it.expenseTracker.utils.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.expenseTracker.domain.events.AccountAdminRegisterEvent;
import pl.lodz.p.it.expenseTracker.utils.CustomMailSenderListener;
import pl.lodz.p.it.expenseTracker.utils.Internationalization;

@Component
public class AccountAdminRegisterMailSenderListener extends CustomMailSenderListener<AccountAdminRegisterEvent> {
    private Internationalization internationalization;

    private final MessageSource messageSource;

    private final String appUrl;

    public AccountAdminRegisterMailSenderListener(JavaMailSender mailSender, @Value("${spring.mail.username}") String sendFrom,
                                             @Value("${app.url}") String appUrl,
                                             Internationalization internationalization,
                                             MessageSource messageSource) {
        super(mailSender, sendFrom);
        this.appUrl = appUrl;
        this.internationalization = internationalization;
        this.messageSource = messageSource;
    }

    @Override
    public String getSendTo(AccountAdminRegisterEvent event) {
        return event.getEmail();
    }

    @Override
    public String getMailSubject(AccountAdminRegisterEvent event) {
        return internationalization.getMessage("mail.account.register.admin.subject", event.getLanguage_());
    }

    @Override
    public String getMailMessage(AccountAdminRegisterEvent event) {
        return internationalization.getMessage("mail.account.register.admin.message1", event.getLanguage_())
                .concat("\n")
                .concat(internationalization.getMessage("mail.account.register.admin.message2", event.getLanguage_()))
                .concat(" ")
                .concat(event.getCallbackRoute())+ "/" +event.getToken();
    }
}
