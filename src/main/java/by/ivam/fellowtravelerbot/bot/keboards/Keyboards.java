package by.ivam.fellowtravelerbot.bot.keboards;
/*
Main menu and inline keyboards
 */

import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
@Data
@Log4j
public class Keyboards {
    @Autowired
    Buttons buttons;

    // Main Admin menu
    public ReplyKeyboardMarkup mainAdminMenu() {
        ReplyKeyboardMarkup mainKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add(buttons.getMAIN_ADMIN_ADD_SETTLEMENT());
        firstRow.add(buttons.getMAIN_ADMIN_DELETE_SETTLEMENT());
        keyboardRows.add(firstRow);

        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add(buttons.getMAIN_ADMIN_ADD_LOCATION());
        secondRow.add(buttons.getMAIN_ADMIN_DELETE_LOCATION());
        keyboardRows.add(secondRow);

        KeyboardRow thirdRow = new KeyboardRow();
        thirdRow.add(buttons.getMAIN_ADMIN_SET_ADMIN());
        thirdRow.add(buttons.getMAIN_ADMIN_BLOCK_USER());
        keyboardRows.add(thirdRow);

        mainKeyboard.setKeyboard(keyboardRows);

        return mainKeyboard;
    }

    // Main menu
    public ReplyKeyboardMarkup mainMenu() {
        ReplyKeyboardMarkup mainKeyboard = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add(buttons.getMAIN_FIND_CAR());
        firstRow.add(buttons.getMAIN_FIND_FELLOW());
        keyboardRows.add(firstRow);

        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add(buttons.getMAIN_GET_HELP());
        secondRow.add(buttons.getMAIN_GET_USER_DATA());
        keyboardRows.add(secondRow);

        mainKeyboard.setKeyboard(keyboardRows);

        return mainKeyboard;
    }

    public InlineKeyboardMarkup oneButtonsInlineKeyboard(Pair<String, String> buttonsAttributes) {

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> firstRowInLine = new ArrayList<>();
        InlineKeyboardButton firstButton = new InlineKeyboardButton();

        firstButton.setText(buttonsAttributes.getFirst());
        firstButton.setCallbackData(buttonsAttributes.getSecond());
        firstRowInLine.add(firstButton);
        rowsInLine.add(firstRowInLine);

        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }

    // Inline keyboard three buttons - horizontal arrangement
    public InlineKeyboardMarkup twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(List<Pair<String, String>> buttonsAttributes) {

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        if (buttonsAttributes.size() == 3) {
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> firstRow = new ArrayList<>();
            List<InlineKeyboardButton> secondRow = new ArrayList<>();

            InlineKeyboardButton firstButton = new InlineKeyboardButton();
            firstButton.setText(buttonsAttributes.get(0).getFirst());
            firstButton.setCallbackData(buttonsAttributes.get(0).getSecond());

            InlineKeyboardButton secondButton = new InlineKeyboardButton();
            secondButton.setText(buttonsAttributes.get(1).getFirst());
            secondButton.setCallbackData(buttonsAttributes.get(1).getSecond());

            InlineKeyboardButton thirdButton = new InlineKeyboardButton();
            thirdButton.setText(buttonsAttributes.get(2).getFirst());
            thirdButton.setCallbackData(buttonsAttributes.get(2).getSecond());

            firstRow.add(firstButton);
            firstRow.add(secondButton);
            secondRow.add(thirdButton);

            rows.add(firstRow);
            rows.add(secondRow);

            markupInLine.setKeyboard(rows);
        } else {
            log.error("Incorrect List of  buttonsAttributes. Keyboard not created");
            markupInLine = null;
        }
        return markupInLine;
    }

    public InlineKeyboardMarkup dynamicRangeColumnInlineKeyboard(List<Pair<String, String>> buttonsAttributes) {

        InlineKeyboardMarkup inLineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Pair<String, String> pair : buttonsAttributes) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(pair.getFirst());
            button.setCallbackData(pair.getSecond());
            row.add(button);
            rows.add(row);
        }

        inLineKeyboard.setKeyboard(rows);
        return inLineKeyboard;
    }

    public InlineKeyboardMarkup dynamicRangeOneRowInlineKeyboard(List<Pair<String, String>> buttonsAttributes) {

        InlineKeyboardMarkup inLineKeyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (Pair<String, String> pair : buttonsAttributes) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(pair.getFirst());
            button.setCallbackData(pair.getSecond());
            row.add(button);
        }
        rows.add(row);
        inLineKeyboard.setKeyboard(rows);
        return inLineKeyboard;
    }
}


