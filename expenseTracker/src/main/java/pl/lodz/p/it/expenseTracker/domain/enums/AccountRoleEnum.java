package pl.lodz.p.it.expenseTracker.domain.enums;

public enum AccountRoleEnum {
    USER("USER"), ADMIN("ADMIN");

    private final String roleName;

    AccountRoleEnum(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

}
