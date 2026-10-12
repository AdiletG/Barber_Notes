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
import kg.barbernotes.barbernotes.common.exceptions.CustomAuthenticationException;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import kg.barbernotes.barbernotes.common.security.auth.AuthenticatedUser;
import kg.barbernotes.barbernotes.common.security.auth.CurrentUserProvider;
import kg.barbernotes.barbernotes.common.security.jwt.RefreshTokenService;
import kg.barbernotes.barbernotes.common.util.PasswordGenerator;
import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountCreateRequest;
import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountCreateResponse;
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
        StaffAccountEntity staffAccount = getById(id);
        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();

        if(staffAccount.getId().equals(currentUser.subjectId())){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Нельзя менять статус собственного аккаунта"
            );
        }

        if(currentUser.isBranchAdmin() && !staffAccount.getRole().equals(StaffRole.BARBER)) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Редактировать статус аккаунта Админа может только супер Админ"
            );
        }

        if(currentUser.isBranchAdmin() &&
                !currentUser.belongsToBranch(staffAccount.getBranchEntity().getId())){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Редактировать статус аккаунта барбера могут только Админы филиала к которому барбер привязан"
            );
        }

        if(staffAccount.getStatus().equals(Status.ACTIVE) && request.getStatus().equals(Status.INACTIVE)){
            staffAccount.setStatus(request.getStatus());
            refreshTokenService.revokeAllForSubject(staffAccount.getId(), SubjectType.STAFF);
            repository.save(staffAccount);
        } else if (staffAccount.getStatus().equals(Status.INACTIVE) && request.getStatus().equals(Status.ACTIVE)) {
            staffAccount.setStatus(request.getStatus());
            repository.save(staffAccount);
        }else {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Сотрудник уже имеет запрошенный статус"
            );
        }

        return mapper.toResponse(staffAccount);
    }


    @Transactional
    public StaffAccountCreateResponse create(StaffAccountCreateRequest request) {

        AuthenticatedUser currentUser = currentUserProvider.getCurrentUser();

        if(currentUser.isBranchAdmin() &&
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


        StaffAccountEntity staffAccount = mapper.toEntity(request);

        if(request.getRole() == StaffRole.BARBER) {

            if(request.getBarberId() == null) {
                throw new BusinessRuleViolationException(
                        ErrorCode.REQUEST_IS_INVALID,
                        "Роли BARBER нужен barberId"
                );
            }

            BarberEntity barberEntity = barberService.getById(request.getBarberId());

            if(currentUser.isBranchAdmin() &&
                    !currentUser.belongsToBranch(barberEntity.getBranchEntity().getId())
            ) {
                    throw new BusinessRuleViolationException(
                            ErrorCode.BUSINESS_RULE_VIOLATION,
                            "Барбер принадлежит другому филиалу"
                    );
            }

            if(barberEntity.getStatus() != Status.ACTIVE) {
                throw new BusinessRuleViolationException(
                        ErrorCode.BUSINESS_RULE_VIOLATION,
                        "Нельзя создать аккаунт для неактивного барбера"
                );
            }

            if(barberEntity.getBranchEntity().getStatus() != Status.ACTIVE) {
                throw new BusinessRuleViolationException(
                        ErrorCode.BUSINESS_RULE_VIOLATION,
                        "Нельзя создать аккаунт барберу с неактивным филиалом"
                );
            }

            if(repository.existsByBarberEntity_Id(barberEntity.getId())) {
                throw new BusinessRuleViolationException(
                        ErrorCode.BARBER_EXISTING_STAFF_ACCOUNT,
                        "Данный сотрудник уже существует"
                );
            }


            staffAccount.setBarberEntity(barberEntity);
            staffAccount.setBranchEntity(barberEntity.getBranchEntity());
            staffAccount.setRole(request.getRole());
        } else if (request.getRole() == StaffRole.BRANCH_ADMIN) {

            if(request.getBranchId() == null) {
                throw new BusinessRuleViolationException(
                        ErrorCode.REQUEST_IS_INVALID,
                        "Роли BRANCH_ADMIN нужен branchId"
                );
            }

            BranchEntity branchEntity = branchService.getById(request.getBranchId());

            if(branchEntity.getStatus() != Status.ACTIVE) {
                throw new BusinessRuleViolationException(
                        ErrorCode.BUSINESS_RULE_VIOLATION,
                        "Нельзя создать администратора для неактивного филиала"
                );
            }

            staffAccount.setBranchEntity(branchEntity);
            staffAccount.setRole(request.getRole());
        }else {
            staffAccount.setRole(request.getRole());
        }

        String temporaryPassword = passwordGenerator.generatePassword();
        staffAccount.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        staffAccount.setStatus(Status.ACTIVE);
        staffAccount.setMustChangePassword(true);
        staffAccount.setFailedLoginAttempts(0);
        repository.save(staffAccount);


        StaffAccountCreateResponse response = new StaffAccountCreateResponse();
        response.setId(staffAccount.getId());
        response.setPhoneNumber(staffAccount.getPhoneNumber());
        response.setTemporaryPassword(temporaryPassword);
        response.setRole(request.getRole());

        return response;
    }

    @Transactional
    public void changePassword(UUID staffID, String oldPassword, String newPassword) {
        StaffAccountEntity account = getById(staffID);

        if(!passwordEncoder.matches(oldPassword, account.getPasswordHash())){
            throw new CustomAuthenticationException(
                    ErrorCode.INVALID_CURRENT_PASSWORD,
                    "Текущий пароль неверный, просьба проверить и снова попробовать"
            );
        }

        if(oldPassword.equals(newPassword)){
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Новый пароль должен отличаться от старого, просьба сменить"
            );
        }

        account.setPasswordHash(passwordEncoder.encode(newPassword));
        account.setMustChangePassword(false);
        repository.save(account);
    }

    @Transactional(readOnly = true)
    public StaffAccountEntity getById(UUID staffAccountId) {
        return repository.findById(staffAccountId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.STAFF_NOT_FOUND,
                        "Сотрудник не найден"
                ));
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
                .orElseThrow(() -> new CustomAuthenticationException(
                        ErrorCode.INVALID_CREDENTIALS,
                        "Неверный телефон или пароль"
                ));
    }

}