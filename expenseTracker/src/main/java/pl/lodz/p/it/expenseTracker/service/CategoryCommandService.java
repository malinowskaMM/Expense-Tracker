package pl.lodz.p.it.expenseTracker.service;

public interface CategoryCommandService {

    Void createCategory(String name, String color, String description, String groupId, String accountId);

    Void deleteCategory(String id, String accountId);

    Void changeCategory(String id, String name, String color, String description, String accountId, String version,
                        String ifMatchHeader);
}
