package pl.lodz.p.it.expenseTracker.domain.events.infomativeEvents;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import pl.lodz.p.it.expenseTracker.domain.entity.Account;

import java.util.Objects;

@Getter
public class AccountLeftGroupEmailEvent extends ApplicationEvent {

    private final String email;

    private final String language;

    private final String groupId;

    private final String groupName;

    public AccountLeftGroupEmailEvent(Account account, String email, String language, Long id, String name) {
        super(account);
        this.email = email;
        this.language = Objects.equals(language, "plPL") ? "pl" : "en";
        this.groupId = id.toString();
        this.groupName = name;
    }
}