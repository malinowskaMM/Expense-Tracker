package pl.lodz.p.it.expenseTracker.dto.authentication.request;

import jakarta.validation.constraints.NotBlank;

public record AccountConfirmPasswordRequestDto(@NotBlank String token,
                                               @NotBlank String password,
                                               @NotBlank String repeatPassword) {
}
