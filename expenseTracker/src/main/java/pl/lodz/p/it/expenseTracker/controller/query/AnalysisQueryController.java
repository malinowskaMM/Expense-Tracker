package pl.lodz.p.it.expenseTracker.controller.query;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.expenseTracker.dto.analysis.request.BalanceRequestDto;
import pl.lodz.p.it.expenseTracker.dto.analysis.request.ExpensesPerCategoryRequestDto;
import pl.lodz.p.it.expenseTracker.dto.analysis.request.ExpensesRequestDto;
import pl.lodz.p.it.expenseTracker.dto.analysis.request.ExpensesVsIncomesRequestDto;
import pl.lodz.p.it.expenseTracker.dto.analysis.response.*;
import pl.lodz.p.it.expenseTracker.service.AnalysisQueryService;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@CrossOrigin
@SecurityRequirement(name = "Authorization")
public class AnalysisQueryController {

    private final AnalysisQueryService service;

    @PostMapping("/expenses")
    public ResponseEntity<ExpensesUnitsListResponseDto> getExpensesPerDay(@RequestBody @Valid ExpensesRequestDto request) {
        return ResponseEntity.ok(service.getExpenses(request.getGroupId(), request.getStartDate(), request.getEndDate()));
    }

    @PostMapping("/transactions")
    public ResponseEntity<TransactionsUnitsListResponseDto> getTransactionsPerDay(@RequestBody @Valid ExpensesRequestDto request) {
        return ResponseEntity.ok(service.getTransactions(request.getGroupId(), request.getStartDate(), request.getEndDate()));
    }

    @PostMapping("/expenses/perCategory")
    public ResponseEntity<ExpensesPerCategoryListResponseDto> getExpensesPerCategory(@RequestBody @Valid ExpensesPerCategoryRequestDto request) {
        return ResponseEntity.ok(service.getExpensesPerCategory(request.getGroupId(), request.getStartDate(), request.getEndDate()));
    }

    @PostMapping("/expenses/perType")
    public ResponseEntity<ExpensesPerTypeListResponseDto> getExpensesPerType(@RequestBody @Valid ExpensesPerCategoryRequestDto request) {
        return ResponseEntity.ok(service.getExpensesPerType(request.getGroupId(), request.getStartDate(), request.getEndDate()));
    }

    @PostMapping("/balance")
    public ResponseEntity<BalanceResponseDto> getBalance(@RequestBody @Valid BalanceRequestDto request) {
        return ResponseEntity.ok(service.getBalance(request.getGroupId(), request.getStartDate(), request.getEndDate()));
    }

    @PostMapping("/balance/expenses/incomes")
    public ResponseEntity<ExpensesVsIncomesResponseDto> getExpensesVsIncomesBalance(@RequestBody @Valid ExpensesVsIncomesRequestDto request) {
        return ResponseEntity.ok(service.getExpensesVsIncomesBalance(request.getGroupId(), request.getStartDate(), request.getEndDate()));
    }
}
