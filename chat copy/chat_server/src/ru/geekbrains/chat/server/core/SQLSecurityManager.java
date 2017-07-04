package ru.geekbrains.chat.server.core;

import java.sql.*;

public class SQLSecurityManager implements SecurityManager {

    private Connection connection;
    private Statement statement;

    @Override
    public void init() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:chat_db.sqlite");
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getNick(String login, String password) {
        String request = "SELECT nickname FROM users WHERE login='" +
                login + "' AND password='" + password + "'";
        try (ResultSet resultSet = statement.executeQuery(request)) {
            if (resultSet.next()) {
                return resultSet.getString(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void dispose() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
