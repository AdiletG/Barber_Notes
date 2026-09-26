package kg.barbernotes.barbernotes.telegram.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TelegramSendHandler{

    private final TelegramClient client;

    public void sendMessage(Long chatId, String text){
        try {
            client.execute(SendMessage.builder().chatId(chatId).text(text).build());
        } catch (TelegramApiException e) {
            throw new RuntimeException("Ошибка при отправке сообщения", e);
        }
    }

    public void sendKeyboard(Long chatId, String text, List<List<String>> buttons) {
        ReplyKeyboardMarkup keyboard = buildKeyboard(buttons, true, false);
        try {
            client.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(keyboard)
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException("Ошибка при отправке клавиатуры", e);
        }
    }

    // Специально для кнопки "запрос контакта"
    public void sendKeyboardRequestContact(Long chatId, String text, List<List<String>> buttons) {
        ReplyKeyboardMarkup keyboard = buildKeyboard(buttons, true, true);
        try {
            client.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(keyboard)
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException("Ошибка при отправке клавиатуры с запросом контакта", e);
        }
    }

    public void removeKeyboard(Long chatId, String text) {
        ReplyKeyboardRemove remove = ReplyKeyboardRemove.builder().removeKeyboard(true).build();
        try {
            client.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .replyMarkup(remove)
                    .build());
        } catch (TelegramApiException e) {
            throw new RuntimeException("Не удалось убрать клавиатуру", e);
        }
    }

    private ReplyKeyboardMarkup buildKeyboard(List<List<String>> buttons, boolean resize, boolean requestContact) {
        List<KeyboardRow> keyboardRows = buttons.stream()
                .map(row -> {
                    KeyboardRow keyboardRow = new KeyboardRow();
                    row.forEach(btn -> {
                        KeyboardButton kb = new KeyboardButton(btn);
                        if (requestContact && ("Отправить контакт").equals(btn)) {
                            kb.setRequestContact(true); // либо параметризовать по содержимому
                        }
                        keyboardRow.add(kb);
                    });
                    return keyboardRow;
                })
                .collect(Collectors.toList());

        return ReplyKeyboardMarkup.builder()
                .keyboard(keyboardRows)
                .resizeKeyboard(resize)
                .oneTimeKeyboard(true)
                .build();
    }

}