package pl.lodz.p.it.expenseTracker.exceptions.transaction;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class CycleTransactionHasNoCyclicAttributesException extends RuntimeException {

    public CycleTransactionHasNoCyclicAttributesException(String message) {
        super(message);
    }
}
