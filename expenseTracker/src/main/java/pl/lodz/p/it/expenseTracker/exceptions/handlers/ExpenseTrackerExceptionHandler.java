package pl.lodz.p.it.expenseTracker.exceptions.handlers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pl.lodz.p.it.expenseTracker.exceptions.account.*;
import pl.lodz.p.it.expenseTracker.exceptions.category.CategoryCannotChangeDefaultCategoryName;
import pl.lodz.p.it.expenseTracker.exceptions.category.CategoryNotFoundException;
import pl.lodz.p.it.expenseTracker.exceptions.group.GroupNotFoundException;
import pl.lodz.p.it.expenseTracker.exceptions.token.*;
import pl.lodz.p.it.expenseTracker.exceptions.transaction.*;
import pl.lodz.p.it.expenseTracker.exceptions.utils.*;

@ControllerAdvice
public class ExpenseTrackerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({
            AccountAlreadyActiveException.class,
            AccountAlreadyInactiveException.class,
            AccountAlreadyDisabledException.class,
            AccountAlreadyEnabledException.class,
            AccountAlreadyExistsException.class,
            AccountArchivedException.class,
            AccountNotActiveException.class,
            AccountNotFoundException.class,
            AccountNotEnabledException.class,
            AccountRegisterPasswordsNotMatchException.class,
            AccountNewEmailAndRepeatedNewEmailDoNotMatchException.class,
            AccountNewPasswordMatchesLastPasswordException.class,
            AccountNewPasswordAndRepeatedNewPasswordDoNotMatchException.class,

            CategoryNotFoundException.class,
            CategoryCannotChangeDefaultCategoryName.class,

            GroupNotFoundException.class,

            TransactionNotCyclicException.class,
            TransactionNotFoundException.class,
            CycleTransactionHasNoCyclicAttributesException.class,
            TransactionAlreadyRenewedFromRecurring.class,
            TransactionAlreadyStoppedFromRecurring.class,

            TokenExpiredException.class,
            TokenNotFoundException.class,
            TokenRevokedException.class,
            TokenExpiredJwtException.class,
            TokenNotValidException.class,

            ETagMessageSignerInitializationFailedException.class,
            ETagMessageSignerMessageSigningFailedException.class,
            ETagMessageVerifierSignatureValidationFailedException.class,
            ETagMessageVerifierInitializationFailedException.class,

            AnalysisCannotBeProcessed.class
    })
    public ResponseEntity<Object> handleExpenseTrackerException(Exception ex, WebRequest request) {
        return new ResponseEntity<Object>(ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            ETagVerificationFailedException.class,
            OptimisticLockException.class
    })
    public ResponseEntity<Object> handleUnprocessableEntityException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
