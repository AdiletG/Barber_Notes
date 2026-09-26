package kg.barbernotes.barbernotes.telegram.handlers;

import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import kg.barbernotes.barbernotes.common.security.otp.OtpService;
import kg.barbernotes.barbernotes.customer.CustomerEntity;
import kg.barbernotes.barbernotes.customer.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TelegramContactHandler {
    private final TelegramSendHandler sendHandler;
    private final CustomerService customerService;
    private final OtpService  otpService;

    public void checkCustomer(String phoneNumber, Long chatId){

        try {
            CustomerEntity customerEntity = customerService.getByPhoneNumber(phoneNumber);

            sendHandler.sendMessage(chatId,
                    "Добро пожаловать " + customerEntity.getFirstName()
            );
            sendHandler.sendMessage(chatId,
                    "Отправляем ОТП"
            );

            String code = otpService.issueFor(customerEntity.getId());

            sendHandler.sendMessage(chatId,
                    code
            );
        }catch (EntityNotFoundException e){
            sendHandler.sendMessage(chatId,
                    "К сожалению клиента под таким номером не существует"
            );
        }catch (BusinessRuleViolationException e){
            sendHandler.sendMessage(chatId,
                    "Код уже был отправлен недавно, подождите несколько минут и попробуйте снова");
        }

    }
}