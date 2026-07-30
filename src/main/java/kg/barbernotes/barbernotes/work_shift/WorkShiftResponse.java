package kg.barbernotes.barbernotes.work_shift;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkShiftResponse {

    private UUID id;
    private UUID barberId;
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
}