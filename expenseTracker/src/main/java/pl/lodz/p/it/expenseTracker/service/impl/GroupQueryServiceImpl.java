package pl.lodz.p.it.expenseTracker.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.expenseTracker.domain.entity.Account;
import pl.lodz.p.it.expenseTracker.domain.entity.AccountGroupRole;
import pl.lodz.p.it.expenseTracker.domain.entity.Category;
import pl.lodz.p.it.expenseTracker.domain.entity.Group;
import pl.lodz.p.it.expenseTracker.domain.enums.AccountRoleEnum;
import pl.lodz.p.it.expenseTracker.dto.category.response.CategoryListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.group.response.*;
import pl.lodz.p.it.expenseTracker.exceptions.account.AccountNotActiveException;
import pl.lodz.p.it.expenseTracker.exceptions.category.CategoryNotFoundException;
import pl.lodz.p.it.expenseTracker.exceptions.group.GroupNotFoundException;
import pl.lodz.p.it.expenseTracker.repository.tracking.AccountGroupRoleRepository;
import pl.lodz.p.it.expenseTracker.repository.administration.AccountRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.CategoryRepository;
import pl.lodz.p.it.expenseTracker.repository.tracking.GroupRepository;
import pl.lodz.p.it.expenseTracker.service.GroupQueryService;
import pl.lodz.p.it.expenseTracker.service.converter.CategoryServiceConverter;
import pl.lodz.p.it.expenseTracker.service.converter.GroupServiceConverter;
import pl.lodz.p.it.expenseTracker.utils.Internationalization;
import pl.lodz.p.it.expenseTracker.utils.LoggerService;
import pl.lodz.p.it.expenseTracker.utils.etag.MessageSigner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class GroupQueryServiceImpl implements GroupQueryService {

    private final GroupRepository repository;

    private final AccountRepository accountRepository;

    private final CategoryRepository categoryRepository;

    private final AccountGroupRoleRepository roleRepository;

    private final CategoryServiceConverter converter;

    private final GroupServiceConverter groupConverter;

    private final LoggerService logger = new LoggerService();

    private final MessageSigner messageSigner;

    private final Internationalization internationalization;

    @Override
    public GroupUserListResponseDto getUsersInGroupWithId(String groupId) {
        var group  = repository.findGroupById(Long.parseLong(groupId));

        if (group.isEmpty()) {
            logger.log("GroupNotFoundException occurred: Group with id: " + groupId + " has not been found.",
                    "GroupQueryService", LoggerService.LoggerServiceLevel.ERROR);
            throw new GroupNotFoundException(internationalization.getMessage("group.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        List<GroupUserResponseDto> users =  new ArrayList<>();
        group.get().getAccountGroupRoles().forEach(accountGroupRole -> users.add(
                GroupUserResponseDto.builder()
                        .id(accountGroupRole.getAccount().getId().toString())
                        .email(accountGroupRole.getAccount().getEmail())
                        .roleInCurrentGroup(accountGroupRole.getRole().name())
                        .build()
        ));

        logger.log("Query: getUsersInGroupWithId executed with group id: " + groupId + ".", "GroupQueryService", LoggerService.LoggerServiceLevel.INFO);
        return GroupUserListResponseDto.builder().users(users).build();
    }

    @Override
    public GroupListResponseDto getGroupsAccountBelongTo(String accountId) {
        var account = accountRepository.findAccountById(Long.parseLong(accountId));

        if (account.isEmpty()) {
            logger.log("AccountNotFoundException occurred: Account with name: " + accountId + " has not been found.",
                    "GroupQueryService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountNotActiveException(internationalization.getMessage("account.notActive", account.get().getLanguage_()));
        }

        List<GroupResponseDto> groups = new ArrayList<>();
        account.get().getGroupRoles().forEach(accountGroupRole -> groups.add(
                GroupResponseDto.builder()
                        .groupId(accountGroupRole.getGroup().getId().toString())
                        .groupName(accountGroupRole.getGroup().getName())
                        .accountRole(accountGroupRole.getRole().name())
                        .build()
        ));

        logger.log("Query: getGroupsAccountBelongTo executed with account id: " + accountId + ".", "GroupQueryService", LoggerService.LoggerServiceLevel.INFO);
        return GroupListResponseDto.builder().groups(groups).build();
    }

    @Override
    public CategoryListResponseDto getCategoriesInAllGroupsAccountBelongTo(String id) {
        var account = accountRepository.findAccountById(Long.parseLong(id));

        if (account.isEmpty()) {
            logger.log("AccountNotFoundException occurred: Account with id: " + id + " has not been found.",
                    "GroupQueryService", LoggerService.LoggerServiceLevel.ERROR);
            throw new AccountNotActiveException(internationalization.getMessage("account.notActive", account.get().getLanguage_()));
        }

        List<Group> groups = new ArrayList<>();
        account.get().getGroupRoles().forEach(accountGroupRole -> groups.add(accountGroupRole.getGroup()));

        if (groups.isEmpty()) {
            logger.log("GroupNotFoundException occurred: Group has not been found.",
                    "GroupQueryService", LoggerService.LoggerServiceLevel.ERROR);
            throw new GroupNotFoundException(internationalization.getMessage("group.notFound", account.get().getLanguage_()));
        }

        List<Category> categories = new ArrayList<>();
        groups.forEach(group -> categories.addAll(categoryRepository.findCategoriesByGroup(group)));

        if (categories.isEmpty()) {
            logger.log("CategoryNotFoundException occurred: Category has not been found.",
                    "GroupQueryService", LoggerService.LoggerServiceLevel.ERROR);
            throw new CategoryNotFoundException(internationalization.getMessage("category.notFound", account.get().getLanguage_()));
        }

        logger.log("Query: getCategoriesInAllGroupsAccountBelongTo executed with account id: " + id + ".", "GroupQueryService", LoggerService.LoggerServiceLevel.INFO);
        return converter.toCategoryListResponseDto(categories);
    }

    @Override
    public GroupUserListResponseDto getUsersOutOfGroupWithId(String groupId) {
        var group  = repository.findGroupById(Long.parseLong(groupId));

        if (group.isEmpty()) {
            logger.log("GroupNotFoundException occurred: Group with id: " + groupId + " has not been found.",
                    "GroupQueryService", LoggerService.LoggerServiceLevel.ERROR);
            throw new GroupNotFoundException(internationalization.getMessage("group.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        List<Account> users = StreamSupport.stream(accountRepository.findAll().spliterator(), false)
                .filter(account -> account.getRole().equals(AccountRoleEnum.USER)).toList();
        List<GroupUserResponseDto> usersOutOfGroup =  new ArrayList<>();
        users.forEach(user -> {
            if (!user.getGroupRoles().stream()
                    .map(AccountGroupRole::getGroup)
                    .map(Group::getId)
                    .toList().contains(group.get().getId())) {
                usersOutOfGroup.add(GroupUserResponseDto.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .roleInCurrentGroup(null)
                        .build());
            }
        });
        logger.log("Query: getUsersOutOfGroupWithId executed with group id: " + groupId + ".", "GroupQueryService", LoggerService.LoggerServiceLevel.INFO);
        return GroupUserListResponseDto.builder().users(usersOutOfGroup).build();
    }

    @Override
    public GroupEntityResponseDto getGroupWithId(String groupId) {
        var group  = repository.findGroupById(Long.parseLong(groupId));

        if (group.isEmpty()) {
            logger.log("GroupNotFoundException occurred: Group with id: " + groupId + " has not been found.",
                    "GroupQueryService", LoggerService.LoggerServiceLevel.ERROR);
            throw new GroupNotFoundException(internationalization.getMessage("group.notFound", LocaleContextHolder.getLocale().getLanguage()));
        }

        logger.log("Query: getGroupWithId executed with group id: " + groupId + ".", "GroupQueryService", LoggerService.LoggerServiceLevel.INFO);
        return GroupEntityResponseDto.builder()
                .groupId(String.valueOf(group.get().getId()))
                .groupName(group.get().getName())
                .users(toGroupUserListResponseDto(group.get().getAccountGroupRoles()))
                .sign(messageSigner.sign(group.get()))
                .version(String.valueOf(group.get().getVersion()))
                .build();
    }

    @Override
    public GroupEntityListResponseDto getGroups() {
        return groupConverter.toGroupEntityListResponseDto(StreamSupport.stream(repository.findAll().spliterator(), false).toList());
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
