package kg.barbernotes.barbernotes.service;

import kg.barbernotes.barbernotes.common.enums.Status;
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
public class ServiceResponse {

    private UUID id;
    private UUID categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationMinutes;
    private Status status;
}