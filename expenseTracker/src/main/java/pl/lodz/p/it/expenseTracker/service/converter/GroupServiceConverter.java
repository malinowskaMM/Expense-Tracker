package pl.lodz.p.it.expenseTracker.service.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.expenseTracker.domain.entity.AccountGroupRole;
import pl.lodz.p.it.expenseTracker.domain.entity.Group;
import pl.lodz.p.it.expenseTracker.dto.group.response.GroupEntityListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.group.response.GroupEntityResponseDto;
import pl.lodz.p.it.expenseTracker.dto.group.response.GroupUserListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.group.response.GroupUserResponseDto;
import pl.lodz.p.it.expenseTracker.utils.etag.MessageSigner;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupServiceConverter {

    private final MessageSigner messageSigner;

    public GroupEntityListResponseDto toGroupEntityListResponseDto(List<Group> groups) {
        return GroupEntityListResponseDto
                .builder()
                .groups(groups.stream().map(this::toGroupEntityResponseDto).toList())
                .build();
    }

    public GroupEntityResponseDto toGroupEntityResponseDto(Group group) {
        return GroupEntityResponseDto.builder()
                .groupId(String.valueOf(group.getId()))
                .groupName(group.getName())
                .users(toGroupUserListResponseDto(group.getAccountGroupRoles()))
                .sign(messageSigner.sign(group))
                .version(String.valueOf(group.getVersion()))
                .build();
    }

    private GroupUserListResponseDto toGroupUserListResponseDto(List<AccountGroupRole> accountGroupRoles) {
        return GroupUserListResponseDto.builder()
                .users(accountGroupRoles.stream().map(this::toGroupUserResponseDto).toList())
                .build();
    }

    private GroupUserResponseDto toGroupUserResponseDto(AccountGroupRole role) {
        return GroupUserResponseDto.builder()
                .id(String.valueOf(role.getAccount().getId()))
                .email(role.getAccount().getEmail())
                .roleInCurrentGroup(role.getRole().name())
                .build();
    }
}