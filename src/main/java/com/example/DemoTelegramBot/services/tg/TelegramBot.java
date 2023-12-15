package com.example.DemoTelegramBot.services.tg;

import com.example.DemoTelegramBot.models.Category;
import com.example.DemoTelegramBot.models.TelegramUser;
import com.example.DemoTelegramBot.repositories.TelegramUserRepository;
import com.example.DemoTelegramBot.services.CategoryService;
import com.example.DemoTelegramBot.services.TelegramUserService;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.grizzly.http.util.TimeStamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private TelegramUserService telegramUserService;
    @Autowired
    private CategoryService categoryService;
    static final String HELP_MSQ= "Available commands:\n\n"+
            "Type /start to see a welcome message\n\n"+
            "Type /viewTree to see data about yourself\n\n"+
            "Type /addElement to delete stored data abou youself\n\n"+
            "Type /addChildElement to delete stored data abou youself\n\n"+
            "Type /help to see this message again\n\n";

    public TelegramBot(@Value("${bot.key}") String botToken) {
        super(botToken);
        List<BotCommand> lists=new ArrayList<>();
        lists.add(new BotCommand("/start","get a welcome message"));
        lists.add(new BotCommand("/viewTree","view tree"));
        lists.add(new BotCommand("/addElement","get info how to use bot"));
        lists.add(new BotCommand("/addChildElement","get info how to use bot"));
        lists.add(new BotCommand("/removeElement","set your preferences"));
        lists.add(new BotCommand("/help","set your preferences"));
        try {
            this.execute(new SetMyCommands(lists,new BotCommandScopeDefault(),null));
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
                    registerUser(update.getMessage());
                    break;
                case "/viewTree":
                    sendCategoryTree(chatId,categoryService.getTree());
                    break;
                case "/addElement":
                    break;
                case "/addChildElement":
                    break;
                case "removeElement":
                    break;
                case "/help":
                    startCommandReceived(chatId,HELP_MSQ);
                default:
                    sendMessage(chatId,"Sorry, command was not recognized");

            }
            }
        }

    private void registerUser(Message message) {
        if((telegramUserService.getUser(message.getChatId())).isEmpty()){
            var chatid=message.getChatId();
            var chat=message.getChat();
            TelegramUser telegramUser=new TelegramUser();

            telegramUser.setChatId(chatid);
            telegramUser.setFirstName(chat.getFirstName());
            telegramUser.setLastName(chat.getLastName());
            telegramUser.setUserName(chat.getUserName());
            telegramUser.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            telegramUserService.addUser(telegramUser);
            log.info("User saved "+ telegramUser);
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

    public void sendCategoryTree(Long chatId, List<Category> categories){
        StringBuilder message = new StringBuilder("ДЕРЕВО КАТЕГОРИИ:\n");
        Set<Long> printedCategories = new HashSet<>();
        formatCategoryTree(message, categories, 0, printedCategories);
        sendMessage(chatId, String.valueOf(message));
    }

    public void formatCategoryTree(StringBuilder message, List<Category> categories, int level, Set<Long> printedCategories) {
        for (Category category : categories) {
            if (printedCategories.add(category.getId())) {
                for (int i = 0; i < level; i++) {
                    message.append("    ");
                }
                message.append("-- ").append(category.getName()).append("\n");
                List<Category> children = categoryService.findChildrenByParentId(category.getId());
                if (!children.isEmpty()) {
                    formatCategoryTree(message, children, level + 1, printedCategories);
                }
            }
        }
    }

}
