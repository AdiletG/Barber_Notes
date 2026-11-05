package kg.barbernotes.barbernotes.telegram;

import kg.barbernotes.barbernotes.common.util.PhoneNormalizer;
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
        try {
            if (update == null || !update.hasMessage() || update.getMessage() == null) return;

            var message = update.getMessage();
            var chat = message.getChat();
            Long chatId = message.getChatId();


            if (message.hasContact()) {
                var contact = message.getContact();

                if (chat == null || !chat.isUserChat()) {
                    sendHandler.sendMessage(
                            chatId,
                            "Отправка контакта доступна только в личном чате с ботом."
                    );
                    return;
                }

                Long contactUserId = contact.getUserId();
                Long senderUserId = message.getFrom() != null
                        ? message.getFrom().getId()
                        : null;

                if (contactUserId == null
                        || !contactUserId.equals(senderUserId)) {
                    sendHandler.sendMessage(
                            chatId,
                            "Пожалуйста, отправьте свой контакт через кнопку ниже."
                    );
                    return;
                }

                String phoneNumber = PhoneNormalizer.normalize(contact.getPhoneNumber());

                contactHandler.checkCustomer(phoneNumber, chatId);
                return;
            }

            if (message.hasText()) {

                String text = message.getText()
                        .strip()
                        .split("\\s+", 2)[0]
                        .toLowerCase();

                if (text.equals("/start")) {
                    sendHandler.sendMessage(update.getMessage().getChatId(),
                            "Добро пожаловать!!!");


                    sendHandler.sendMessage(update.getMessage().getChatId(),
                            "Просьба отправить контакт для проверки и дальнейшей отправки ОТП");

                    List<List<String>> register = List.of(
                            List.of("Отправить контакт")
                    );

                    sendHandler.sendKeyboardRequestContact(chatId,
                            "Пожалуйста, нажмите на кнопку ниже, чтобы поделиться контактом.", register);
                } else {
                    sendHandler.sendMessage(update.getMessage().getChatId(),
                            "Не понятная команда для бота");

                    sendHandler.sendKeyboard(chatId, "Просьба выбрать команду из списка ниже", menu());
                }

                return;
            }

            sendHandler.sendMessage(update.getMessage().getChatId(),
                    "Не понятная команда для бота");

            sendHandler.sendKeyboard(chatId, "Просьба выбрать команду из списка ниже", menu());

        }catch (Exception e){
            log.error(
                    "Ошибка при обработке Telegram update. updateId={}",
                    update.getUpdateId(),
                    e
            );

            if (!update.hasMessage() || update.getMessage() == null) {
                return;
            }

            Long chatId = update.getMessage().getChatId();

            try {
                sendHandler.sendMessage(
                        chatId,
                        "Произошла ошибка. Пожалуйста, попробуйте позже."
                );
            } catch (Exception sendException) {
                log.error(
                        "Не удалось отправить сообщение об ошибке в Telegram. chatId={}",
                        chatId,
                        sendException
                );
            }
        }
    }

    private List<List<String>> menu(){
       return List.of(
                List.of("/start")
        );

    }
}