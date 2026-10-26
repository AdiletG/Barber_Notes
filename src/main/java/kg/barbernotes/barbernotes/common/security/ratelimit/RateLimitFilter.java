package kg.barbernotes.barbernotes.common.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kg.barbernotes.barbernotes.common.enums.RateLimitGroup;
import kg.barbernotes.barbernotes.common.exceptions.RateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;


@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final String APPOINTMENTS_PATH = "/api/v1/appointments";

    private final HandlerExceptionResolver resolver;
    private final RateLimiterService rateLimiterService;
    private final static AntPathMatcher  MATCHER = new AntPathMatcher();

    private RateLimitGroup getRateLimitGroup(HttpServletRequest request){
        String method = request.getMethod();
        String path = request.getServletPath();

        if("POST".equalsIgnoreCase(method)
        && "/api/v1/appointments".equals(path) ){
            return RateLimitGroup.IP;
        }

        if("GET".equalsIgnoreCase(method)
            && MATCHER.match("/api/v1/barbers/*/available-slots",  path)
        ){
            return RateLimitGroup.SLOTS;
        }

        return null;
    }

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
        return getRateLimitGroup(request) == null;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException
    {
        RateLimitGroup group = getRateLimitGroup(request);

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
                group,
                remoteAddr
        );

        if(!decision.allowed()){
            log.warn(
                    "Rate limit exceeded: group={}, ip={}, path={}, retryAfter={}s",
                    group,
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