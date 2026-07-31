package kg.barbernotes.barbernotes.branch;

import kg.barbernotes.barbernotes.common.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {

    private UUID id;
    private String name;
    private String address;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer workplaceCount;
    private Status status;
}