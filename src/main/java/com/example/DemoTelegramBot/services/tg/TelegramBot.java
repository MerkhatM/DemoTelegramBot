package com.example.DemoTelegramBot.services.tg;

import com.example.DemoTelegramBot.models.BotState;
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
import java.util.*;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private TelegramUserService telegramUserService;
    @Autowired
    private CategoryService categoryService;
    private Map<Long, BotState> userStates = new HashMap<>();
    static final String HELP_MSQ = "Доступные команды:\n\n" +
            "/start - получить приветствие и заргеистрировать пользователя \n\n" +
            "/view_tre - посмотреть список категории\n\n" +
            "/add_element - добавить категорию в корневую\n\n" +
            "/add_child_element - добавить дочернюю категорию в родительскии. Введите название родительского и дочернего элементов через пробел\n\n" +
            "/remove_element - удалить категорию. Если у категории есть дочерние, они тоже удалятся\n\n" +
            "/help - посмотреть это сообщение еще раз\n\n";

    public TelegramBot(@Value("${bot.key}") String botToken) {
        super(botToken);
        List<BotCommand> lists = new ArrayList<>();
        lists.add(new BotCommand("/start", "Приветствие"));
        lists.add(new BotCommand("/view_tree", "Посмотреть дерево категории"));
        lists.add(new BotCommand("/add_element", "Добавить категорию "));
        lists.add(new BotCommand("/add_child_element", "Добавить дочернюю категорию для родителя"));
        lists.add(new BotCommand("/remove_element", "Удалить категорию"));
        lists.add(new BotCommand("/help", "Посмотреть список доступных команд"));
        try {
            this.execute(new SetMyCommands(lists, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error, settings bot's command list: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    registerUser(update.getMessage());
                    break;
                case "/view_tree":
                    sendCategoryTree(chatId, categoryService.getTree());
                    break;
                case "/add_element":
                    userStates.put(chatId,BotState.WAITING_FOR_ELEMENT_NAME);
                    sendMessage(chatId, "Введите название элемента:");
                    break;
                case "/add_child_element":
                    userStates.put(chatId, BotState.WAITING_FOR_CHILD_ELEMENT_NAMES);
                    sendMessage(chatId, "Введите название родительского и дочернего элементов через пробел:");
                    break;
                case "/remove_element":
                    userStates.put(chatId,BotState.WAITING_FOR_REMOVE_ELEMENT);
                    sendMessage(chatId, "Введите название элемента, который хотите удалить:");
                    break;
                case "/help":
                    startCommandReceived(chatId, HELP_MSQ);
                default:
                    BotState currentState = userStates.getOrDefault(chatId, BotState.NORMAL);
                    switch (currentState) {
                        case WAITING_FOR_ELEMENT_NAME:
                            handleAddElementCommand(chatId, messageText);
                            break;
                        case WAITING_FOR_CHILD_ELEMENT_NAMES:
                            handleAddChildElementCommand(chatId,messageText);
                            break;
                        case WAITING_FOR_REMOVE_ELEMENT:
                            handleRemoveElementCommand(chatId, messageText);
                            break;
                        default:
                            sendMessage(chatId, "Sorry, command was not recognized");
                    }
            }
        }
    }


    private void registerUser(Message message) {
        if ((telegramUserService.getUser(message.getChatId())).isEmpty()) {
            var chatid = message.getChatId();
            var chat = message.getChat();
            TelegramUser telegramUser = new TelegramUser();

            telegramUser.setChatId(chatid);
            telegramUser.setFirstName(chat.getFirstName());
            telegramUser.setLastName(chat.getLastName());
            telegramUser.setUserName(chat.getUserName());
            telegramUser.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            telegramUserService.addUser(telegramUser);
            log.info("User saved " + telegramUser);
        }
    }


    @Override
    public String getBotUsername() {
        return "DemoPanDevBot";
    }

    public void startCommandReceived(long chatId, String name) {
        String answer="";
        if (name.equals(HELP_MSQ)){
            answer=name ;
        }
        else
         answer = "HI,  " + name + ", nice to meet you!";
        sendMessage(chatId, answer);
        log.info("Replied to user " + name);
    }

    public void sendMessage(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(textToSend);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("ERROR OCCURED: " + e.getMessage());
        }
    }

    public void sendCategoryTree(Long chatId, List<Category> categories) {
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

    public void handleAddElementCommand(Long chatId, String messageText) {
        Category newCategory = new Category();
        newCategory.setName(messageText);
        newCategory.setParent(null);
        categoryService.addCategory(newCategory);
        userStates.put(chatId, BotState.NORMAL);

        sendMessage(chatId, "Элемент успешно добавлен.");
    }

    private void handleAddChildElementCommand(Long chatId, String messageText) {
        String[] commandParts = messageText.split(" ");
        if (commandParts.length < 2) {
            sendMessage(chatId, "Некорректная команда. Используйте: /addChildElement <Родитель> <Дочерний>");
            return;
        }

        String parentName = commandParts[0];
        String childName = commandParts[1];

        Category parentCategory = categoryService.getCategoryByName(parentName);
        if (parentCategory != null) {
            Category newChild = new Category();
            newChild.setName(childName);
            newChild.setParent(parentCategory);
            categoryService.addCategory(newChild);
            sendMessage(chatId, "Дочерний элемент успешно добавлен.");
        } else {
            sendMessage(chatId, "Родительская категория не найдена.");
        }
        userStates.put(chatId, BotState.NORMAL);

    }

    public void handleRemoveElementCommand(Long chatId, String messageText) {
        Category categoryToRemove = categoryService.getCategoryByName(messageText);
        if (categoryToRemove != null) {
            categoryService.deleteCategoryAndChildrem(categoryToRemove.getId());
            sendMessage(chatId, "Элемент и все его дочерние элементы успешно удалены.");
        } else {
            sendMessage(chatId, "Элемент не найден.");
        }
        userStates.put(chatId, BotState.NORMAL);
    }
}


