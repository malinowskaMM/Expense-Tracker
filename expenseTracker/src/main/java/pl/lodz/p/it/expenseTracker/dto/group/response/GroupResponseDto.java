package pl.lodz.p.it.expenseTracker.dto.group.response;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class GroupResponseDto {

    String groupId;

    String groupName;

    String accountRole;
}