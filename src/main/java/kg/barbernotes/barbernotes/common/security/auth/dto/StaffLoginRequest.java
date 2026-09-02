package kg.barbernotes.barbernotes.common.security.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "\\d{10,15}")
    private String phoneNumber;

    @NotBlank
    private String password;
}