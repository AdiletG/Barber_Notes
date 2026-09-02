package kg.barbernotes.barbernotes.common.security.auth;

import java.util.UUID;

public record AuthenticatedUser(
        UUID subjectId,
        String role,
        UUID barberId,   // null, если не BARBER
        UUID branchId    // null, если не BARBER/BRANCH_ADMIN
) {}