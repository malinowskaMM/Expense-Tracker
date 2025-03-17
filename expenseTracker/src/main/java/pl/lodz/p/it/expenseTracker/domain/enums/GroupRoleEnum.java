package pl.lodz.p.it.expenseTracker.domain.enums;

import java.util.List;

public enum GroupRoleEnum {

    ADMIN(List.of("REMOVE", "EDIT")),
    USER(List.of("LEAVE"));

    private final List<String> actions;

    GroupRoleEnum(List<String> actions) {
        this.actions = actions;
    }
    List<String> getActions() {
        return this.actions;
    }
}