package com.example.DemoTelegramBot.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component

public class TelegramBot extends TelegramLongPollingBot {


    public TelegramBot(@Value("${bot.key}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText=update.getMessage().getText();
            long chatId=update.getMessage().getChatId();

            switch (messageText){
                case "/start":
                    startCommandReceived(chatId,update.getMessage().getChat().getFirstName());
                    break;
                default:
                    sendMessage(chatId,"Sorry, command was not recognized");

            }
            }
        }


    @Override
    public String getBotUsername() {
        return "DemoPanDevBot";
    }

    public void startCommandReceived(long chatId,String name)  {
        String answer = "HI,  " + name + ", nice to meet you!";
        sendMessage(chatId,answer);
    }

    public void sendMessage(long chatId, String textToSend)  {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(textToSend);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {

        }
    }
}
