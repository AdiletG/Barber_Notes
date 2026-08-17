package kg.barbernotes.barbernotes.staff_account;

import kg.barbernotes.barbernotes.barber.BarberEntity;
import kg.barbernotes.barbernotes.barber.BarberService;
import kg.barbernotes.barbernotes.branch.BranchEntity;
import kg.barbernotes.barbernotes.branch.BranchService;
import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.StaffRole;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountCreateRequest;
import kg.barbernotes.barbernotes.staff_account.dto.StaffAccountCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaffAccountService {
    private final StaffAccountRepository repository;
    private final StaffAccountMapper  mapper;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;
    private final BarberService barberService;
    private final BranchService branchService;


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