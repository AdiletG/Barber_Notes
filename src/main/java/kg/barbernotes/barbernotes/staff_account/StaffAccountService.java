package kg.barbernotes.barbernotes.staff_account;

import kg.barbernotes.barbernotes.barber.BarberEntity;
import kg.barbernotes.barbernotes.barber.BarberService;
import kg.barbernotes.barbernotes.branch.BranchEntity;
import kg.barbernotes.barbernotes.branch.BranchService;
import kg.barbernotes.barbernotes.common.dto.StatusUpdateRequest;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.StaffRole;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.enums.SubjectType;
import kg.barbernotes.barbernotes.common.exceptions.AuthenticationException;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import kg.barbernotes.barbernotes.common.security.auth.AuthenticatedUser;
import kg.barbernotes.barbernotes.common.security.auth.CurrentUserProvider;
import kg.barbernotes.barbernotes.common.security.jwt.RefreshTokenService;
import kg.barbernotes.barbernotes.common.util.PasswordGenerator;
import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountCreateRequest;
import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountCreateResponse;
import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountEntity;
import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffAccountService {
    private final StaffAccountRepository repository;
    private final StaffAccountMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;
    private final BarberService barberService;
    private final BranchService branchService;
    private final CurrentUserProvider currentUserProvider;
    private final RefreshTokenService refreshTokenService;


    @Transactional
    public StaffAccountResponse statusUpdate(UUID id, StatusUpdateRequest request){
        StaffAccountEntity stuff = getById(id);
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();

        if("BRANCH_ADMIN".equals(currentUser.role()) && !stuff.getRole().equals(StaffRole.BARBER)) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Редактировать статус аккаунта Админа может только супер Админ"
            );
        }
        if(stuff.getId().equals(currentUser.subjectId())){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "нельзя менять статус собственного аккаунта"
            );
        }
        if("BRANCH_ADMIN".equals(currentUser.role()) &&
                !stuff.getBranchEntity().getId().equals(currentUser.branchId())){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Редактировать статус аккаунта барбера могут только Админы филиала к которому барбер привязан"
            );
        }

        if(stuff.getStatus().equals(Status.ACTIVE) && request.getStatus().equals(Status.INACTIVE)){
            stuff.setStatus(request.getStatus());
            refreshTokenService.revokeAllForSubject(stuff.getId(), SubjectType.STAFF);
            repository.save(stuff);
        } else if (stuff.getStatus().equals(Status.INACTIVE) && request.getStatus().equals(Status.ACTIVE)) {
            stuff.setStatus(request.getStatus());
            repository.save(stuff);
        }else {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Сотрудник уже имеет запрошенный статус"
            );
        }

        return mapper.toResponse(stuff);
    }

    @Transactional
    public void changePassword(UUID staffID, String oldPassword, String newPassword) {
        StaffAccountEntity account = getById(staffID);

        if(!passwordEncoder.matches(oldPassword, account.getPasswordHash())){
            throw new AuthenticationException(
                    ErrorCode.INVALID_CURRENT_PASSWORD,
                    "Текущий пароль неверный, просьба проверить и снова попробовать"
                    );
        }

        account.setPasswordHash(passwordEncoder.encode(newPassword));
        account.setMustChangePassword(false);
        repository.save(account);
    }

    public StaffAccountEntity getById(UUID staffAccountId) {
        return repository.findById(staffAccountId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.STAFF_NOT_FOUND,
                        "Сотрудник не найден"
                ));
    }



    @Transactional
    public StaffAccountCreateResponse create(StaffAccountCreateRequest request) {
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();
        BarberEntity barberEntity = barberService.getById(request.getBarberId());
        BranchEntity branchEntity = branchService.getById(request.getBranchId());

        if(request.getRole().equals(StaffRole.BARBER) && repository.existsByBarberEntity_Id(barberEntity.getId())) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Данный сотрудник уже существует"
            );
        }

        boolean equals = currentUser.isBranchAdmin();

        if(equals &&
                (request.getRole().equals(StaffRole.BRANCH_ADMIN) || request.getRole().equals(StaffRole.SUPER_ADMIN)))
        {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Администратор филиала не может создать другого Администратора"
            );
        }

        if(repository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BusinessRuleViolationException(
                    ErrorCode.PHONE_NUMBER_ALREADY_EXISTS,
                    "Под данным номером сотрудник уже существует"
            );
        }

        StaffAccountEntity staffAccountEntity = mapper.toEntity(request);

        if(request.getRole() == StaffRole.BARBER) {

            if(equals &&
                    !barberEntity.getBranchEntity().getId().equals(currentUser.branchId())
            ) {
                    throw new BusinessRuleViolationException(
                            ErrorCode.BUSINESS_RULE_VIOLATION,
                            "Барбер принадлежит другому филиалу"
                    );
            }

            staffAccountEntity.setBarberEntity(barberEntity);
            staffAccountEntity.setBranchEntity(barberEntity.getBranchEntity());
            staffAccountEntity.setRole(request.getRole());
        } else if (request.getRole() == StaffRole.BRANCH_ADMIN) {
            staffAccountEntity.setBranchEntity(branchEntity);
            staffAccountEntity.setRole(request.getRole());
        }else {
            staffAccountEntity.setRole(request.getRole());
        }

        String temporaryPassword = passwordGenerator.generatePassword();
        staffAccountEntity.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        staffAccountEntity.setStatus(Status.ACTIVE);
        staffAccountEntity.setMustChangePassword(true);
        staffAccountEntity.setFailedLoginAttempts(0);
        repository.save(staffAccountEntity);


        StaffAccountCreateResponse stuffAccountResponse = new StaffAccountCreateResponse();
        stuffAccountResponse.setId(staffAccountEntity.getId());
        stuffAccountResponse.setPhoneNumber(staffAccountEntity.getPhoneNumber());
        stuffAccountResponse.setTemporaryPassword(temporaryPassword);
        stuffAccountResponse.setRole(request.getRole());

        return stuffAccountResponse;
    }

    @Transactional
    public void recordSuccessfulLogin(UUID staffAccountId){
        StaffAccountEntity accountEntity = repository.findById(staffAccountId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.STAFF_NOT_FOUND,
                        "Сотрудник не найден"
                ));

        accountEntity.setFailedLoginAttempts(0);
        accountEntity.setLockedUntil(null);
        accountEntity.setLastLoginAt(OffsetDateTime.now(ZoneOffset.UTC));

        repository.save(accountEntity);
    }

    @Transactional
    public void recordFailedAttempt(UUID staffAccountId){
        StaffAccountEntity accountEntity = repository.findById(staffAccountId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.STAFF_NOT_FOUND,
                        "Сотрудник не найден"
                ));

        accountEntity.setFailedLoginAttempts(accountEntity.getFailedLoginAttempts() + 1);

        if(accountEntity.getFailedLoginAttempts() >= 5) {
            accountEntity.setFailedLoginAttempts(0);
            accountEntity.setLockedUntil(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(15));
        }

        repository.save(accountEntity);
    }

    @Transactional(readOnly = true)
    public StaffAccountEntity getStaffAccount(String phoneNumber){
        return repository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new AuthenticationException(
                        ErrorCode.STAFF_NOT_FOUND,
                        "Неверный телефон или пароль"
                ));
    }

}