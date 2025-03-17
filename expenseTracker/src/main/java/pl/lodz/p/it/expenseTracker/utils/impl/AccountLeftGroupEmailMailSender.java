package pl.lodz.p.it.expenseTracker.utils.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.expenseTracker.domain.events.infomativeEvents.AccountLeftGroupEmailEvent;
import pl.lodz.p.it.expenseTracker.utils.CustomMailSenderListener;
import pl.lodz.p.it.expenseTracker.utils.Internationalization;

@Component
public class AccountLeftGroupEmailMailSender extends CustomMailSenderListener<AccountLeftGroupEmailEvent> {

    private Internationalization internationalization;

    private final MessageSource messageSource;

    private final String appUrl;

    public AccountLeftGroupEmailMailSender(JavaMailSender mailSender, @Value("${spring.mail.username}") String sendFrom,
                                        @Value("${app.url}") String appUrl,
                                        Internationalization internationalization,
                                        MessageSource messageSource) {
        super(mailSender, sendFrom);
        this.appUrl = appUrl;
        this.internationalization = internationalization;
        this.messageSource = messageSource;
    }

    @Override
    public String getSendTo(AccountLeftGroupEmailEvent event) {
        return event.getEmail();
    }

    @Override
    public String getMailSubject(AccountLeftGroupEmailEvent event) {
        return internationalization.getMessage("mail.account.left.group.subject", event.getLanguage());
    }

    @Override
    public String getMailMessage(AccountLeftGroupEmailEvent event) {
        return internationalization.getMessage("mail.account.left.group.message1", event.getLanguage())
                .concat("\n")
                .concat(internationalization.getMessage("mail.account.left.group.message2", event.getLanguage()))
                .concat(" ")
                .concat(event.getGroupId())
                .concat(internationalization.getMessage("mail.account.left.group.message3", event.getLanguage()))
                .concat(" ")
                .concat(event.getGroupName());
    }
}