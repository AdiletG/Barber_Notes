package kg.barbernotes.barbernotes.appointment.dto;

import jakarta.validation.constraints.NotNull;
import kg.barbernotes.barbernotes.appointment.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentStatusUpdateRequest {

    @NotNull(message = "")
    private AppointmentStatus status;
}