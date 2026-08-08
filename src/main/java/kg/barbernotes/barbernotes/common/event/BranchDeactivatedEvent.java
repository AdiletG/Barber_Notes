package kg.barbernotes.barbernotes.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchDeactivatedEvent {
    private UUID branchId;
}