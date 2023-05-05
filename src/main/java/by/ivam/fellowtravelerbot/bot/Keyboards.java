package by.ivam.fellowtravelerbot.bot;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
@Data
public
class Keyboards {

    // Клавиатура главного меню

    ReplyKeyboardMarkup mainMenu() {
    ReplyKeyboardMarkup mainKeyboard = new ReplyKeyboardMarkup();

    List<KeyboardRow> keyboardRows = new ArrayList<>();

    KeyboardRow firstRow = new KeyboardRow();

        firstRow.add("Найти машину");
        firstRow.add("Найти пассажира");

        keyboardRows.add(firstRow);

        KeyboardRow secondRow = new KeyboardRow();

        secondRow.add("Помощь");
        secondRow.add("Мои данные");
//        secondRow.add("delete my data");

        keyboardRows.add(secondRow);

        mainKeyboard.setKeyboard(keyboardRows);

//        message.setReplyMarkup(mainKeyboard);
//
//    executeMessage(message);
        return mainKeyboard;
    }


     public InlineKeyboardMarkup twoButtonsInlineKeyboard(String firstButtonText, String firstButtonCallbackData, String secondButtonText, String secondButtonCallbackData) {

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> firstRowInLine = new ArrayList<>();
        InlineKeyboardButton firstButton = new InlineKeyboardButton();

        firstButton.setText(firstButtonText);
        firstButton.setCallbackData(firstButtonCallbackData);

        InlineKeyboardButton secondButton = new InlineKeyboardButton();

        secondButton.setText(secondButtonText);
        secondButton.setCallbackData(secondButtonCallbackData);

        firstRowInLine.add(firstButton);
        firstRowInLine.add(secondButton);

        rowsInLine.add(firstRowInLine);

        markupInLine.setKeyboard(rowsInLine);
         return markupInLine;
     }


}
