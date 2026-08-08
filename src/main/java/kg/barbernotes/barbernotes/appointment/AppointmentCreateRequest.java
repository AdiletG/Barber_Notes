package kg.barbernotes.barbernotes.appointment;

import jakarta.validation.constraints.*;
import kg.barbernotes.barbernotes.common.enums.Source;
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

    @NotBlank(message = "")
    @Pattern(regexp = "\\d{10,15}")
    private String customerPhoneNumber;

    @NotBlank(message = "")
    private String customerName;

    @NotNull()
    private UUID barberId;

    @NotNull()
    private UUID branchId;

    @NotNull()
    @FutureOrPresent(message = "Дата не может быть в прошедшем времени")
    private LocalDate appointmentDate;

    @NotNull(message = "Начало записи обязательна")
    private LocalTime startTime;

    @NotEmpty(message = "Услуга обязательна")
    private List<UUID> serviceIds;

    @NotNull()
    private Source source;

}