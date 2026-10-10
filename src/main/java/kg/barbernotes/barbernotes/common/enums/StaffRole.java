package kg.barbernotes.barbernotes.common.enums;

import lombok.Getter;

@Getter
public enum StaffRole {
    BARBER("МАСТЕР"),
    BRANCH_ADMIN("АДМИНИСТРАТОР ФИЛИАЛА"),
    SUPER_ADMIN("НАЧАЛЬНИК");

    private final String staffRole;

    StaffRole(String staffRole) {
        this.staffRole = staffRole;
    }
}

