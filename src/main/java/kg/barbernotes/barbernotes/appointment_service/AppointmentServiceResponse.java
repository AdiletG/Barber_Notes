package kg.barbernotes.barbernotes.appointment_service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentServiceResponse {

    private UUID id;
    private UUID serviceId;
    private String serviceName;
    private BigDecimal price;
    private Integer durationMinutes;
}