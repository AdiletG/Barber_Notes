package kg.barbernotes.barbernotes.branch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchCreateRequest {

    @NotBlank(message = "Название филиала не может быть пустым")
    @Size(max = 100, message = "Название филиала не может быть больше 100")
    private String name;

    @NotBlank(message = "Адрес филиала не может быть пустым")
    @Size(max = 255, message = "Название филиала не может быть больше 55")
    private String address;

    @NotNull
    private LocalTime openTime;

    @NotNull
    private LocalTime closeTime;

    @NotNull
    @Positive(message = "Количество рабочих мест не может быть меньше или равен 0")
    private Integer workplaceCount;
}