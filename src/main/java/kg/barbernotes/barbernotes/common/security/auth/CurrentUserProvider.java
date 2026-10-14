package kg.barbernotes.barbernotes.common.security.auth;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Component

public class CurrentUserProvider {

    public AuthenticatedUser getCurrentUser() {
        return (AuthenticatedUser) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }
}