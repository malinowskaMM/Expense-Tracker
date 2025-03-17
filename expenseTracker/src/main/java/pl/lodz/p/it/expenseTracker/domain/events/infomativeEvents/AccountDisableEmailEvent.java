package pl.lodz.p.it.expenseTracker.domain.events.infomativeEvents;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pl.lodz.p.it.expenseTracker.domain.entity.Account;

import java.util.Objects;

@Getter
public class AccountDisableEmailEvent extends ApplicationEvent {

    private final String email;

    private final String language_;


    public AccountDisableEmailEvent(Account account, String email, String language_) {
        super(account);
        this.email = email;
        this.language_ = Objects.equals(language_, "plPL") ? "pl" : "en";
    }
}
