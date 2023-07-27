package by.ivam.fellowtravelerbot.servise.handler;

import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
}
