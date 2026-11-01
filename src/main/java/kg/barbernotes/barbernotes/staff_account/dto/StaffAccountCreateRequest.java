package kg.barbernotes.barbernotes.staff_account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import kg.barbernotes.barbernotes.common.enums.StaffRole;
import kg.barbernotes.barbernotes.common.util.PhoneNormalizer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAccountCreateRequest {

    @NotBlank(message = "Номер телефона сотрудника обязателен")
    @Pattern(regexp = PhoneNormalizer.PHONE_RULES,
            message = PhoneNormalizer.PHONE_ERROR_TEXT)
    private String phoneNumber;

    private UUID barberId;

    private UUID branchId;

    @NotNull(message = "Роль обязательна для заполнения")
    private StaffRole role;
}