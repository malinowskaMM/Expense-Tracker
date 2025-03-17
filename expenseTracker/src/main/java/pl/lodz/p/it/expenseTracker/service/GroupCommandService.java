package pl.lodz.p.it.expenseTracker.service;

import java.util.List;

public interface GroupCommandService {

    Void createGroup(String name, List<String> accountsIds, String ownerId);

    Void changeGroup(String id, String ownerAccountId, String name, List<String> emails, String version,
                     String ifMatchHeader);

    void leaveGroup(String id, String accountId);

    Void changeHeadOfGroup(String id, List<String> newOwnerIds, String version, String ifMatchHeader);
}
