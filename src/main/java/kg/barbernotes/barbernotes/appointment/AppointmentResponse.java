package kg.barbernotes.barbernotes.appointment;

import kg.barbernotes.barbernotes.common.enums.Source;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {

    private UUID id;
    private Long appointmentNumber;
    private UUID customerId;
    private UUID barberId;
    private UUID branchId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private Integer totalDurationMinutes;
    private BigDecimal totalPrice;
    private AppointmentStatus status;
    private Source source;

}