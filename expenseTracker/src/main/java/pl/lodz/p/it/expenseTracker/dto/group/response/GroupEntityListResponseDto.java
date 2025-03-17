package pl.lodz.p.it.expenseTracker.dto.group.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupEntityListResponseDto {

    List<GroupEntityResponseDto> groups;
}