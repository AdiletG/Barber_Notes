package kg.barbernotes.barbernotes.common.security.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.TokenType;
import kg.barbernotes.barbernotes.common.enums.UserRole;
import kg.barbernotes.barbernotes.common.exceptions.BaseException;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.InvalidTokenException;
import kg.barbernotes.barbernotes.common.security.PublicEndpointPolicy;
import kg.barbernotes.barbernotes.common.security.jwt.JwtClaimNames;
import kg.barbernotes.barbernotes.common.security.jwt.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService  jwtService;
    private final HandlerExceptionResolver resolver;
    private final PublicEndpointPolicy endpointPolicy;

    private static final String CHANGE_PASSWORD_PATH = "/api/v1/auth/change-password";

    public JwtAuthenticationFilter(
            JwtService jwtService,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver,
            PublicEndpointPolicy endpointPolicy
    ) {
        this.jwtService = jwtService;
        this.resolver = resolver;
        this.endpointPolicy = endpointPolicy;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return endpointPolicy.isPublic(request);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try{
            Claims claims = jwtService.extractAllClaims(jwt);

            if(!TokenType.ACCESS.name().equals(claims.get(JwtClaimNames.TOKEN_TYPE, String.class))) {
                throw new InvalidTokenException(ErrorCode.INVALID_TOKEN, "Токен недействителен");
            }

            AuthenticatedUser user = buildUser(claims);
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name()));

            if(user.mustChangePassword()) {
                if(!request.getServletPath().equals(CHANGE_PASSWORD_PATH)) {
                    throw new BusinessRuleViolationException(
                            ErrorCode.PASSWORD_CHANGE_REQUIRED,
                            "Смена временного пароля обязательна"
                    );
                }
            }

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(user, null, authorities)
                );



        }catch (BaseException e){
            SecurityContextHolder.clearContext();
            resolver.resolveException(request, response, null, e);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private AuthenticatedUser buildUser (Claims claims){
        try{
            String role = claims.get(JwtClaimNames.ROLE,  String.class);
            if(role == null){
                throw new InvalidTokenException(ErrorCode.INVALID_TOKEN, "Токен недействителен");
            }

            UUID subjectID = parseUuid(claims.getSubject());
            if(subjectID == null){
                throw new InvalidTokenException(ErrorCode.INVALID_TOKEN, "Токен недействителен");
            }

            UUID barberId = parseUuid(claims.get(JwtClaimNames.BARBER_ID, String.class));
            UUID branchId = parseUuid(claims.get(JwtClaimNames.BRANCH_ID, String.class));
            boolean mustChangePassword = Boolean.TRUE.equals(claims.get(JwtClaimNames.MUST_CHANGE_PASSWORD, Boolean.class));

            return new  AuthenticatedUser(subjectID, UserRole.valueOf(role), barberId, branchId,  mustChangePassword);

        }catch (IllegalArgumentException e){
            throw new InvalidTokenException(ErrorCode.INVALID_TOKEN, "Токен недействителен");
        }
    }

    private UUID parseUuid(String raw){
     return raw != null ? UUID.fromString(raw) : null;
    }

}