package kg.barbernotes.barbernotes.customer;

import kg.barbernotes.barbernotes.common.dto.StatusUpdateRequest;
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
    public CustomerResponse update(UUID id, CustomerUpdateRequest request){
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.CUSTOMER_NOT_FOUND,
                        "Пользователь с таким id не существует"
                ));
        customerMapper.updateCustomer(request, customer);
        customer = customerRepository.save(customer);
        return customerMapper.toResponse(customer);
    }

    @Transactional
    public CustomerResponse updateStatus(UUID id, StatusUpdateRequest request) {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.CUSTOMER_NOT_FOUND,
                        "Пользователь с таким id не существует"
                ));
        if (customer.getStatus() == Status.ACTIVE && request.getStatus() == Status.INACTIVE) {
            customer.setStatus(Status.INACTIVE);
            customerRepository.save(customer);
        } else if (customer.getStatus() == Status.INACTIVE && request.getStatus() == Status.ACTIVE) {
            customer.setStatus(Status.ACTIVE);
            customerRepository.save(customer);
        } else if (customer.getStatus() == Status.INACTIVE && request.getStatus() == Status.INACTIVE) {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Статус пользователя уже НЕАКТИВЕН"
            );
        } else {
            throw new BusinessRuleViolationException(
                    ErrorCode.BUSINESS_RULE_VIOLATION,
                    "Статус пользователя уже АКТИВЕН"
            );
        }
        return customerMapper.toResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(UUID id) {
        return customerRepository.findById(id)
                .map(customerMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException(
                        ErrorCode.CUSTOMER_NOT_FOUND,
                        "Пользователь с таким id не существует"
                ));
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> findByStatus(Status status, Pageable pageable) {
        return customerRepository.findByStatus(status, pageable)
                .map(customerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable)
                .map(customerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> findByPhoneNumber(String phoneNumber,  Pageable pageable) {
        return customerRepository.findByPhoneNumber(phoneNumber, pageable)
                .map(customerMapper::toResponse);
    }
}