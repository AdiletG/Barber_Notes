package kg.barbernotes.barbernotes.service_category;

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
public class ServiceCategoryResponse {

    private UUID id;
    private String name;
    private String description;
    private Status status;
}