package kg.barbernotes.barbernotes.common.security.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.TokenType;
import kg.barbernotes.barbernotes.common.enums.UserRole;
import kg.barbernotes.barbernotes.common.exceptions.InvalidTokenException;
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
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService  jwtService;
    private final HandlerExceptionResolver resolver;

    private static final Set<String> PUBLIC = Set.of(
            "/api/v1/auth/refresh", "/api/v1/auth/logout", "/api/v1/auth/staff-login",
            "/api/v1/auth/customer/otp/request", "/api/v1/auth/customer/otp/verify");


    public JwtAuthenticationFilter(
            JwtService jwtService,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.jwtService = jwtService;
        this.resolver = resolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PUBLIC.contains(request.getServletPath());
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


                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(user, null, authorities)
                );



        }catch (InvalidTokenException e){
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

            UUID subjectID = UUID.fromString(claims.getSubject());
            UUID barberId = parseUuid(claims.get(JwtClaimNames.BARBER_ID, String.class));
            UUID branchId = parseUuid(claims.get(JwtClaimNames.BRANCH_ID, String.class));

            return new  AuthenticatedUser(subjectID, UserRole.valueOf(role), barberId, branchId);

        }catch (IllegalArgumentException | NullPointerException e){
            throw new InvalidTokenException(ErrorCode.INVALID_TOKEN, "Токен недействителен");
        }
    }

    private UUID parseUuid(String raw){
     return raw != null ? UUID.fromString(raw) : null;
    }

}