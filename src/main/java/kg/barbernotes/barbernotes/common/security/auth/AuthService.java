package kg.barbernotes.barbernotes.common.security.auth;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.StaffRole;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.enums.SubjectType;
import kg.barbernotes.barbernotes.common.exceptions.AuthenticationException;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.security.auth.dto.AuthResult;
import kg.barbernotes.barbernotes.common.security.jwt.JwtService;
import kg.barbernotes.barbernotes.common.security.jwt.RefreshTokenService;
import kg.barbernotes.barbernotes.staff_account.StaffAccountEntity;
import kg.barbernotes.barbernotes.staff_account.StaffAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final StaffAccountService  staffAccountService;
    private final JwtService jwtService;
    private final RefreshTokenService  refreshTokenService;
    private final PasswordEncoder  passwordEncoder;


    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeByRawToken(rawRefreshToken);
    }

    public AuthResult refresh(String oldRawRefreshToken){
        RefreshTokenService.RotatedRefreshToken rotated = refreshTokenService.rotate(oldRawRefreshToken);

        if(rotated.subjectType() == SubjectType.STAFF){
            StaffAccountEntity staffAccount = staffAccountService.getById(rotated.subjectId());

            if(staffAccount.getStatus() != Status.ACTIVE){
                throw new AuthenticationException(
                        ErrorCode.STAFF_NOT_FOUND,
                        "Неверный телефон или пароль"
                );

            }

            Map<String, Object> extraClaims = buildStaffClaims(staffAccount);

            String access = jwtService.generateStaffAccessToken(staffAccount.getId(),
                    staffAccount.getRole(), extraClaims);

            return new AuthResult(access, rotated.rawRefreshToken());
        }



        throw new BusinessRuleViolationException(
                ErrorCode.BUSINESS_RULE_VIOLATION,
                "Обновление сессии клиента пока не реализовано"
        );
    }

    public AuthResult staffLogin(String phoneNumber, String rawPassword){
        StaffAccountEntity staffAccount = staffAccountService.getStaffAccount(phoneNumber);

        if(staffAccount.getStatus() != Status.ACTIVE){
            throw new AuthenticationException(
                    ErrorCode.STAFF_NOT_FOUND,
                    "Неверный телефон или пароль"
            );
        }

        if(staffAccount.getLockedUntil() != null &&
                staffAccount.getLockedUntil().isAfter(OffsetDateTime.now(ZoneOffset.UTC))){
            throw new AuthenticationException(
                    ErrorCode.ACCOUNT_IS_TEMPORARILY_BLOCKED,
                    "Аккаунт временно заблокирован, попробуйте позже"
            );
        }

        if(!passwordEncoder.matches(rawPassword, staffAccount.getPasswordHash())){
            staffAccountService.recordFailedAttempt(staffAccount.getId());
            throw new AuthenticationException(
                    ErrorCode.STAFF_NOT_FOUND,
                    "Неверный телефон или пароль"
            );
        }

        staffAccountService.recordSuccessfulLogin(staffAccount.getId());

        Map<String, Object> extraClaims = buildStaffClaims(staffAccount);

        String access = jwtService.generateStaffAccessToken(staffAccount.getId(),
                staffAccount.getRole(), extraClaims);

        RefreshTokenService.IssuedRefreshToken refresh = refreshTokenService.issue(staffAccount.getId(), SubjectType.STAFF);

        return new AuthResult(access, refresh.rawToken());
    }

    private Map<String, Object> buildStaffClaims(StaffAccountEntity staffAccount){
        Map<String, Object> extraClaims = new HashMap<>();

        if(staffAccount.getRole() == StaffRole.BARBER){
            extraClaims.put("staffAccountId",  staffAccount.getId());
            extraClaims.put("barberId", staffAccount.getBarberEntity().getId());
            extraClaims.put("branchId", staffAccount.getBranchEntity().getId());
        } else if (staffAccount.getRole() == StaffRole.BRANCH_ADMIN) {
            extraClaims.put("staffAccountId",  staffAccount.getId());
            extraClaims.put("branchId", staffAccount.getBranchEntity().getId());
        }else {
            extraClaims.put("staffAccountId",  staffAccount.getId());
        }

        return extraClaims;
    }
}