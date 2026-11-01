package kg.barbernotes.barbernotes.barber.dto;

import jakarta.validation.constraints.*;
import kg.barbernotes.barbernotes.common.util.PhoneNormalizer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BarberUpdateRequest {

    @NotBlank(message = "Имя сотрудника обязательна")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Фамилия сотрудника обязательна")
    @Size(max = 100)
    private String lastName;


    @NotBlank(message = "Номер телефона сотрудника обязателен")
    @Pattern(regexp = PhoneNormalizer.phoneNormalizeRules,
            message = PhoneNormalizer.phoneNormalizeErrorsText)
    private String phoneNumber;

    @NotNull(message = "Опыт работы сотрудника обязателен")
    @PositiveOrZero(message = "Опыт работы не может отрицательным")
    private Integer experienceYears;

}