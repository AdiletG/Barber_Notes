package kg.barbernotes.barbernotes.common.security.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kg.barbernotes.barbernotes.common.util.PhoneNormalizer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOtpVerifyRequest {
    @NotBlank(message = "Номер телефона обязателен")
    @Pattern(regexp = PhoneNormalizer.phoneNormalizeRules,
            message = PhoneNormalizer.phoneNormalizeErrorsText)
    private String phoneNumber;

    @NotBlank(message = "Код подтверждения обязателен")
    private String code;
}