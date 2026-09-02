package kg.barbernotes.barbernotes.common.security.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kg.barbernotes.barbernotes.common.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService  jwtService;

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
            String tokenType = claims.get("tokenType", String.class);

            if("access".equals(tokenType)) {
                String role = claims.get("role", String.class);
                String barberIdRaw = claims.get("barberId", String.class);
                UUID barberId = barberIdRaw != null ? UUID.fromString(barberIdRaw) : null;
                String branchIdRaw = claims.get("branchId", String.class);
                UUID branchId = branchIdRaw != null ? UUID.fromString(branchIdRaw) : null;
                UUID subjectId = UUID.fromString(claims.getSubject());

                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                new AuthenticatedUser(subjectId, role, barberId, branchId),
                                null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }



        }catch (Exception e){

        }

        filterChain.doFilter(request, response);
    }

}