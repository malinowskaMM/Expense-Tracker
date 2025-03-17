package pl.lodz.p.it.expenseTracker.controller.query;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.expenseTracker.dto.transaction.response.TransactionListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.transaction.response.TransactionResponseDto;
import pl.lodz.p.it.expenseTracker.service.TransactionQueryService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin
@SecurityRequirement(name = "Authorization")
public class TransactionQueryController {

    private final TransactionQueryService service;

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<TransactionListResponseDto> getTransactionsInCategory(@PathVariable String categoryId) {
        return ResponseEntity.ok(service.getTransactionsInCategory(categoryId));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<TransactionListResponseDto> getTransactionsByAccountId(@PathVariable String accountId) {
        return ResponseEntity.ok(service.getTransactionsByAccountId(accountId));
    }

    @GetMapping("/transaction/{id}")
    public ResponseEntity<TransactionResponseDto> getTransactionById(@PathVariable String id) {
        TransactionResponseDto response = service.getTransactionById(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("ETag", response.getSign());

        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(response);
    }

    @GetMapping("/account/{accountId}/byDate/{date}")
    public ResponseEntity<TransactionListResponseDto> getTransactionsByAccountIdAndDate(@PathVariable String accountId,
                                                                                        @PathVariable LocalDate date) {
        return ResponseEntity.ok(service.getTransactionsByAccountIdAndDate(accountId, date));
    }

}