package kg.barbernotes.barbernotes.common.security.auth.dto;

import kg.barbernotes.barbernotes.common.enums.SubjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResult {
    private String accessToken;
    private String refreshToken;
    private SubjectType  subjectType;
}