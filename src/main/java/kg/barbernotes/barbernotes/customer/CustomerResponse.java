package kg.barbernotes.barbernotes.customer;

import kg.barbernotes.barbernotes.common.enums.Source;
import kg.barbernotes.barbernotes.common.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private UUID id;
    private String firstName;
    private String phoneNumber;
    private Source createdFrom;
    private Status status;
}