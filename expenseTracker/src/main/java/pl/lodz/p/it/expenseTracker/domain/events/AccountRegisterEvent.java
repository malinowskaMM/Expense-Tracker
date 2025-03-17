package pl.lodz.p.it.expenseTracker.domain.events;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pl.lodz.p.it.expenseTracker.domain.entity.Account;

import java.util.Objects;

@Getter
public class AccountRegisterEvent extends ApplicationEvent {

    private final String email;

    private final String token;

    private final String callbackRoute;

    private final String language_;

    public AccountRegisterEvent(Account account, String token, String callbackRoute) {
        super(account);
        this.email = account.getEmail();
        this.callbackRoute = callbackRoute;
        this.token = token;
        this.language_ = Objects.equals(account.getLanguage_(), "plPL") ? "pl" : "en";
    }
}
