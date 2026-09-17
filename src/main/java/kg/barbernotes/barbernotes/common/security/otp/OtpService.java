package kg.barbernotes.barbernotes.common.security.otp;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.exceptions.AuthenticationException;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.customer.CustomerEntity;
import kg.barbernotes.barbernotes.customer.CustomerService;
import kg.barbernotes.barbernotes.common.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerService customerService;
    private final PasswordGenerator passwordGenerator;


    @Transactional
    public void verify(UUID customerId, String rawCode){
        Optional<OtpEntity> oldOtp = otpRepository.findFirstByCustomerEntity_IdOrderByCreatedAtDesc(customerId);

        if(oldOtp.isEmpty()) {
            throw new BusinessRuleViolationException(
                    ErrorCode.OTP_NOT_FOUND,
                    "Такого кода не существует"
            );
        }

        OtpEntity lastOtp = oldOtp.get();

        if(lastOtp.getConsumedAt() != null){
            throw new BusinessRuleViolationException(
                    ErrorCode.OTP_NOT_FOUND,
                    "Такого кода не существует"
            );
        }

        if(lastOtp.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessRuleViolationException(
                    ErrorCode.OTP_EXPIRED,
                    "Время жизни кода истекло, попробуйте получить новый код"
            );
        }

        if(lastOtp.getAttempts() >= 5){
            throw new BusinessRuleViolationException(
                    ErrorCode.OTP_ATTEMPTS_EXCEEDED,
                    "Исчерпан лимит по попыткам"
            );
        }

        if(passwordEncoder.matches(rawCode, lastOtp.getCodeHash())){
            lastOtp.setConsumedAt(OffsetDateTime.now());
            otpRepository.save(lastOtp);
        }else {
            lastOtp.setAttempts(lastOtp.getAttempts() + 1);
            otpRepository.save(lastOtp);
            throw new AuthenticationException(
                    ErrorCode.INVALID_OTP,
                    "Неверный код, попробуйте еще раз"
            );
        }
    }

    @Transactional
    public String issueFor(UUID customerId) {
        Optional<OtpEntity> oldOtp = otpRepository.findFirstByCustomerEntity_IdOrderByCreatedAtDesc(customerId);

        if(oldOtp.isPresent()) {
            OtpEntity lastOtp = oldOtp.get();
            if(lastOtp.getConsumedAt() == null && lastOtp.getExpiresAt().isAfter(OffsetDateTime.now())) {
                throw new BusinessRuleViolationException(
                        ErrorCode.OTP_COOLDOWN_ACTIVE,
                        "код уже отправлен, попробуйте чуть позже"
                );
            }

        }

        int rawCodeAsNumber = passwordGenerator.generateOtp(); // 0..999999
        String rawCode = String.format("%06d", rawCodeAsNumber);      // всегда 6 символов

        String hash = passwordEncoder.encode(rawCode);
        CustomerEntity customerEntity = customerService.getById(customerId);


        OtpEntity newOtp = new OtpEntity();
        newOtp.setCustomerEntity(customerEntity);
        newOtp.setCodeHash(hash);
        newOtp.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
        newOtp.setAttempts(0);

        otpRepository.save(newOtp);
        return rawCode;
    }
}