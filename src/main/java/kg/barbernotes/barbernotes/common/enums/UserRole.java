package kg.barbernotes.barbernotes.common.enums;

import lombok.Getter;

@Getter

public enum UserRole {
    CUSTOMER("CUSTOMER"),
    BARBER("BARBER"),
    BRANCH_ADMIN("BRANCH_ADMIN"),
    SUPER_ADMIN("SUPER_ADMIN");

    private final String userRole;

    UserRole(String userRole) {
        this.userRole = userRole;
    }
}
