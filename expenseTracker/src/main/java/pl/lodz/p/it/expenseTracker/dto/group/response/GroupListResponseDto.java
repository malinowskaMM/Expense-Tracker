package pl.lodz.p.it.expenseTracker.dto.group.response;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class GroupListResponseDto {

    List<GroupResponseDto> groups;
}