package pl.lodz.p.it.expenseTracker.domain.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pl.lodz.p.it.expenseTracker.domain.entity.Account;

import java.util.Objects;

@Getter
public class AccountChangeEmailEvent extends ApplicationEvent {

    private final String email;

    private final String token;

    private final String callbackRoute;

    private final String language_;

    private final String accountId;

    public AccountChangeEmailEvent(Account account, String email, String token, String callbackRoute) {
        super(account);
        this.accountId = account.getId().toString();
        this.email = email;
        this.callbackRoute = callbackRoute;
        this.token = token;
        this.language_ = Objects.equals(account.getLanguage_(), "plPL") ? "pl" : "en";
    }
}