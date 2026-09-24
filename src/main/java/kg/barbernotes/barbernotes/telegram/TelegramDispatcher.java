package kg.barbernotes.barbernotes.telegram;

import kg.barbernotes.barbernotes.telegram.handlers.TelegramContactHandler;
import kg.barbernotes.barbernotes.telegram.handlers.TelegramSendHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramDispatcher {

    private final TelegramSendHandler  sendHandler;
    private final TelegramContactHandler contactHandler;

    public void route(Update update){
        if (update ==  null) return;

        if (!update.hasMessage() || update.getMessage() == null) return;

        var message = update.getMessage();
        Long chatId = message.getChatId();


            if(message.hasContact()){
               contactHandler.checkCustomer(message.getContact().getPhoneNumber(),  chatId);
                System.out.println("Зашел на проверку контакта");
            }

            if(message.hasText()){

                List<List<String>> register = List.of(
                        List.of("📱 Зарегистрироваться")
                );

                sendHandler.sendKeyboardRequestContact(chatId, "Пожалуйста, нажмите на кнопку ниже, чтобы поделиться контактом.", register);
            return;
            }

        sendHandler.sendMessage(update.getMessage().getChatId(),
                "Просьба отправить контакт для проверки и дальнейшей отправки ОТП");

        List<List<String>> register = List.of(
                List.of("📱 Зарегистрироваться")
        );

        sendHandler.sendKeyboardRequestContact(chatId, "Пожалуйста, нажмите на кнопку ниже, чтобы поделиться контактом.", register);

    }
}