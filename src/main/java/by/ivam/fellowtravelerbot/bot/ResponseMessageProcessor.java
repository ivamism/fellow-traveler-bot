package by.ivam.fellowtravelerbot.bot;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

@Component
@Data
public class ResponseMessageProcessor {
    Bot bot;
    public void setBot(Bot bot) {
        this.bot = bot;
    }
    public  void sendMessage (SendMessage responseMessage){
        bot.sendMessage(responseMessage);
    }
    public void sendEditedMessage(EditMessageText responseMessage) {
        bot.sendEditMessage(responseMessage);
    }

}
