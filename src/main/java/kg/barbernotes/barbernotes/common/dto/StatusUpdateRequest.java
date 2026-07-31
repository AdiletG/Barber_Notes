package kg.barbernotes.barbernotes.common.dto;

import jakarta.validation.constraints.NotNull;
import kg.barbernotes.barbernotes.common.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusUpdateRequest {

    @NotNull(message = "")
    private Status status;
}