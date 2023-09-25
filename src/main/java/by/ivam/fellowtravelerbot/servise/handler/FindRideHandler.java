package by.ivam.fellowtravelerbot.servise.handler;


import by.ivam.fellowtravelerbot.DTO.FindPassengerRequestDTO;
import by.ivam.fellowtravelerbot.DTO.FindRideRequestDTO;
import by.ivam.fellowtravelerbot.bot.enums.FindRideOperation;
import by.ivam.fellowtravelerbot.bot.enums.Handlers;
import by.ivam.fellowtravelerbot.model.FindRideRequest;
import by.ivam.fellowtravelerbot.storages.interfaces.FindRideDTOStorageAccess;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;


// This class handle operations with search of hitchhiker's rides
@Service
@Data
@Log4j
public class FindRideHandler extends Handler implements HandlerInterface {
    @Autowired
    private final FindRideDTOStorageAccess storageAccess;
    private final String handlerPrefix = Handlers.FIND_RIDE.getHandlerPrefix();
    SendMessage sendMessage = new SendMessage();
    EditMessageText editMessage = new EditMessageText();

    @Override
    public void handleReceivedMessage(String chatStatus, Message incomeMessage) {
        String messageText = incomeMessage.getText();
        Long chatId = incomeMessage.getChatId();
        log.debug("method handleReceivedMessage. get chatStatus: " + chatStatus + ". message: " + messageText);
        String process = chatStatus;
        if (chatStatus.contains(":")) {
            process = trimProcess(chatStatus);
        }
        switch (process) {

        }
            sendBotMessage(sendMessage);
        }

        @Override
        public void handleReceivedCallback (String callback, Message incomeMessage){
            log.debug("method handleReceivedCallback. get callback: " + callback);
            Long chatId = incomeMessage.getChatId();
            String process = callback;
            if (callback.contains(":")) {
                process = trimProcess(callback);
            }
            switch (process){
                case "CREATE_REQUEST" -> {
                    if (isRequestQuantityLimit(chatId)) editMessage = nextStep(incomeMessage);
//                createNecessityToCancelMessage(incomeMessage);
                    else {
                        createRequestDTO(chatId);
                        editMessage = nextStep(incomeMessage);
//                            createNewRequestChoseDirectionMessage(incomeMessage);
                    }
                }
            }
            sendEditMessage(editMessage);
        }
        public void startCreateNewRequest ( long chatId){
            sendMessage.setChatId(chatId);
            sendMessage.setText(messages.getCREATE_FIND_RIDE_REQUEST_START_PROCESS_MESSAGE());
            List<Pair<String, String>> buttonsAttributesList = new ArrayList<>(); // List of buttons attributes pairs (text of button name and callback)
            buttonsAttributesList.add(buttons.yesButtonCreate(handlerPrefix + FindRideOperation.CREATE_REQUEST_CALLBACK.getValue())); // Create new request button
            buttonsAttributesList.add(buttons.cancelButtonCreate()); // Cancel button
            sendMessage.setReplyMarkup(keyboards.dynamicRangeOneRowInlineKeyboard(buttonsAttributesList));
            log.debug("method: startCreateNewRequest");
            sendBotMessage(sendMessage);
        }

        private boolean isRequestQuantityLimit (long chatId){
            return getUserActiveRequestsList(chatId).size()>2;
        }
        private List<FindRideRequest> getUserActiveRequestsList(long chatId) {
            return findRideRequestService.usersActiveRequestList(chatId);
        }
    private void createRequestDTO(long chatId) {
        FindPassengerRequestDTO findPassengerRequestDTO = new FindPassengerRequestDTO();
        FindRideRequestDTO requestDTO = new FindRideRequestDTO();
        requestDTO.setUser(userService.findUserById(chatId));
        storageAccess.addFindRideDTO(chatId, requestDTO);
        log.debug("method: createFindPassengerRequestDTO - create DTO " + findPassengerRequestDTO + " and save it in storage");
    }
        public void editMessageTextGeneralPreset (Message incomeMessage){
            editMessage.setChatId(incomeMessage.getChatId());
            editMessage.setMessageId(incomeMessage.getMessageId());
        }
    private SendMessage nextStep(long chatId) {
//        TODO удалить метод по окончании реализации всего функционала
        sendMessage.setChatId(chatId);
        sendMessage.setText("nextStep");
        sendMessage.setReplyMarkup(null);
        log.debug("method: nextStep");
        return sendMessage;
    }

    private EditMessageText nextStep(Message incomemessage) {
        //        TODO удалить метод по окончании реализации всего функционала
        editMessageTextGeneralPreset(incomemessage);
        editMessage.setText("nextStep");
        editMessage.setReplyMarkup(null);
        log.debug("method: nextStep");
        return editMessage;
    }

    }

