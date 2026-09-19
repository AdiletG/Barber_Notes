package kg.barbernotes.barbernotes.common.security.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOtpVerifyRequest {
    @NotBlank(message = "Номер телефона обязателен")
    @Pattern(regexp = "\\d{10,15}")
    private String phoneNumber;

    @NotBlank
    private String code;
}