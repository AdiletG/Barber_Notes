package kg.barbernotes.barbernotes.customer;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional
    public void update(UUID id, CustomerUpdateRequest request){
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.CUSTOMER_NOT_FOUND,
                        "Пользователь с таким id не существует"
                ));
        customerMapper.updateCustomer(request, customer);
    }

    @Transactional
    public void inactive(UUID id) {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.CUSTOMER_NOT_FOUND,
                        "Пользователь с таким id не существует"
                ));
        if (customer.getStatus() == Status.ACTIVE) {
            customer.setStatus(Status.INACTIVE);
        }else {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Статус пользователя уже НЕАКТИВЕН"
            );
        }
        customerRepository.save(customer);
    }


    @Transactional(readOnly = true)
    public Page<CustomerResponse> findByStatus(Status status, Pageable pageable) {
        return customerRepository.findByStatus(status, pageable)
                .map(customerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CustomerResponse findByPhoneNumber(String phoneNumber) {
        return customerRepository.findByPhoneNumber(phoneNumber)
                .map(customerMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.CUSTOMER_NOT_FOUND,
                        "Пользователя с таким номером не существует"
                ));
    }
}