package kg.barbernotes.barbernotes.common.security.auth;

import kg.barbernotes.barbernotes.common.enums.UserRole;


import java.util.UUID;

public record AuthenticatedUser(
        UUID subjectId,
        UserRole role,
        UUID barberId,   // null, если не BARBER
        UUID branchId,  // null, если не BARBER/BRANCH_ADMIN
        boolean mustChangePassword
) {

    public boolean belongsToBranch(UUID id) {
        return id.equals(branchId);
    }

    public boolean isSuperAdmin(){
        return  role == UserRole.SUPER_ADMIN;
    }
    public boolean isBranchAdmin(){
        return  role == UserRole.BRANCH_ADMIN;
    }
    public boolean isBarber(){
        return  role == UserRole.BARBER;
    }
    public boolean belongsToBarber(UUID id) {
        return id.equals(barberId);
    }
    public boolean isCustomer(){
        return  role == UserRole.CUSTOMER;
    }
}