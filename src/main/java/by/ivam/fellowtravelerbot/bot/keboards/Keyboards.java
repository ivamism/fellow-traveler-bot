package by.ivam.fellowtravelerbot.bot.keboards;
/*
Main menu and inline keyboards
 */

import by.ivam.fellowtravelerbot.model.DepartureLocation;
import by.ivam.fellowtravelerbot.model.Settlement;
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
import java.util.stream.Collectors;

@Component
@Data
@Log4j
public class Keyboards {
    @Autowired
    Buttons buttons;

    private List<Pair<String, String>> buttonsAttributesList = new ArrayList<>();

// Main menu

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

    // Inline keyboard three buttons - horizontal arrangement
    public InlineKeyboardMarkup twoButtonsFirstRowOneButtonSecondRowInlineKeyboard(List<Pair<String, String>> buttonsAttributes) {

            InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
            if (buttonsAttributes.size()==3) {
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
            }
            else {
                log.error("Incorrect List of  buttonsAttributes. Keyboard not created");
                markupInLine = null;
            }
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
    public List<Pair<String, String>> settlementsButtonsAttributesListCreator(List<Settlement> settlements, String callbackData) {

        List<Pair<String, String>> buttonsAttributes = settlements
                .stream()
                .map(settlement -> Pair.of(settlement.getName(), callbackData + settlement.getId()))
                .collect(Collectors.toList());
        buttonsAttributes.add(Pair.of(buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));

        return buttonsAttributes;
    }
    public List<Pair<String, String>> departureLocationListButtonsAttributesCreator(List<DepartureLocation> departureLocations) {

        List<Pair<String, String>> buttonsAttributes = departureLocations
                .stream()
                .map(location -> Pair.of(location.getName(), buttons.getADD_LOCATION_GET_SETTLEMENT_CALLBACK() + location.getId()))
                .collect(Collectors.toList());
        buttonsAttributes.add(Pair.of(buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));

        return buttonsAttributes;
    }

    public List<Pair<String, String>> ListButtonsAttributesCreator(List<DepartureLocation> departureLocations) {

        List<Pair<String, String>> buttonsAttributes = departureLocations
                .stream()
                .map(location -> Pair.of(location.getName(), buttons.getADD_LOCATION_GET_SETTLEMENT_CALLBACK() + location.getId()))
                .collect(Collectors.toList());
        buttonsAttributes.add(Pair.of(buttons.getCANCEL_BUTTON_TEXT(), buttons.getCANCEL_CALLBACK()));

        return buttonsAttributes;
    }

    public Pair<String, String> buttonAttributesPairCreator(String buttonName, String buttonCallback) {
        return Pair.of(buttonName, buttonCallback);
    }

    public List<Pair<String, String>> buttonAttributesPairsListCreator(Pair<String, String> buttonAttributes) {
        buttonsAttributesList.add(buttonAttributes);
        return buttonsAttributesList;
    }

    public List<Pair<String, String>> buttonAttributesPairsListCreator(List<Pair<String, String>> buttonsAttributesPairsList, Pair<String, String> buttonAttributes) {
        buttonsAttributesPairsList.add(buttonAttributes);
        return buttonsAttributesPairsList;
    }
}

