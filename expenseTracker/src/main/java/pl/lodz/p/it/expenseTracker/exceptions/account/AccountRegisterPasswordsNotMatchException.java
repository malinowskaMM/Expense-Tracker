package pl.lodz.p.it.expenseTracker.exceptions.account;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AccountRegisterPasswordsNotMatchException extends RuntimeException {

    public AccountRegisterPasswordsNotMatchException(String message) {
        super(message);
    }
}
