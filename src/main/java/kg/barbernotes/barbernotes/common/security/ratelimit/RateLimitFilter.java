package kg.barbernotes.barbernotes.common.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.RateLimitGroup;
import kg.barbernotes.barbernotes.common.exceptions.RateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.path;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final String APPOINTMENTS_PATH = "/api/v1/appointments";

    private final HandlerExceptionResolver resolver;
    private final RateLimiterService rateLimiterService;

    public RateLimitFilter(
            @Qualifier("handlerExceptionResolver")
            HandlerExceptionResolver resolver,
            RateLimiterService rateLimiterService)
    {
        this.rateLimiterService = rateLimiterService;
        this.resolver = resolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod())
                || !APPOINTMENTS_PATH.equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException
    {
        String remoteAddr = request.getRemoteAddr();

        if(remoteAddr == null || remoteAddr.isBlank()){
            log.warn(
                    "Rate limit skipped: client IP is empty, path={}",
                    request.getRequestURI()
            );

            filterChain.doFilter(request, response);
            return;
        }

        RateLimitDecision decision = rateLimiterService.check(
                RateLimitGroup.IP,
                path
        );

        if(!decision.allowed()){
            log.warn(
                    "Rate limit exceeded: ip={}, path={}, retryAfter={}s",
                    remoteAddr,
                    request.getRequestURI(),
                    decision.retryAfterSeconds()
            );

            resolver.resolveException(
                    request,
                    response,
                    null,
                    new RateLimitException(decision.retryAfterSeconds())
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}