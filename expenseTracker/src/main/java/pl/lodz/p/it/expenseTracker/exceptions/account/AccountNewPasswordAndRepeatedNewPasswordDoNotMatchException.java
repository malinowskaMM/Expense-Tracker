package pl.lodz.p.it.expenseTracker.exceptions.account;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AccountNewPasswordAndRepeatedNewPasswordDoNotMatchException extends RuntimeException {
    public AccountNewPasswordAndRepeatedNewPasswordDoNotMatchException(String message) {
        super(message);
    }
}
