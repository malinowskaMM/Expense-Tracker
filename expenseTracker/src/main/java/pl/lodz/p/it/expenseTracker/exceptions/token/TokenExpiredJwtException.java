package pl.lodz.p.it.expenseTracker.exceptions.token;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class TokenExpiredJwtException extends RuntimeException {
    public TokenExpiredJwtException(String message) {
        super(message);
    }
}
