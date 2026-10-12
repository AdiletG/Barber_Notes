package kg.barbernotes.barbernotes.telegram.handlers;

import kg.barbernotes.barbernotes.common.enums.ErrorCode;
import kg.barbernotes.barbernotes.common.enums.Status;
import kg.barbernotes.barbernotes.common.exceptions.CustomAuthenticationException;
import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import kg.barbernotes.barbernotes.common.security.otp.OtpService;
import kg.barbernotes.barbernotes.customer.CustomerEntity;
import kg.barbernotes.barbernotes.customer.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramContactHandler {
    private final TelegramSendHandler sendHandler;
    private final CustomerService customerService;
    private final OtpService  otpService;

    public void checkCustomer(String phoneNumber, Long chatId){

        try {
            CustomerEntity customerEntity = customerService.getByPhoneNumber(phoneNumber);

            if(customerEntity.getStatus() != Status.ACTIVE) {
                throw new CustomAuthenticationException(
                        ErrorCode.CUSTOMER_NOT_FOUND,
                        "Пользователь не найден"
                );
            }

            sendHandler.sendMessage(chatId,
                    "Добро пожаловать " + customerEntity.getFirstName()
            );
            sendHandler.sendMessage(chatId,
                    "Отправляем ОТП"
            );

            String code = otpService.issueFor(customerEntity.getId());
            log.info("OTP для {}: {}", phoneNumber, code);

            sendHandler.sendMessage(chatId,
                    code
            );
        }catch (EntityNotFoundException e){
            sendHandler.removeKeyboard(chatId,
                    "К сожалению клиента под таким номером не существует");
        }catch (BusinessRuleViolationException e){
            sendHandler.removeKeyboard(chatId,
                    "Код уже был отправлен недавно, подождите несколько минут и попробуйте снова");
        }

    }
}