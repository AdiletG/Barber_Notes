package kg.barbernotes.barbernotes.barber_service;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddBarberServiceRequest {

    @NotNull(message = "")
    private UUID serviceId;
}