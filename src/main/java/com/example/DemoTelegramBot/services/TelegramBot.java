package com.example.DemoTelegramBot.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    static final String HELP_MSQ= "Available commands:\n\n"+
            "Type /start to see a welcome message\n\n"+
            "Type /mydata to see data about yourself\n\n"+
            "Type /deletedata to delete stored data abou youself\n\n"+
            "Type /help to see this message again\n\n";

    public TelegramBot(@Value("${bot.key}") String botToken) {
        super(botToken);
        List<BotCommand> list=new ArrayList<>();
        list.add(new BotCommand("/start","get a welcome message"));
        list.add(new BotCommand("/mydata","get your data"));
        list.add(new BotCommand("/help","get info how to use bot"));
        list.add(new BotCommand("/settings","set your preferences"));
        try {
            this.execute(new SetMyCommands(list,new BotCommandScopeDefault(),null));
        }catch (TelegramApiException e){
            log.error("Error, settings bot's command list: "+ e.getMessage());
        }
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
                case "/help":
                    startCommandReceived(chatId,HELP_MSQ);
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
        log.info("Replied to user "+name);
    }

    public void sendMessage(long chatId, String textToSend)  {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(textToSend);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("ERROR OCCURED: "+e.getMessage());
        }
    }
}
