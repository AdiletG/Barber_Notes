package kg.barbernotes.barbernotes.common.security.auth;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.StaffRole;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.enums.SubjectType;
import kg.barbernotes.barbernotes.common.exceptions.AuthenticationException;
import kg.barbernotes.barbernotes.common.security.auth.dto.AuthResult;
import kg.barbernotes.barbernotes.common.security.auth.dto.ChangePasswordRequest;
import kg.barbernotes.barbernotes.common.security.jwt.JwtService;
import kg.barbernotes.barbernotes.common.security.jwt.RefreshTokenService;
import kg.barbernotes.barbernotes.common.security.otp.OtpService;
import kg.barbernotes.barbernotes.customer.CustomerEntity;
import kg.barbernotes.barbernotes.customer.CustomerService;
import kg.barbernotes.barbernotes.staff_account.StaffAccountService;
import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final StaffAccountService staffAccountService;
    private final CustomerService customerService;
    private final OtpService  otpService;
    private final JwtService jwtService;
    private final RefreshTokenService  refreshTokenService;
    private final PasswordEncoder  passwordEncoder;


    public AuthResult customerOtpLogin(String phoneNumber, String rawCode) {
        CustomerEntity customer = customerService.getByPhoneNumber(phoneNumber);
        otpService.verify(customer.getId(), rawCode);

        Map<String, Object> extraClaims = buildCustomerClaims(customer);
        String access = jwtService.generateCustomerAccessToken(customer.getId(), extraClaims);
        RefreshTokenService.IssuedRefreshToken refresh =
                refreshTokenService.issue(customer.getId(), SubjectType.CUSTOMER);

        return new AuthResult(access, refresh.rawToken(), SubjectType.CUSTOMER);
    }

    public void requestCustomerOtp(String phoneNumber){
        CustomerEntity customer = customerService.getByPhoneNumber(phoneNumber);
        String code = otpService.issueFor(customer.getId());
        log.info("OTP для {}: {}", phoneNumber, code);
        // TODO: заменить на реальную отправку через Telegram
    }

    public AuthResult changePassword(UUID staffId, ChangePasswordRequest request) {
        StaffAccountEntity staffAccount = staffAccountService.getById(staffId);
        staffAccountService.changePassword(staffAccount.getId(), request.getOldPassword(), request.getNewPassword());
        refreshTokenService.revokeAllForSubject(staffId, SubjectType.STAFF);

        Map<String, Object> extraClaims = buildStaffClaims(staffAccount);
        RefreshTokenService.IssuedRefreshToken refresh = refreshTokenService.issue(staffAccount.getId(), SubjectType.STAFF);
        String access = jwtService.generateStaffAccessToken(staffAccount.getId(),
                staffAccount.getRole(), extraClaims);

        return new AuthResult(access, refresh.rawToken(), SubjectType.STAFF);
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeByRawToken(rawRefreshToken);
    }

    public AuthResult refresh(String oldRawRefreshToken){

        RefreshTokenService.RotatedRefreshToken rotated = refreshTokenService.rotate(oldRawRefreshToken);

        if(rotated.subjectType() == SubjectType.STAFF){
            StaffAccountEntity stuffAccount = staffAccountService.getById(rotated.subjectId());

            if(stuffAccount.getStatus() != Status.ACTIVE){
                refreshTokenService.revokeAllForSubject(stuffAccount.getId(), SubjectType.STAFF);
                throw new AuthenticationException(
                        ErrorCode.STAFF_NOT_FOUND,
                        "Неверный телефон или пароль"
                );

            }

            Map<String, Object> extraClaims = buildStaffClaims(stuffAccount);

            String access = jwtService.generateStaffAccessToken(stuffAccount.getId(),
                    stuffAccount.getRole(), extraClaims);

            return new AuthResult(access, rotated.rawRefreshToken(), SubjectType.STAFF);
        }

            CustomerEntity customer = customerService.getById(rotated.subjectId());

            if(customer.getStatus() != Status.ACTIVE){
                refreshTokenService.revokeAllForSubject(customer.getId(), SubjectType.CUSTOMER);
                throw new AuthenticationException(
                        ErrorCode.CUSTOMER_NOT_FOUND,
                        "Клиент не найден"
                );
            }
            Map<String, Object> extraClaims = buildCustomerClaims(customer);
            String access = jwtService.generateCustomerAccessToken(customer.getId(), extraClaims);
            return new AuthResult(access, rotated.rawRefreshToken(),  SubjectType.CUSTOMER);

    }

    public AuthResult staffLogin(String phoneNumber, String rawPassword){
        StaffAccountEntity stuffAccount = staffAccountService.getStaffAccount(phoneNumber);

        if(stuffAccount.getStatus() != Status.ACTIVE){
            throw new AuthenticationException(
                    ErrorCode.STAFF_NOT_FOUND,
                    "Неверный телефон или пароль"
            );
        }

        if(stuffAccount.getLockedUntil() != null &&
                stuffAccount.getLockedUntil().isAfter(OffsetDateTime.now(ZoneOffset.UTC))){
            throw new AuthenticationException(
                    ErrorCode.ACCOUNT_IS_TEMPORARILY_BLOCKED,
                    "Аккаунт временно заблокирован, попробуйте позже"
            );
        }

        if(!passwordEncoder.matches(rawPassword, stuffAccount.getPasswordHash())){
            staffAccountService.recordFailedAttempt(stuffAccount.getId());
            throw new AuthenticationException(
                    ErrorCode.STAFF_NOT_FOUND,
                    "Неверный телефон или пароль"
            );
        }

        staffAccountService.recordSuccessfulLogin(stuffAccount.getId());

        Map<String, Object> extraClaims = buildStaffClaims(stuffAccount);

        String access = jwtService.generateStaffAccessToken(stuffAccount.getId(),
                stuffAccount.getRole(), extraClaims);

        RefreshTokenService.IssuedRefreshToken refresh = refreshTokenService.issue(stuffAccount.getId(), SubjectType.STAFF);

        return new AuthResult(access, refresh.rawToken(), SubjectType.STAFF);
    }

    private Map<String, Object> buildCustomerClaims(CustomerEntity customer){
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customerId", customer.getId());
        return extraClaims;
    }

    private Map<String, Object> buildStaffClaims(StaffAccountEntity stuffAccount){
        Map<String, Object> extraClaims = new HashMap<>();

        if(stuffAccount.getRole() == StaffRole.BARBER){
            extraClaims.put("staffAccountId",  stuffAccount.getId());
            extraClaims.put("barberId", stuffAccount.getBarberEntity().getId());
            extraClaims.put("branchId", stuffAccount.getBranchEntity().getId());
        } else if (stuffAccount.getRole() == StaffRole.BRANCH_ADMIN) {
            extraClaims.put("staffAccountId",  stuffAccount.getId());
            extraClaims.put("branchId", stuffAccount.getBranchEntity().getId());
        }else {
            extraClaims.put("staffAccountId",  stuffAccount.getId());
        }

        return extraClaims;
    }
}