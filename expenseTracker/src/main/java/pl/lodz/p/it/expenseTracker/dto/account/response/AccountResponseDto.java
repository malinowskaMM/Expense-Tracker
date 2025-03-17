package pl.lodz.p.it.expenseTracker.dto.account.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponseDto {

    private Long id;

    private String email;

    private String language;

    private boolean isActive;

    private boolean isEnable;

    private LocalDateTime registerData;

    private LocalDateTime lastValidLoginDate;

    private LocalDateTime lastInvalidLoginDate;

    private String role;

    private String sign;

    private Long version;
}
