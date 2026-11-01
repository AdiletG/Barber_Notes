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
public class StaffLoginRequest {

    @NotBlank(message = "Номер телефона сотрудника обязателен")
    @Pattern(regexp = PhoneNormalizer.PHONE_RULES,
            message = PhoneNormalizer.PHONE_ERROR_TEXT)
    private String phoneNumber;

    @NotBlank(message = "Пароль обязателен")
    private String password;
}