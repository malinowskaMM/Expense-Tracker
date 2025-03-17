package pl.lodz.p.it.expenseTracker.exceptions.account;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
public class AnalysisCannotBeProcessed extends RuntimeException {
    public AnalysisCannotBeProcessed(String message) {
        super(message);
    }
}