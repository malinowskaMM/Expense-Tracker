package pl.lodz.p.it.expenseTracker.exceptions.handlers;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ValidationExceptionHandler {

  @ExceptionHandler(BindException.class)
  public ResponseEntity<String> handleBindException(BindException ex) {
    return ResponseEntity.badRequest().body(ex.getFieldError().getDefaultMessage());
  }
}
