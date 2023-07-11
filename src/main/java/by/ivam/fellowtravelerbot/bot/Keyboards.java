package by.ivam.fellowtravelerbot.bot;
/*
Main menu and inline keyboards
 */

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
@Data
public class Keyboards {
    @Autowired
    Buttons buttons;

// Main menu

    ReplyKeyboardMarkup mainAdminMenu() {
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

    ReplyKeyboardMarkup mainMenu() {
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

    //    Inline keyboard one buttons
    public InlineKeyboardMarkup oneButtonsInlineKeyboard(String firstButtonText, String firstButtonCallbackData) {

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> firstRowInLine = new ArrayList<>();
        InlineKeyboardButton firstButton = new InlineKeyboardButton();

        firstButton.setText(firstButtonText);
        firstButton.setCallbackData(firstButtonCallbackData);
        firstRowInLine.add(firstButton);
        rowsInLine.add(firstRowInLine);

        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }

    // Inline keyboard two buttons - horizontal arrangement
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


    // Inline keyboard three buttons - horizontal arrangement
    public InlineKeyboardMarkup threeButtonsInlineKeyboard(String firstButtonText, String firstButtonCallbackData, String secondButtonText, String secondButtonCallbackData, String thirdButtonText, String thirdButtonCallbackData) {

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> firstRowInLine = new ArrayList<>();
        InlineKeyboardButton firstButton = new InlineKeyboardButton();

        firstButton.setText(firstButtonText);
        firstButton.setCallbackData(firstButtonCallbackData);

        InlineKeyboardButton secondButton = new InlineKeyboardButton();

        secondButton.setText(secondButtonText);
        secondButton.setCallbackData(secondButtonCallbackData);

        InlineKeyboardButton thirdButton = new InlineKeyboardButton();

        thirdButton.setText(thirdButtonText);
        thirdButton.setCallbackData(thirdButtonCallbackData);

        firstRowInLine.add(firstButton);
        firstRowInLine.add(secondButton);
        firstRowInLine.add(thirdButton);

        rowsInLine.add(firstRowInLine);

        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }


    //    Inline keyboard two buttons - vertical arrangement
    public InlineKeyboardMarkup twoButtonsColumnInlineKeyboard(String firstButtonText, String firstButtonCallbackData, String secondButtonText, String secondButtonCallbackData) {

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> firstRowInLine = new ArrayList<>();
        List<InlineKeyboardButton> secondRowInLine = new ArrayList<>();
        InlineKeyboardButton firstButton = new InlineKeyboardButton();

        firstButton.setText(firstButtonText);
        firstButton.setCallbackData(firstButtonCallbackData);

        InlineKeyboardButton secondButton = new InlineKeyboardButton();

        secondButton.setText(secondButtonText);
        secondButton.setCallbackData(secondButtonCallbackData);

        firstRowInLine.add(firstButton);
        secondRowInLine.add(secondButton);

        rowsInLine.add(firstRowInLine);
        rowsInLine.add(secondRowInLine);

        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }

    //    Inline keyboard tree buttons - vertical arrangement
    public InlineKeyboardMarkup treeButtonsColumnInlineKeyboard(String firstButtonText, String firstButtonCallbackData, String secondButtonText, String secondButtonCallbackData, String thirdButtonText, String thirdButtonCallbackData) {

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> firstRowInLine = new ArrayList<>();
        List<InlineKeyboardButton> secondRowInLine = new ArrayList<>();
        List<InlineKeyboardButton> thirdRowInLine = new ArrayList<>();

        InlineKeyboardButton firstButton = new InlineKeyboardButton();
        firstButton.setText(firstButtonText);
        firstButton.setCallbackData(firstButtonCallbackData);

        InlineKeyboardButton secondButton = new InlineKeyboardButton();
        secondButton.setText(secondButtonText);
        secondButton.setCallbackData(secondButtonCallbackData);

        InlineKeyboardButton thirdButton = new InlineKeyboardButton();
        thirdButton.setText(thirdButtonText);
        thirdButton.setCallbackData(thirdButtonCallbackData);

        firstRowInLine.add(firstButton);
        secondRowInLine.add(secondButton);
        thirdRowInLine.add(thirdButton);

        rowsInLine.add(firstRowInLine);
        rowsInLine.add(secondRowInLine);
        rowsInLine.add(thirdRowInLine);


        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }

    //    Inline keyboard four buttons - vertical arrangement
    public InlineKeyboardMarkup fourButtonsColumnInlineKeyboard(String firstButtonText, String firstButtonCallbackData, String secondButtonText, String secondButtonCallbackData, String thirdButtonText, String thirdButtonCallbackData, String fourthButtonText, String fourthButtonCallbackData) {

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> firstRowInLine = new ArrayList<>();
        List<InlineKeyboardButton> secondRowInLine = new ArrayList<>();
        List<InlineKeyboardButton> thirdRowInLine = new ArrayList<>();
        List<InlineKeyboardButton> fourthRowInLine = new ArrayList<>();

        InlineKeyboardButton firstButton = new InlineKeyboardButton();
        firstButton.setText(firstButtonText);
        firstButton.setCallbackData(firstButtonCallbackData);

        InlineKeyboardButton secondButton = new InlineKeyboardButton();
        secondButton.setText(secondButtonText);
        secondButton.setCallbackData(secondButtonCallbackData);

        InlineKeyboardButton thirdButton = new InlineKeyboardButton();
        thirdButton.setText(thirdButtonText);
        thirdButton.setCallbackData(thirdButtonCallbackData);

        InlineKeyboardButton fourthButton = new InlineKeyboardButton();
        fourthButton.setText(fourthButtonText);
        fourthButton.setCallbackData(fourthButtonCallbackData);

        firstRowInLine.add(firstButton);
        secondRowInLine.add(secondButton);
        thirdRowInLine.add(thirdButton);
        fourthRowInLine.add(fourthButton);

        rowsInLine.add(firstRowInLine);
        rowsInLine.add(secondRowInLine);
        rowsInLine.add(thirdRowInLine);
        rowsInLine.add(fourthRowInLine);

        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }

    //    Inline keyboard five buttons - vertical arrangement
    public InlineKeyboardMarkup fiveButtonsColumnInlineKeyboard(String firstButtonText, String firstButtonCallbackData, String secondButtonText, String secondButtonCallbackData, String thirdButtonText, String thirdButtonCallbackData, String fourthButtonText, String fourthButtonCallbackData, String fifthButtonText, String fifthButtonCallbackData) {

        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> firstRowInLine = new ArrayList<>();
        List<InlineKeyboardButton> secondRowInLine = new ArrayList<>();
        List<InlineKeyboardButton> thirdRowInLine = new ArrayList<>();
        List<InlineKeyboardButton> fourthRowInLine = new ArrayList<>();
        List<InlineKeyboardButton> fifthRowInLine = new ArrayList<>();

        InlineKeyboardButton firstButton = new InlineKeyboardButton();
        firstButton.setText(firstButtonText);
        firstButton.setCallbackData(firstButtonCallbackData);

        InlineKeyboardButton secondButton = new InlineKeyboardButton();
        secondButton.setText(secondButtonText);
        secondButton.setCallbackData(secondButtonCallbackData);

        InlineKeyboardButton thirdButton = new InlineKeyboardButton();
        thirdButton.setText(thirdButtonText);
        thirdButton.setCallbackData(thirdButtonCallbackData);

        InlineKeyboardButton fourthButton = new InlineKeyboardButton();
        fourthButton.setText(fourthButtonText);
        fourthButton.setCallbackData(fourthButtonCallbackData);

        InlineKeyboardButton fifthButton = new InlineKeyboardButton();
        fifthButton.setText(fifthButtonText);
        fifthButton.setCallbackData(fifthButtonCallbackData);

        firstRowInLine.add(firstButton);
        secondRowInLine.add(secondButton);
        thirdRowInLine.add(thirdButton);
        fourthRowInLine.add(fourthButton);
        fifthRowInLine.add(fifthButton);

        rowsInLine.add(firstRowInLine);
        rowsInLine.add(secondRowInLine);
        rowsInLine.add(thirdRowInLine);
        rowsInLine.add(fourthRowInLine);
        rowsInLine.add(fifthRowInLine);

        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }

}
