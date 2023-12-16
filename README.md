# DemoPanDevBot
___

## **Цель:** Создать Telegram-бота, который позволит пользователям создавать, просматривать и удалять дерево категорий

## Основные возможности:
+ Пользователь может посмотреть дерево категории
+ Пользователь может добавлять категорию 
+ Пользователь может добавлять дочерние категории для родительской категории
  ____

## Как использовать проект
### Настройка подключения к базе данных

- application.properties
```java
bot.key=6706873282:AAGxOthiUUwxJXBGHIdQH19J2LeIFS1kfdM
bot.name=DemoPanDevBot
spring.datasource.url=jdbc:postgresql://localhost:5432/PanDevDemoBot
spring.datasource.username=postgres
spring.datasource.password=2805

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```
1. Создать базу данных "PanDevDemoBot"
2. Изменить имя пользователя под вашу реляционную базу данных для `spring.datasource.username`
3. Изменить пароль для `spring.datasource.password`
___
### Настройка токена 
+ Создайте бота через @Botfather 
+ Присвойте токен к `bot.key`

___
# Интерфейс бота и вид дерево категории 
![img](https://github.com/MerkhatM/DemoTelegramBot/blob/master/src/main/resources/img/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%20(23).png?raw=true)
