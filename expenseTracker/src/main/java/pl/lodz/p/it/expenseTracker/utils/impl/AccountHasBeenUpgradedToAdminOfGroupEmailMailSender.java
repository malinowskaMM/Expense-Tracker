package pl.lodz.p.it.expenseTracker.utils.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.expenseTracker.domain.events.infomativeEvents.AccountHasBeenUpgradedToAdminOfGroupEmailEvent;
import pl.lodz.p.it.expenseTracker.utils.CustomMailSenderListener;
import pl.lodz.p.it.expenseTracker.utils.Internationalization;

@Component
public class AccountHasBeenUpgradedToAdminOfGroupEmailMailSender extends CustomMailSenderListener<AccountHasBeenUpgradedToAdminOfGroupEmailEvent> {

    private Internationalization internationalization;

    private final MessageSource messageSource;

    private final String appUrl;

    public AccountHasBeenUpgradedToAdminOfGroupEmailMailSender(JavaMailSender mailSender, @Value("${spring.mail.username}") String sendFrom,
                                           @Value("${app.url}") String appUrl,
                                           Internationalization internationalization,
                                           MessageSource messageSource) {
        super(mailSender, sendFrom);
        this.appUrl = appUrl;
        this.internationalization = internationalization;
        this.messageSource = messageSource;
    }

    @Override
    public String getSendTo(AccountHasBeenUpgradedToAdminOfGroupEmailEvent event) {
        return event.getEmail();
    }

    @Override
    public String getMailSubject(AccountHasBeenUpgradedToAdminOfGroupEmailEvent event) {
        return internationalization.getMessage("mail.account.new.owner.group.subject", event.getLanguage());
    }

    @Override
    public String getMailMessage(AccountHasBeenUpgradedToAdminOfGroupEmailEvent event) {
        return internationalization.getMessage("mail.account.new.owner.group.message1", event.getLanguage())
                .concat("\n")
                .concat(internationalization.getMessage("mail.account.new.owner.group.message2", event.getLanguage()))
                .concat(" ")
                .concat(event.getGroupId())
                .concat(internationalization.getMessage("mail.account.new.owner.group.message3", event.getLanguage()))
                .concat(" ")
                .concat(event.getGroupName());
    }
}