package kg.barbernotes.barbernotes.appointment.dto;

import jakarta.validation.constraints.*;
import kg.barbernotes.barbernotes.common.util.PhoneNormalizer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCreateRequest {

    @NotBlank(message = "Номер телефона обязателен")
    @Pattern(regexp = PhoneNormalizer.phoneNormalizeRules,
            message = PhoneNormalizer.phoneNormalizeErrorsText)
    private String customerPhoneNumber;

    @NotBlank(message = "ФИО клиента обязательное")
    @Size(max = 100, message = "Длина ФИО не должна превышать 100 символов")
    private String customerName;

    @NotNull(message = "Барбер для записи обязателен")
    private UUID barberId;

    @NotNull(message = "Филиал для записи обязателен")
    private UUID branchId;

    @NotNull(message = "Дата записи обязательна")
    private LocalDate appointmentDate;

    @NotNull(message = "Время начала записи обязательно")
    private LocalTime startTime;

    @NotEmpty(message = "Услуга обязательна")
    private List<UUID> serviceIds;

}