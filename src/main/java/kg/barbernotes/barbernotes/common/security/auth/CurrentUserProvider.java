package kg.barbernotes.barbernotes.common.security.auth;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.exceptions.CustomAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;


@Component

public class CurrentUserProvider {

    public AuthenticatedUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if((auth == null) || !(auth.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new CustomAuthenticationException(
                    ErrorCode.UNAUTHORIZED,
                    "Требуется авторизация"
            );
        }

        return user;
    }
}