package kg.barbernotes.barbernotes.staff_account;

import kg.barbernotes.barbernotes.barber.BarberEntity;
import kg.barbernotes.barbernotes.barber.BarberService;
import kg.barbernotes.barbernotes.branch.BranchEntity;
import kg.barbernotes.barbernotes.branch.BranchService;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.StaffRole;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.exceptions.AuthenticationException;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountCreateRequest;
import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffAccountService {
    private final StaffAccountRepository repository;
    private final StaffAccountMapper  mapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;
    private final BarberService barberService;
    private final BranchService branchService;


    public StaffAccountEntity getById(UUID staffAccountId) {
        return repository.findById(staffAccountId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.STAFF_NOT_FOUND,
                        "Сотрудник не найдет"
                ));
    }

    public void recordSuccessfulLogin(UUID staffAccountId){
        StaffAccountEntity accountEntity = repository.findById(staffAccountId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.STAFF_NOT_FOUND,
                        "Сотрудник не найдет"
                ));

        accountEntity.setFailedLoginAttempts(0);
        accountEntity.setLockedUntil(null);
        accountEntity.setLastLoginAt(OffsetDateTime.now(ZoneOffset.UTC));

        repository.save(accountEntity);
    }

    public void recordFailedAttempt(UUID staffAccountId){
        StaffAccountEntity accountEntity = repository.findById(staffAccountId)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.STAFF_NOT_FOUND,
                        "Сотрудник не найдет"
                ));

        accountEntity.setFailedLoginAttempts(accountEntity.getFailedLoginAttempts() + 1);

        if(accountEntity.getFailedLoginAttempts() >= 5) {
            accountEntity.setLockedUntil(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(15));
        }

        repository.save(accountEntity);
    }

    public StaffAccountEntity getStaffAccount(String phoneNumber){
        return repository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new AuthenticationException(
                        ErrorCode.STAFF_NOT_FOUND,
                        "Неверный телефон или пароль"
                ));
    }

    public StaffAccountCreateResponse create(StaffAccountCreateRequest request) {

        StaffAccountEntity staffAccountEntity = mapper.toEntity(request);

        if(repository.existsByPhoneNumber(staffAccountEntity.getPhoneNumber())) {
            throw new BusinessRuleViolationException(
                    ErrorCode.PHONE_NUMBER_ALREADY_EXISTS,
                    "Под данным номером сотрудник уже существует"
                    );
        }

        if(request.getRole() == StaffRole.BARBER) {
            BarberEntity barberEntity = barberService.getById(request.getBarberId());
            staffAccountEntity.setBarberEntity(barberEntity);
            staffAccountEntity.setBranchEntity(barberEntity.getBranchEntity());
            staffAccountEntity.setRole(StaffRole.BARBER);
        } else if (request.getRole() == StaffRole.BRANCH_ADMIN) {
            BranchEntity branchEntity = branchService.getById(request.getBranchId());
            staffAccountEntity.setBranchEntity(branchEntity);
            staffAccountEntity.setRole(StaffRole.BRANCH_ADMIN);
        }else {
            staffAccountEntity.setRole(StaffRole.SUPER_ADMIN);
        }

        String temporaryPassword = passwordGenerator.generatePassword();
        staffAccountEntity.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        staffAccountEntity.setStatus(Status.ACTIVE);
        staffAccountEntity.setMustChangePassword(true);
        staffAccountEntity.setFailedLoginAttempts(0);
        repository.save(staffAccountEntity);


        StaffAccountCreateResponse staffAccountResponse = new StaffAccountCreateResponse();
        staffAccountResponse.setId(staffAccountEntity.getId());
        staffAccountResponse.setPhoneNumber(staffAccountEntity.getPhoneNumber());
        staffAccountResponse.setTemporaryPassword(temporaryPassword);
        staffAccountResponse.setRole(request.getRole());

        return staffAccountResponse;
    }
}