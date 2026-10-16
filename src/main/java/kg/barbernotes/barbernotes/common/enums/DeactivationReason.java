package kg.barbernotes.barbernotes.common.enums;

import lombok.Getter;

@Getter
public enum DeactivationReason {
    MANUAL("MANUAL"),
    BRANCH_CLOSED("BRANCH_CLOSED");

    private final String value;
    DeactivationReason(String value) {this.value = value;}
}
