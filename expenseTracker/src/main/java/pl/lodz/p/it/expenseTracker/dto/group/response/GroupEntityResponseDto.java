package pl.lodz.p.it.expenseTracker.dto.group.response;

import lombok.*;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupEntityResponseDto {

    String groupId;

    String groupName;

    GroupUserListResponseDto users;

    String sign;

    String version;
}