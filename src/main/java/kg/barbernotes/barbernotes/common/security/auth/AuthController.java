package kg.barbernotes.barbernotes.common.security.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.exceptions.InvalidTokenException;
import kg.barbernotes.barbernotes.common.security.auth.dto.*;
import kg.barbernotes.barbernotes.common.security.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtProperties  jwtProperties;
    private static final String REFRESH_COOKIE_NAME =  "refresh_token";


    @PostMapping("/customer/otp/verify")
    public AccessTokenResponse verifyCustomerOtp (
            @Valid @RequestBody CustomerOtpVerifyRequest request,
            HttpServletResponse response) {
        AuthResult authResult = authService.customerOtpLogin(
                request.getPhoneNumber(), request.getCode());

        ResponseCookie responseCookie = buildCustomerRefreshCookie(authResult.getRefreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        return new AccessTokenResponse(authResult.getAccessToken());
    }

    @PostMapping("/customer/otp/request")
    public void requestCustomerOtp(@Valid @RequestBody CustomerOtpRequest request){
        authService.requestCustomerOtp(request.getPhoneNumber());
    }

    @PostMapping("/change-password")
    public AccessTokenResponse changePassword(
            @Valid @RequestBody ChangePasswordRequest changePasswordRequest,
            HttpServletResponse response) {

        AuthenticatedUser principal = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID staffAccountId = principal.subjectId();

        AuthResult  authResult = authService.changePassword(staffAccountId, changePasswordRequest);

        ResponseCookie responseCookie = buildStuffRefreshCookie(authResult.getRefreshToken());

        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        return new AccessTokenResponse(authResult.getAccessToken());
    }

    @PostMapping("/staff-logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();

        if(cookies != null) {
            for(Cookie cookie : cookies){
                if(REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                    authService.logout(cookie.getValue());
                    break;
                }
            }
        }

        ResponseCookie expiredCookie = buildExpiredRefreshCookie();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

    }

    @PostMapping("/staff-login")
    public AccessTokenResponse login(@Valid @RequestBody StaffLoginRequest request,
            HttpServletResponse response){

        AuthResult  authResult = authService.staffLogin(
                request.getPhoneNumber(), request.getPassword()
        );

        ResponseCookie responseCookie = buildStuffRefreshCookie(authResult.getRefreshToken());

        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        return new AccessTokenResponse(authResult.getAccessToken());
    }

    @PostMapping("/refresh")
    public AccessTokenResponse refresh(HttpServletRequest request, HttpServletResponse response){
        String rawToken = extractRefreshToken(request);

        AuthResult  authResult = authService.refresh(rawToken);

        ResponseCookie responseCookie = buildStuffRefreshCookie(authResult.getRefreshToken());

        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        return new AccessTokenResponse(authResult.getAccessToken());
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

    private ResponseCookie buildCustomerRefreshCookie(String rawRefreshToken) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, rawRefreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(Duration.ofMinutes(jwtProperties.customer().refreshTtlMinutes()))
                .build();
    }

    private ResponseCookie buildStuffRefreshCookie(String rawRefreshToken) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, rawRefreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(Duration.ofMinutes(jwtProperties.staff().refreshTtlMinutes()))
                .build();
    }
}