package pl.lodz.p.it.expenseTracker.exceptions.account;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class AccountIsNotOwnerOfGroup extends RuntimeException {
    public AccountIsNotOwnerOfGroup(String message) {
        super(message);
    }
}
