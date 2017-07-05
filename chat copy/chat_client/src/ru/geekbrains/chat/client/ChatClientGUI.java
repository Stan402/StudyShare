package ru.geekbrains.chat.client;

import ru.geekbrains.chat.library.Messages;
import ru.geekbrains.network.SocketThread;
import ru.geekbrains.network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;

public class ChatClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatClientGUI();
            }
        });
    }

    private static final int WIDTH = 800;
    private static final int HEIGHT = 300;
    private static final String TITLE = "Chat client";

    private final JPanel upperPanel = new JPanel(new GridLayout(2, 3));
    private final JTextField fieldIPAddr = new JTextField("89.222.249.131");
    private final JTextField fieldPort = new JTextField("8189");
    private final JCheckBox chkAlwaysOnTop = new JCheckBox("Always on top", true);
    private final JTextField fieldLogin = new JTextField("login_1");
    private final JPasswordField fieldPass = new JPasswordField("pass_1");
    private final JButton btnLogin = new JButton("Login");

    private final JTextArea log = new JTextArea();
    private final JList<String> userList = new JList<>();

    private final JPanel bottomPanel = new JPanel(new BorderLayout());
    private final JButton btnDisconnect = new JButton("Disconnect");
    private final JTextField fieldInput = new JTextField();
    private final JButton btnSend = new JButton("Send");

    private ChatClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setTitle(TITLE);
        setLocationRelativeTo(null);

        upperPanel.add(fieldIPAddr);
        upperPanel.add(fieldPort);
        upperPanel.add(chkAlwaysOnTop);
        upperPanel.add(fieldLogin);
        upperPanel.add(fieldPass);
        upperPanel.add(btnLogin);
        add(upperPanel, BorderLayout.NORTH);

        log.setEditable(false);
        log.setLineWrap(true);
        JScrollPane scrollLog = new JScrollPane(log);
        add(scrollLog, BorderLayout.CENTER);
        JScrollPane scrollUsers = new JScrollPane(userList);
        scrollUsers.setPreferredSize(new Dimension(150, 0));
        add(scrollUsers, BorderLayout.EAST);

        bottomPanel.add(btnDisconnect, BorderLayout.WEST);
        bottomPanel.add(fieldInput, BorderLayout.CENTER);
        bottomPanel.add(btnSend, BorderLayout.EAST);
        bottomPanel.setVisible(true);
        add(bottomPanel, BorderLayout.SOUTH);

        fieldIPAddr.addActionListener(this);
        fieldPort.addActionListener(this);
        fieldLogin.addActionListener(this);
        fieldPass.addActionListener(this);
        btnLogin.addActionListener(this);
        btnDisconnect.addActionListener(this);
        fieldInput.addActionListener(this);
        btnSend.addActionListener(this);
        chkAlwaysOnTop.addActionListener(this);

        setAlwaysOnTop(chkAlwaysOnTop.isSelected());
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (    src == fieldIPAddr ||
                src == fieldPort ||
                src == fieldLogin ||
                src == fieldPass ||
                src == btnLogin) {
            connect();
        } else if (src == btnDisconnect) {
            disconnect();
        } else if (src == fieldInput || src == btnSend) {
            sendMsg();
        } else if(src == chkAlwaysOnTop) {
            setAlwaysOnTop(chkAlwaysOnTop.isSelected());
        } else {
            throw new RuntimeException("Unknown src = " + src);
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        String msg;
        if (stackTraceElements.length == 0) {
            msg = "Пустой stackTraceElements";
        } else {
            msg = e.getClass().getCanonicalName() + ": " + e.getMessage() + "\n" + stackTraceElements[0];
        }
        JOptionPane.showMessageDialog(null, msg, "Exception: ", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    private SocketThread socketThread;
    private String nick;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

    private void connect() {
        try {
            Socket socket = new Socket(fieldIPAddr.getText(), Integer.parseInt(fieldPort.getText()));
            socketThread = new SocketThread(this, "SocketThread", socket);
        } catch (IOException e) {
            e.printStackTrace();
            log.append("Exception: " + e.getMessage() + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        }
    }

    private void disconnect() {
        socketThread.close();
    }

    private void sendMsg() {
        String msg = fieldInput.getText();
        if(msg.equals("")) return;
        fieldInput.setText(null);
        socketThread.sendMsg(msg);
    }

    @Override
    public void onStartSocketThread(SocketThread socketThread) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append("Поток сокета запущен.\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    @Override
    public void onStopSocketThread(SocketThread socketThread) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append("Соединение потеряно.\n");
                log.setCaretPosition(log.getDocument().getLength());
                upperPanel.setVisible(true);
                bottomPanel.setVisible(false);
            }
        });
    }

    @Override
    public void onReadySocketThread(SocketThread socketThread, Socket socket) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append("Соединение установлено.\n");
                log.setCaretPosition(log.getDocument().getLength());
                upperPanel.setVisible(false);
                bottomPanel.setVisible(true);
                String login = fieldLogin.getText();
                String password = new String(fieldPass.getPassword());
                socketThread.sendMsg(Messages.getAuthRequest(login, password));
            }
        });
    }

    @Override
    public void onReceiveString(SocketThread socketThread, Socket socket, String value) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                handleMsg(value);
            }
        });
    }

    private void handleMsg(String msg) {

        log.append("Received msg: '" + msg + "'\n");
        log.setCaretPosition(log.getDocument().getLength());
        String[] tokens = msg.split(Messages.DELIMITER);
        String msgLog ="";
        switch (tokens[0]){
            case Messages.AUTH_ERROR :
                msgLog = "Invalid username and/or password. Please try again.";
                break;
            case Messages.MSG_FORMAT_ERROR:
                if (tokens.length < 2) {
                    msgLog = "Invalid message format from server. Disconnected.";
                    disconnect();
                } else {
                    msgLog = "Invalid message format: ";
                    for (int i = 1; i < tokens.length ; i++) {
                        msgLog += tokens[i] + Messages.DELIMITER;
                    }
                }
                break;
            case Messages.AUTH_ACCEPT:
                if (tokens.length != 2) {
                    msgLog = "Invalid message format from server. Disconnected.";
                    disconnect();
                } else {
                    nick = tokens[1];
                    msgLog = nick + ", you succesfully connected to chat! \n Welcome aboard!!!";
                }
                break;
            case Messages.BROADCAST:
                if (tokens.length < 4) msgLog = "Invalid message format from server.";
                else {
                    msgLog = String.format("%s %s : ", simpleDateFormat.format(Long.parseLong(tokens[1])), tokens[2]);
                    for (int i = 3; i < tokens.length ; i++) {
                        msgLog += tokens[i] + Messages.DELIMITER;
                    }
                }
                break;
            case Messages.USERS_LIST:
                if (tokens.length < 2){
                    msgLog = "Invalid message format from server. User_list is outdated...";
                }
                else{
                    msgLog = "User_list is updated...";
                    String[] users = new String[tokens.length - 1];
                    System.arraycopy(tokens, 1,users, 0, tokens.length - 1);
                    handleUserList(users);
                }
            default: {
                msgLog = "Invalid message header from server.";
                break;
            }
        }
        log.append(msgLog + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    private void handleUserList(String[] tokens){


        for (int i = 2; i < tokens.length; i++) {

        }
    }

    @Override
    public void onExceptionSocketThread(SocketThread socketThread, Socket socket, Exception e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                e.printStackTrace();
                log.append("Exception: " + e.getMessage() + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }
}
