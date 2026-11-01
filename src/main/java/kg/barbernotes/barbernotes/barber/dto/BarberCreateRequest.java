package kg.barbernotes.barbernotes.barber.dto;

import jakarta.validation.constraints.*;
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
public class BarberCreateRequest {

    @NotNull(message = "Филиал обязателен")
    private UUID branchId;

    @NotBlank(message = "Имя сотрудника обязательна")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Фамилия сотрудника обязательна")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "Номер телефона сотрудника обязателен")
    @Pattern(regexp = PhoneNormalizer.PHONE_RULES,
            message = PhoneNormalizer.PHONE_ERROR_TEXT)
    private String phoneNumber;

    @NotNull(message = "Опыт работы сотрудника обязателен")
    @PositiveOrZero
    private Integer experienceYears;

}