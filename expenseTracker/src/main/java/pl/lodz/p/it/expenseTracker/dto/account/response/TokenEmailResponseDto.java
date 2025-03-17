package pl.lodz.p.it.expenseTracker.dto.account.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenEmailResponseDto {

    private String token;

    private String language;

    private String newEmail;

    private String id;
}