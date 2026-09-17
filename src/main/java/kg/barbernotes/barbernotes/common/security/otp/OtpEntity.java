package kg.barbernotes.barbernotes.common.security.otp;

import jakarta.persistence.*;
import kg.barbernotes.barbernotes.common.entity.BaseEntity;
import kg.barbernotes.barbernotes.customer.CustomerEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "otp")
public class OtpEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customerEntity;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime  expiresAt;

    @Column(nullable = false)
    private Integer attempts;

    @Column(name = "consumed_at")
    private OffsetDateTime consumedAt;
}