package kg.barbernotes.barbernotes.service_category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCategoryCreateRequest{

    @NotBlank(message = "Название категории не может быть пустым")
    private String name;

    @Size(max = 255, message = "Достигнута пороговое значение описание в 255 символов")
    private String description;
}