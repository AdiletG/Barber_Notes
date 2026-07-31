package kg.barbernotes.barbernotes.appointment;

import lombok.Getter;

@Getter
public enum AppointmentStatus {

    CONFIRMED("ПОДТВЕРЖДЕНО"),
    COMPLETED("ЗАВЕРШЕНО"),
    CANCELLED("ОТМЕНЕНО"),
    NO_SHOW("НЕЯВКА");

    private final String statusName;

    AppointmentStatus(String statusName) {
        this.statusName =  statusName;
    }
}
