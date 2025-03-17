package pl.lodz.p.it.expenseTracker.exceptions.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class ETagVerificationFailedException extends RuntimeException {

    public ETagVerificationFailedException(String message) {
        super(message);
    }
}