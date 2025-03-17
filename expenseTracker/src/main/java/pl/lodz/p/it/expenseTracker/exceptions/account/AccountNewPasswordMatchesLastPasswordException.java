package pl.lodz.p.it.expenseTracker.exceptions.account;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AccountNewPasswordMatchesLastPasswordException extends RuntimeException {
    public AccountNewPasswordMatchesLastPasswordException(String message) {
        super(message);
    }
}
