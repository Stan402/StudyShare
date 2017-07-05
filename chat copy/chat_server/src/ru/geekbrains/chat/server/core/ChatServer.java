package ru.geekbrains.chat.server.core;

import ru.geekbrains.chat.library.Messages;
import ru.geekbrains.network.ServerSocketThread;
import ru.geekbrains.network.ServerSocketThreadListener;
import ru.geekbrains.network.SocketThread;
import ru.geekbrains.network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListener, SocketThreadListener {

    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss: ");
    private final ChatServerListener eventListener;
    private final SecurityManager securityManager;
    private ServerSocketThread serverSocketThread;
    private final Vector<SocketThread> clients = new Vector<>();

    public ChatServer(ChatServerListener eventListener, SecurityManager securityManager) {
        this.eventListener = eventListener;
        this.securityManager = securityManager;
    }

    public void startListening(int port) {
        if(serverSocketThread != null && serverSocketThread.isAlive()) {
            putLog("Сервер уже запущен.");
            return;
        }
        serverSocketThread = new ServerSocketThread(this, "ServerSocketThread", port, 2000);
        securityManager.init();
    }

    public void dropAllClients() {
        putLog("dropAllClients");
    }

    public void stopListening() {
        if(serverSocketThread == null || !serverSocketThread.isAlive()) {
            putLog("Сервер не запущен.");
            return;
        }
        serverSocketThread.interrupt();
        securityManager.dispose();
    }

    //ServerSocketThread
    @Override
    public void onStartServerSocketThread(ServerSocketThread thread) {
        putLog("started...");
    }

    @Override
    public void onStopServerSocketThread(ServerSocketThread thread) {
        putLog("stopped.");
    }

    @Override
    public void onReadyServerSocketThread(ServerSocketThread thread, ServerSocket serverSocket) {
        putLog("ServerSocket is ready...");
    }

    @Override
    public void onTimeOutAccept(ServerSocketThread thread, ServerSocket serverSocket) {
//        putLog("accept() timeout");
    }

    @Override
    public void onAcceptedSocket(ServerSocketThread thread, ServerSocket serverSocket, Socket socket) {
        putLog("Client connected: " + socket);
        String threadName = "Socket thread: " + socket.getInetAddress() + ":" + socket.getPort();
        new ChatSocketThread(this, threadName, socket);
    }

    @Override
    public void onExceptionServerSocketThread(ServerSocketThread thread, Exception e) {
        putLog("Exception: " + e.getClass().getName() + ": " + e.getMessage());
    }

    private synchronized void putLog(String msg) {
        String msgLog = dateFormat.format(System.currentTimeMillis()) +
                Thread.currentThread().getName() + ": " + msg;
        eventListener.onChatServerLog(this, msgLog);
    }

    //SocketThread
    @Override
    public synchronized void onStartSocketThread(SocketThread socketThread) {
        putLog("started...");
    }

    @Override
    public synchronized void onStopSocketThread(SocketThread socketThread) {
        clients.remove(socketThread);
        putLog("stopped.");
        ChatSocketThread client = (ChatSocketThread) socketThread;
        if(client.isAuthorized()) {
            sendToAllAuthorizedClients(Messages.getBroadcast("Server", client.getNick() + " disconnected."));
        }
        updateUserList();
    }

    @Override
    public synchronized void onReadySocketThread(SocketThread socketThread, Socket socket) {
        putLog("Socket is ready.");
        clients.add(socketThread);
        updateUserList();
    }

    private void updateUserList(){
        String users = "";
        for (int i = 0; i < clients.size(); i++) {
            ChatSocketThread client = (ChatSocketThread)clients.get(i);
            users += client.getNick() + Messages.DELIMITER;
        }
        users = users.trim();
        sendToAllAuthorizedClients(Messages.getUsersList(users));
    }

    @Override
    public synchronized void onReceiveString(SocketThread socketThread, Socket socket, String value) {
        ChatSocketThread client = (ChatSocketThread) socketThread;
        if(client.isAuthorized()) {
            handleAuthorizeClient(client, value);
        } else {
            handleNonAuthorizeClient(client, value);
        }
    }

    private void handleAuthorizeClient(ChatSocketThread client, String msg){
        sendToAllAuthorizedClients(Messages.getBroadcast(client.getNick(), msg));
    }

    private void sendToAllAuthorizedClients(String msg) {
        for (int i = 0; i < clients.size(); i++) {
            ChatSocketThread client = (ChatSocketThread) clients.get(i);
            if(client.isAuthorized()) client.sendMsg(msg);
        }
    }

    private void handleNonAuthorizeClient(ChatSocketThread client, String msg){
        putLog("auth msg: '" + msg + "'");
        String[] tokens = msg.split(Messages.DELIMITER);
        if(tokens.length != 3 || !tokens[0].equals(Messages.AUTH_REQUEST)) {
            client.messageFormatError(msg);
            return;
        }
        String login = tokens[1];
        String password = tokens[2];
        String nickname = securityManager.getNick(login, password);
        if(nickname == null) {
            client.authError();
            return;
        }
        client.authorizeAccept(nickname);
        putLog(nickname + " connected.");
        sendToAllAuthorizedClients(Messages.getBroadcast("Server", client.getNick() + " connected."));
    }

    @Override
    public synchronized void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {
        putLog("Exception: " + e.getClass().getName() + ": " + e.getMessage());
    }
}
