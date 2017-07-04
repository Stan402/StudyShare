package ru.geekbrains.chat.server.core;

import ru.geekbrains.chat.library.Messages;
import ru.geekbrains.network.SocketThread;
import ru.geekbrains.network.SocketThreadListener;

import java.net.Socket;

class ChatSocketThread extends SocketThread {

    private boolean isAuthorized;
    private String nick;

    ChatSocketThread(SocketThreadListener eventListener, String name, Socket socket) {
        super(eventListener, name, socket);
    }

    void authorizeAccept(String nick) {
        this.isAuthorized = true;
        this.nick = nick;
        sendMsg(Messages.getAuthAccept(nick));
    }

    boolean isAuthorized() {
        return isAuthorized;
    }

    void authError() {
        sendMsg(Messages.getAuthError());
        close();
    }

    void messageFormatError(String msg) {
        sendMsg(Messages.getMsgFormatError(msg));
        close();
    }

    String getNick() {
        return nick;
    }
}
