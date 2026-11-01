package kg.barbernotes.barbernotes.work_shift.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkShiftRequest {

    @NotNull(message = "Дата обязательна")
    private LocalDate workDate;

    @NotNull(message = "Время начала работы обязательно")
    private LocalTime startTime;

    @NotNull(message = "Время конца работы обязательно")
    private LocalTime endTime;
}