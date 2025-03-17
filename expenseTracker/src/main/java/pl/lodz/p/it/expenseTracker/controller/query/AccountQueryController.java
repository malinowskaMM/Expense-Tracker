package pl.lodz.p.it.expenseTracker.controller.query;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.expenseTracker.dto.account.response.AccountListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.account.response.AccountResponseDto;
import pl.lodz.p.it.expenseTracker.service.AccountQueryService;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@CrossOrigin
@SecurityRequirement(name = "Authorization")
public class AccountQueryController {

    private final AccountQueryService service;

    @GetMapping()
    public ResponseEntity<AccountListResponseDto> getAccounts() {
        return ResponseEntity.ok(service.getAccounts());
    }

    @GetMapping("/account/{id}")
    public ResponseEntity<AccountResponseDto> getAccountById(@PathVariable String id) {
        AccountResponseDto response = service.getAccountById(id);
        HttpHeaders headers = new HttpHeaders();
        headers.add("ETag", response.getSign());

        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(response);
    }

    @GetMapping("/account")
    public ResponseEntity<AccountResponseDto> getAccountByEmail(@RequestParam String email) {
        AccountResponseDto response = service.getAccountByEmail(email);
        HttpHeaders headers = new HttpHeaders();
        headers.add("ETag", response.getSign());

        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(response);
    }

    @GetMapping("/users")
    public ResponseEntity<AccountListResponseDto> getUsersAccounts() {
        return ResponseEntity.ok(service.getUsersAccounts());
    }
}