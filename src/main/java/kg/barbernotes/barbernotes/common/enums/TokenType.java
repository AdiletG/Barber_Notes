package kg.barbernotes.barbernotes.common.enums;

import lombok.Getter;

@Getter
public enum TokenType {
    ACCESS("access"),
    REFRESH("refresh"),;

    private final String tokenType;

    TokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
