package kg.barbernotes.barbernotes.common.enums;

import lombok.Getter;

@Getter
public enum Source {

    ONLINE("ОНЛАЙН"),
    ADMIN("АДМИНИСТРАТОР"),
    TELEGRAM ("ТЕЛЕГРАМ");

    private final String sourceName;

    Source(String sourceName) {
        this.sourceName =  sourceName;
    }
}
