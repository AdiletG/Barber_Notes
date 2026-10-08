package kg.barbernotes.barbernotes.work_shift.dto;

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
public class AvailableResponse {
    LocalDate slotDate;
    LocalTime startSlotTime;
    LocalTime endSlotTime;
    Boolean isAvailable;
}