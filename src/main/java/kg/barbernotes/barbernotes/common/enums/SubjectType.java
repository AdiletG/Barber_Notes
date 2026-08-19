package kg.barbernotes.barbernotes.common.enums;

import lombok.Getter;

@Getter
public enum SubjectType {
    CUSTOMER("Пользователь"),
    STAFF("Сотрудник");

    private final String subject;

    SubjectType(String subject) {
        this.subject = subject;
    }
}

