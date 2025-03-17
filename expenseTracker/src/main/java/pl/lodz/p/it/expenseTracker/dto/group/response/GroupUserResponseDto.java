package pl.lodz.p.it.expenseTracker.dto.group.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupUserResponseDto {

    private String id;

    private String email;

    private String roleInCurrentGroup;
}
