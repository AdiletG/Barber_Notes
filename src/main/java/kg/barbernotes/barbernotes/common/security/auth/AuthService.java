package kg.barbernotes.barbernotes.common.security.auth;

import kg.barbernotes.barbernotes.common.util.PhoneNormalizer;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.StaffRole;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.enums.SubjectType;
import kg.barbernotes.barbernotes.common.exceptions.CustomAuthenticationException;
import kg.barbernotes.barbernotes.common.security.auth.dto.AuthResult;
import kg.barbernotes.barbernotes.common.security.auth.dto.ChangePasswordRequest;
import kg.barbernotes.barbernotes.common.security.jwt.JwtClaimNames;
import kg.barbernotes.barbernotes.common.security.jwt.JwtService;
import kg.barbernotes.barbernotes.common.security.jwt.RefreshTokenService;
import kg.barbernotes.barbernotes.common.security.otp.OtpService;
import kg.barbernotes.barbernotes.customer.CustomerEntity;
import kg.barbernotes.barbernotes.customer.CustomerService;
import kg.barbernotes.barbernotes.staff_account.StaffAccountService;
import kg.barbernotes.barbernotes.staff_account.StaffAccountEntity;
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
        String customerPhoneNumber = PhoneNormalizer.normalize(phoneNumber);
        CustomerEntity customer = customerService.getByPhoneNumber(customerPhoneNumber);

        if(customer.getStatus() != Status.ACTIVE) {
            throw new CustomAuthenticationException(
                    ErrorCode.CUSTOMER_NOT_FOUND,
                    "Пользователь не найден"
            );
        }

        otpService.verify(customer.getId(), rawCode);

        Map<String, Object> extraClaims = buildCustomerClaims(customer);
        String access = jwtService.generateCustomerAccessToken(customer.getId(), extraClaims);
        RefreshTokenService.IssuedRefreshToken refresh =
                refreshTokenService.issue(customer.getId(), SubjectType.CUSTOMER);

        return new AuthResult(access, refresh.rawToken(), SubjectType.CUSTOMER, Boolean.FALSE);
    }

    public String requestCustomerOtp(String phoneNumber){
        String customerPhoneNumber = PhoneNormalizer.normalize(phoneNumber);
        CustomerEntity customerEntity = customerService.getByPhoneNumber(customerPhoneNumber);

            if(customerEntity.getStatus() != Status.ACTIVE) {
                throw new CustomAuthenticationException(
                        ErrorCode.CUSTOMER_NOT_FOUND,
                        "Пользователь не найден"
                );
            }

        return otpService.issueFor(customerEntity.getId());
    }

    public AuthResult changePassword(UUID staffId, ChangePasswordRequest request) {
        StaffAccountEntity staffAccount = staffAccountService.getById(staffId);
        staffAccountService.changePassword(staffAccount.getId(), request.getOldPassword(), request.getNewPassword());
        refreshTokenService.revokeAllForSubject(staffId, SubjectType.STAFF);

        staffAccount =  staffAccountService.getById(staffId);
        Map<String, Object> extraClaims = buildStaffClaims(staffAccount);
        RefreshTokenService.IssuedRefreshToken refresh =
                refreshTokenService.issue(staffAccount.getId(), SubjectType.STAFF);
        String access = jwtService.generateStaffAccessToken(staffAccount.getId(),
                staffAccount.getRole(), extraClaims);

        return new AuthResult(access, refresh.rawToken(), SubjectType.STAFF, staffAccount.getMustChangePassword());
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeByRawToken(rawRefreshToken);
    }

    public AuthResult refresh(String oldRawRefreshToken){

        RefreshTokenService.RotatedRefreshToken rotated = refreshTokenService.rotate(oldRawRefreshToken);

        if(rotated.subjectType() == SubjectType.STAFF){
            StaffAccountEntity staffAccount = staffAccountService.getById(rotated.subjectId());

            if(staffAccount.getStatus() != Status.ACTIVE){
                refreshTokenService.revokeAllForSubject(staffAccount.getId(), SubjectType.STAFF);
                throw new CustomAuthenticationException(
                        ErrorCode.UNAUTHORIZED,
                        "Пользователь не найден"
                );

            }

            Map<String, Object> extraClaims = buildStaffClaims(staffAccount);

            String access = jwtService.generateStaffAccessToken(staffAccount.getId(),
                    staffAccount.getRole(), extraClaims);

            return new AuthResult(
                    access,
                    rotated.rawRefreshToken(),
                    SubjectType.STAFF,
                    staffAccount.getMustChangePassword());
        }

            CustomerEntity customer = customerService.getById(rotated.subjectId());

            if(customer.getStatus() != Status.ACTIVE){
                refreshTokenService.revokeAllForSubject(customer.getId(), SubjectType.CUSTOMER);
                throw new CustomAuthenticationException(
                        ErrorCode.UNAUTHORIZED,
                        "Пользователь не найден"
                );
            }
            Map<String, Object> extraClaims = buildCustomerClaims(customer);
            String access = jwtService.generateCustomerAccessToken(customer.getId(), extraClaims);
            return new AuthResult(access, rotated.rawRefreshToken(),  SubjectType.CUSTOMER, Boolean.FALSE);

    }

    public AuthResult staffLogin(String phoneNumber, String rawPassword){
        String staffAccountPhoneNumber = PhoneNormalizer.normalize(phoneNumber);
        StaffAccountEntity staffAccount = staffAccountService.getStaffAccount(staffAccountPhoneNumber);

        if(staffAccount.getLockedUntil() != null &&
                staffAccount.getLockedUntil().isAfter(OffsetDateTime.now(ZoneOffset.UTC))){
            throw new CustomAuthenticationException(
                    ErrorCode.ACCOUNT_IS_TEMPORARILY_BLOCKED,
                    "Аккаунт временно заблокирован, попробуйте позже"
            );
        }

        if(staffAccount.getStatus() != Status.ACTIVE){
            throw new CustomAuthenticationException(
                    ErrorCode.INVALID_CREDENTIALS,
                    "Неверный телефон или пароль"
            );
        }

        if(!passwordEncoder.matches(rawPassword, staffAccount.getPasswordHash())){
            staffAccountService.recordFailedAttempt(staffAccount.getId());
            throw new CustomAuthenticationException(
                    ErrorCode.INVALID_CREDENTIALS,
                    "Неверный телефон или пароль"
            );
        }

        staffAccountService.recordSuccessfulLogin(staffAccount.getId());

        Map<String, Object> extraClaims = buildStaffClaims(staffAccount);

        String access = jwtService.generateStaffAccessToken(staffAccount.getId(),
                staffAccount.getRole(), extraClaims);

        RefreshTokenService.IssuedRefreshToken refresh =
                refreshTokenService.issue(staffAccount.getId(), SubjectType.STAFF);

        return new AuthResult(
                access,
                refresh.rawToken(),
                SubjectType.STAFF,
                staffAccount.getMustChangePassword());
    }

    private Map<String, Object> buildCustomerClaims(CustomerEntity customer){
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put(JwtClaimNames.CUSTOMER_ID, customer.getId());
        extraClaims.put(JwtClaimNames.MUST_CHANGE_PASSWORD, Boolean.FALSE);
        return extraClaims;
    }

    private Map<String, Object> buildStaffClaims(StaffAccountEntity staffAccount){
        Map<String, Object> extraClaims = new HashMap<>();

        if(staffAccount.getRole() == StaffRole.BARBER){
            extraClaims.put(JwtClaimNames.STAFF_ACCOUNT_ID,  staffAccount.getId());
            extraClaims.put(JwtClaimNames.BARBER_ID, staffAccount.getBarberEntity().getId());
            extraClaims.put(JwtClaimNames.BRANCH_ID, staffAccount.getBranchEntity().getId());
        } else if (staffAccount.getRole() == StaffRole.BRANCH_ADMIN) {
            extraClaims.put(JwtClaimNames.STAFF_ACCOUNT_ID,  staffAccount.getId());
            extraClaims.put(JwtClaimNames.BRANCH_ID, staffAccount.getBranchEntity().getId());
        }else {
            extraClaims.put(JwtClaimNames.STAFF_ACCOUNT_ID,  staffAccount.getId());
        }

        extraClaims.put(JwtClaimNames.MUST_CHANGE_PASSWORD, staffAccount.getMustChangePassword());
        return extraClaims;
    }
}