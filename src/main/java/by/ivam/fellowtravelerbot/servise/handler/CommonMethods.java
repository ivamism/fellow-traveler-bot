package by.ivam.fellowtravelerbot.servise.handler;

import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Data
@Log4j
public class CommonMethods {
    public static int trimId(String callbackData) {
        StringBuilder s = new StringBuilder(callbackData);

        while (Character.isDigit(s.charAt(0)) == false) {
            s.deleteCharAt(0);
        }
        int id = Integer.parseInt(s.toString());
        log.debug("find id - " + id);

        return id;
    }

    public static int trimId3(String callbackData) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(callbackData);
        String s = "";
        while (matcher.find()) {
            s = matcher.group();
        }

        int id = Integer.parseInt(s);
        log.debug("find id - " + id);

        return id;
    }


    public static int trimId2(String callbackData) {

        List<Character> charsList = new ArrayList<>();
        for (Character character :callbackData.toCharArray()) {
            charsList.add(character);
        }
        String s = charsList
                .stream()
                .filter(character -> Character.isDigit(character))
                .map(String::valueOf)
                .collect(Collectors.joining());

        return Integer.parseInt(s);
    }

    public static void editMessageTextGeneralPreset(Message incomeMessage) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(incomeMessage.getChatId());
        editMessage.setMessageId(incomeMessage.getMessageId());
    }

    public static String firstLetterToUpperCase(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
