package kg.barbernotes.barbernotes.common.security;

import kg.barbernotes.barbernotes.common.config.CorsProperties;
import kg.barbernotes.barbernotes.common.security.auth.JwtAuthenticationFilter;
import kg.barbernotes.barbernotes.common.security.ratelimit.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
   private final JwtAuthenticationFilter  jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final PublicEndpointPolicy publicEndpointPolicy;
    private final RateLimitFilter rateLimitFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(customAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicEndpointPolicy.matchers()).permitAll()
                        .anyRequest().authenticated()
                ).addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                ).addFilterBefore(
                        rateLimitFilter,
                        JwtAuthenticationFilter.class
                );
        return http.build();
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(
            RateLimitFilter rateLimitFilter
    ) {
        FilterRegistrationBean<RateLimitFilter> registration =
                new FilterRegistrationBean<>(rateLimitFilter);

        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            CorsProperties corsProperties
    ) {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(corsProperties.allowedOrigins());

        configuration.setAllowedMethods(List.of(
                "GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept"
        ));

        configuration.setExposedHeaders(List.of("Retry-After"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(corsProperties.maxAgeSeconds());

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}