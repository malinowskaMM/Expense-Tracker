package pl.lodz.p.it.expenseTracker.service;

import pl.lodz.p.it.expenseTracker.dto.category.response.CategoryListResponseDto;
import pl.lodz.p.it.expenseTracker.dto.group.response.*;

public interface GroupQueryService {
    GroupUserListResponseDto getUsersInGroupWithId(String groupId);

    GroupListResponseDto getGroupsAccountBelongTo(String accountId);

    CategoryListResponseDto getCategoriesInAllGroupsAccountBelongTo(String id);

    GroupUserListResponseDto getUsersOutOfGroupWithId(String groupId);

    GroupEntityResponseDto getGroupWithId(String groupId);

    GroupEntityListResponseDto getGroups();
}
