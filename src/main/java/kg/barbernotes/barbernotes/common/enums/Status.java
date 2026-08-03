package kg.barbernotes.barbernotes.common.enums;

import lombok.Getter;

@Getter
public enum Status {
    ACTIVE("АКТИВНЫЙ"),
    INACTIVE("НЕАКТИВНЫЙ");

    private final String statusName;

    Status(String statusName) {
        this.statusName =  statusName;
    }
}
