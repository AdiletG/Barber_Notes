package kg.barbernotes.barbernotes.barber.dto;

import kg.barbernotes.barbernotes.common.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BarberResponse {

    private UUID id;
    private UUID branchId;
    private String firstName;
    private String lastName;
    private Integer experienceYears;
    private Status status;
}