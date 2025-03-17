package pl.lodz.p.it.expenseTracker.dto.account.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TokenEmailRequestDto(@NotBlank String token,
                                   @NotBlank @Email String newEmail) {
}
