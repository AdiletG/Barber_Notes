package kg.barbernotes.barbernotes.service;

import jakarta.validation.constraints.*;
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
public class ServiceUpdateRequest {

    @NotNull(message = "Сервис должен иметь категорию")
    private UUID categoryId;

    @NotBlank(message = "Название услуги не может быть пустым")
    private String name;

    @Size(max = 255)
    private String description;

    @NotNull(message = "Цена услуги не может быть пустым")
    @DecimalMin("0.01")
    private BigDecimal price;

    @NotNull(message = "Длительность услуги обязательна")
    @Positive
    private Integer durationMinutes;
}