package pl.lodz.p.it.expenseTracker.exceptions.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ETagMessageSignerInitializationFailedException extends RuntimeException {

    public ETagMessageSignerInitializationFailedException(String message) {
        super(message);
    }
}
