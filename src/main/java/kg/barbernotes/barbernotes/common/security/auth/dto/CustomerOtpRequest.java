package kg.barbernotes.barbernotes.common.security.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kg.barbernotes.barbernotes.common.util.PhoneNormalizer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOtpRequest {

    @NotBlank(message = "Номер телефона обязателен")
    @Pattern(regexp = PhoneNormalizer.phoneNormalizeRules,
            message = PhoneNormalizer.phoneNormalizeErrorsText)
    private String phoneNumber;
}