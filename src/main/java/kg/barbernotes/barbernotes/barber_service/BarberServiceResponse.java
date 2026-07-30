package kg.barbernotes.barbernotes.barber_service;

import kg.barbernotes.barbernotes.common.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BarberServiceResponse {

    private UUID id;
    private UUID barberId;
    private UUID serviceId;
    private Status status;
}