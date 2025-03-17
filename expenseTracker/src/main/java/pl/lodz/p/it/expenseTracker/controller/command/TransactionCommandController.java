package pl.lodz.p.it.expenseTracker.controller.command;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.expenseTracker.dto.transaction.request.*;
import pl.lodz.p.it.expenseTracker.service.TransactionCommandService;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin
@SecurityRequirement(name = "Authorization")
public class TransactionCommandController {

    private final TransactionCommandService service;

    @PostMapping("/transaction")
    public ResponseEntity<Void> createTransaction(@RequestBody @Valid TransactionCreateRequestDto request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        LocalDateTime localDateTime = LocalDateTime.parse(request.getDate(), formatter);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"));
        Instant instant = zonedDateTime.toInstant();
        LocalDate localDate = instant.atZone(ZoneId.of("UTC")).toLocalDate();


        return ResponseEntity.ok(service.createTransaction(
                request.getName(),
                TransactionCyclicEnumDto.valueOf(request.getCycle()),
                request.getCategoryId(),
                TransactionTypeEnumDto.valueOf(request.getType()),
                localDate,
                request.getValue(),
                !Objects.equals(request.getPeriod(), null) ? request.getPeriod() : null,
                (!Objects.equals(request.getPeriodType(), "") && !Objects.equals(request.getPeriodType(), null))
                        ? TransactionPeriodTypeDto.valueOf(request.getPeriodType()) : null,
                request.getCreatorId()
        ));
    }

    @PatchMapping("/transaction/{id}")
    public ResponseEntity<Void> changeTransaction(@PathVariable String id,
                                                  @RequestBody @Valid TransactionChangeRequestDto request,
                                                  @RequestHeader(value = "If-Match", required = false) String ifMatchHeader) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(request.getDate(), formatter);

        return ResponseEntity.ok(service.changeTransaction(
                id,
                request.getName(),
                TransactionCyclicEnumDto.valueOf(request.getCycle()),
                request.getPeriod() != null ? request.getPeriod() : null,
                request.getPeriodType() != null ? TransactionPeriodTypeDto.valueOf(request.getPeriodType()) : null,
                TransactionTypeEnumDto.valueOf(request.getType()),
                request.getCategoryId(),
                localDate,
                request.getValue(),
                request.getVersion(),
                ifMatchHeader
        ));
    }

    @DeleteMapping("/transaction/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id) {
        return ResponseEntity.ok(service.deleteTransaction(id));
    }

    @PatchMapping("/transaction/{id}/stop-recurring")
    public ResponseEntity<Void> stopRecurringTransaction(@PathVariable String id) {
        return ResponseEntity.ok(service.stopRecurringTransaction(id));
    }

    @PatchMapping("/transaction/{id}/renew-recurring")
    public ResponseEntity<Void> renewRecurringTransaction(@PathVariable String id) {
        return ResponseEntity.ok(service.renewRecurringTransaction(id));
    }
}
