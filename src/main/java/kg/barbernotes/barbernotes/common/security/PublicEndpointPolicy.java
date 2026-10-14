
package kg.barbernotes.barbernotes.common.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;

@Component
public class PublicEndpointPolicy {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final List<EndpointRule> rules = List.of(
            new EndpointRule(null, "/api/v1/auth/customer/otp/verify"),
            new EndpointRule(null, "/api/v1/auth/staff-login"),
            new EndpointRule(null, "/api/v1/auth/refresh"),
            new EndpointRule(null, "/api/v1/auth/logout"),
            new EndpointRule(null, "/error"),

            new EndpointRule("GET", "/api/v1/branches/**"),
            new EndpointRule("GET", "/api/v1/service-categories/**"),
            new EndpointRule("GET", "/api/v1/services/**"),
            new EndpointRule("GET", "/api/v1/barbers/**"),

            new EndpointRule("POST", "/api/v1/appointments"),

            new EndpointRule(null, "/v3/api-docs/**"),
            new EndpointRule(null, "/swagger-ui/**"),
            new EndpointRule(null, "/swagger-ui.html")
    );

    public boolean isPublic(HttpServletRequest request) {
        return rules.stream().anyMatch(rule -> rule.matches(request));
    }

    public RequestMatcher[] matchers() {
        return rules.stream()
                .<RequestMatcher>map(rule -> rule::matches)
                .toArray(RequestMatcher[]::new);
    }

    private record EndpointRule(String method, String pattern) {

        private boolean matches(HttpServletRequest request) {
            boolean methodMatches =
                    method == null
                            || method.equalsIgnoreCase(request.getMethod());

            return methodMatches
                    && matchesPath(request.getServletPath());
        }

        private boolean matchesPath(String path) {
            if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }

            return pattern.endsWith("/**")
                    && PATH_MATCHER.match(
                    pattern.substring(0, pattern.length() - 3),
                    path
            );
        }
    }
}