package pl.lodz.p.it.expenseTracker.exceptions.category;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class CategoryNameAlreadyException extends RuntimeException {

  public CategoryNameAlreadyException(String message) {
    super(message);
  }
}