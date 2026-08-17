package kg.barbernotes.barbernotes.common.enums;

import lombok.Getter;

@Getter
public enum StaffRole {
    BARBER("МАСТЕР"),
    BRANCH_ADMIN("АДМИНИСТРАТОР ФИЛИАЛА"),
    SUPER_ADMIN("НАЧАЛЬНИК");

    private final String stuffRole;

    StaffRole(String stuffRole) {
        this.stuffRole = stuffRole;
    }
}

