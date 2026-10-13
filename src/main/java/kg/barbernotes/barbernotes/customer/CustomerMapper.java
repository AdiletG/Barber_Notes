package kg.barbernotes.barbernotes.customer;

import kg.barbernotes.barbernotes.customer.dto.CustomerResponse;
import kg.barbernotes.barbernotes.customer.dto.CustomerUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CustomerMapper {

    CustomerResponse toResponse(CustomerEntity customer);
    void updateCustomer(CustomerUpdateRequest request, @MappingTarget CustomerEntity customer);
}
