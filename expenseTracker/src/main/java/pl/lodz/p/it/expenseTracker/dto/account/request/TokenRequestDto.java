package pl.lodz.p.it.expenseTracker.dto.account.request;

import jakarta.validation.constraints.NotBlank;

public record TokenRequestDto(@NotBlank String token) {
}
