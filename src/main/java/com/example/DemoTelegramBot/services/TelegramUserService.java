package com.example.DemoTelegramBot.services;

import com.example.DemoTelegramBot.models.TelegramUser;
import com.example.DemoTelegramBot.repositories.TelegramUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TelegramUserService {
    @Autowired
    private TelegramUserRepository telegramUserRepository;

    public Optional<TelegramUser> getUser(Long chatId) {
        return telegramUserRepository.findById(chatId);
    }

    public void addUser(TelegramUser telegramUser) {
        telegramUserRepository.save(telegramUser);
    }
}
