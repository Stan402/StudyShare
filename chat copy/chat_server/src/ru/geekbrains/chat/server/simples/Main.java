package ru.geekbrains.chat.server.simples;

public class Main {

    public static void main(String[] args) {
        try {
            Class.forName("ru.geekbrains.chat.server.simples.ExampleClass");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}