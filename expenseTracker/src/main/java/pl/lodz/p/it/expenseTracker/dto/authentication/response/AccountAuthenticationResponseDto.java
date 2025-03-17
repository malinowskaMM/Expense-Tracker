package pl.lodz.p.it.expenseTracker.dto.authentication.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class AccountAuthenticationResponseDto {

    private String authenticationToken;

    private String refreshToken;

    private String language;

    private String role;

    private String id;
}