package ru.geekbrains.chat.server.core;

public interface ChatServerListener {

    void onChatServerLog (ChatServer chatServer, String msg);
}
