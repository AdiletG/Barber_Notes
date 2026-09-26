package kg.barbernotes.barbernotes.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;


@Slf4j
@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramDispatcher telegramDispatcher;

    public UpdateConsumer(
            TelegramDispatcher telegramDispatcher) {
        this.telegramDispatcher = telegramDispatcher;
    }
    @Override
    public void consume(Update update) {
        telegramDispatcher.route(update);
    }
}