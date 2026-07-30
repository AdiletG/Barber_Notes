package kg.barbernotes.barbernotes.work_shift;

import jakarta.validation.constraints.FutureOrPresent;
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
public class WorkShiftUpdateRequest {

    @NotNull(message = "Дата должны быть указана")
    @FutureOrPresent(message = "Дата не может быть прошедшем")
    private LocalDate workDate;

    @NotNull(message = "Время началы работы обязательна")
    private LocalTime startTime;

    @NotNull(message = "Время конца работы обязательна")
    private LocalTime endTime;
}