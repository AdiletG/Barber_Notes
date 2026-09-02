package kg.barbernotes.barbernotes.common.security.auth;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component

public class CurrentUserProvider {

    public AuthenticatedUser getCurrentUser() {
        return (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}