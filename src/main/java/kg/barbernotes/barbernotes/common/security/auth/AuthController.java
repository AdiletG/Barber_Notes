package kg.barbernotes.barbernotes.common.security.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.exceptions.InvalidTokenException;
import kg.barbernotes.barbernotes.common.security.auth.dto.AuthResult;
import kg.barbernotes.barbernotes.common.security.auth.dto.StaffLoginRequest;
import kg.barbernotes.barbernotes.common.security.auth.dto.StaffLoginResponse;
import kg.barbernotes.barbernotes.common.security.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtProperties  jwtProperties;
    private static final String REFRESH_COOKIE_NAME =  "refresh_token";

    @PostMapping("/staff-logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();

        if(cookies != null) {
            for(Cookie cookie : cookies){
                if(REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                    authService.logout(cookie.getValue());
                    ResponseCookie expiredCookie = buildExpiredRefreshCookie();

                    response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
                    break;
                }
            }
        }

        ResponseCookie expiredCookie = buildExpiredRefreshCookie();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

    }

    @PostMapping("/staff-login")
    public StaffLoginResponse login(@Valid @RequestBody StaffLoginRequest request,
            HttpServletResponse response){

        AuthResult  authResult = authService.staffLogin(
                request.getPhoneNumber(), request.getPassword()
        );

        ResponseCookie responseCookie = buildRefreshCookie(authResult.getRefreshToken());

        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        return new StaffLoginResponse(authResult.getAccessToken());
    }

    @PostMapping("/refresh")
    public StaffLoginResponse refresh(HttpServletRequest request, HttpServletResponse response){
        String rawToken = extractRefreshToken(request);

        AuthResult  authResult = authService.refresh(rawToken);

        ResponseCookie responseCookie = buildRefreshCookie(authResult.getRefreshToken());

        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        return new StaffLoginResponse(authResult.getAccessToken());
    }



    private String extractRefreshToken(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            throw new InvalidTokenException(ErrorCode.INVALID_TOKEN, "Refresh-токен отсутствует");
        }

        for(Cookie cookie : cookies){
            if(REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        throw new InvalidTokenException(ErrorCode.INVALID_TOKEN, "Refresh-токен отсутствует");
    }

    private ResponseCookie buildExpiredRefreshCookie(){
       return ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
    }

    private ResponseCookie buildRefreshCookie(String rawRefreshToken) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, rawRefreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(Duration.ofMinutes(jwtProperties.staff().refreshTtlMinutes()))
                .build();
    }
}