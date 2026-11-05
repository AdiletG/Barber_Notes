package kg.barbernotes.barbernotes.telegram.handlers;

import kg.barbernotes.barbernotes.common.exceptions.BusinessRuleViolationException;
import kg.barbernotes.barbernotes.common.exceptions.CustomAuthenticationException;
import kg.barbernotes.barbernotes.common.exceptions.EntityNotFoundException;
import kg.barbernotes.barbernotes.common.security.auth.AuthService;
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
    private final AuthService authService;

    public void checkCustomer(String phoneNumber, Long chatId){

        try {


            CustomerEntity customerEntity = customerService.getByPhoneNumber(phoneNumber);

            sendHandler.sendMessage(chatId,
                    "Добро пожаловать " + customerEntity.getFirstName()
            );

            String code = authService.requestCustomerOtp(phoneNumber);

            sendHandler.sendMessage(chatId,
                    "Ваш ОТП: " + code
            );
        }catch (EntityNotFoundException | CustomAuthenticationException e){
            if (e instanceof EntityNotFoundException) {
                sendHandler.removeKeyboard(chatId,
                        "Клиента с таким номером нет. Сначала запишитесь на приём, потом возвращайтесь за кодом");
            } else {
                sendHandler.removeKeyboard(chatId,
                        "Вход по этому номеру сейчас недоступен. Обратитесь в салон.");
            }
        }catch (BusinessRuleViolationException e){
            sendHandler.removeKeyboard(chatId,
                    "Код уже был отправлен недавно, подождите несколько минут и попробуйте снова");
        }

    }
}