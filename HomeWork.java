package ru.geekbrains.lesson_4_hw;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

// Так себе пока алгоритм, но если в начале ИИ с ходами повезет, то может побороться :)
// Поле, конечно, должно быть 15х15, но на консоли тяжко номера строк/столбцов углядывать...
//
    //Один раз что-то не то ввел - сбойнул алгоритм (не давал сходить в пустую клетку)
// , но потом не смог повторить, так что не знаю что случилось

public class HomeWork {
    private static final char EMPTY_DOT = '*';
     private static final char HUMAN_DOT = 'X';
     private static final char AI_DOT = 'O';
     private static int fieldSizeX;
     private static int fieldSizeY;
     private static int WIN_LENGTH;
     private static char[][] field;
     private static Scanner sc = new Scanner(System.in);
     private static Random rnd = new Random();
     private static int lastMoveX, lastMoveY;



    public static void main(String[] args) {
        initField(9, 9);
        WIN_LENGTH = 5;
        printField();
        while (true) {
            humanMove();
            if (checkWin(lastMoveX, lastMoveY)){
                System.out.println("Вы победили!");
                printField();
                break;
            }
            printField();
            if (isFieldFull()){
                System.out.println("Ничья!");
                break;
            }
            //aiMoveRnd();
            aiMove();
            if (checkWin(lastMoveX, lastMoveY)){
                System.out.println("Компьютер выиграл!");
                printField();
                break;
            }
            printField();
            if (isFieldFull()) {
                System.out.println("Ничья!");
                break;
            }
        }
        sc.close();

    }

    private static void initField(int x, int y){
        fieldSizeX = x;
        fieldSizeY = y;
        field = new char[fieldSizeX][fieldSizeY];
        for (int i = 0; i < fieldSizeX; i++) {
            for (int j = 0; j < fieldSizeY ; j++) {
                field[i][j] = EMPTY_DOT;
            }
        }
    }

    private static void printField(){
        System.out.print("+ ");
        for (int i = 0; i < fieldSizeX; i++) {
            System.out.print((i+1)+ " ");
        }
        System.out.println();
        for (int i = 0; i < fieldSizeY; i++) {
            System.out.print((i+1) + " ");
            for (int j = 0; j < fieldSizeX; j++) {
                System.out.print(field[j][i] + " ");
            }
            System.out.println();
        }
    }

    private static void humanMove(){
       int x,y;
        do {
           System.out.println("Ваш ход! Введите координаты Х У");
           x = sc.nextInt() - 1;
           y = sc.nextInt() - 1;
       } while (!isEmpty(x ,y));
        field[x][y] = HUMAN_DOT;
        lastMoveX = x;
        lastMoveY = y;
    }

    private static void aiMoveRnd(){
        int x, y;
        do {
            x = rnd.nextInt(fieldSizeX);
            y = rnd.nextInt(fieldSizeY);
        } while (!isEmpty(x, y));
        System.out.println("Компьютер сходил в " + (x + 1) + " " + (y + 1));
        field[x][y] = AI_DOT;
        lastMoveY = y;
        lastMoveX = x;
    }

    private static boolean isEmpty(int x, int y){
        if (isValidPoint(x, y))
            return field[x][y] == EMPTY_DOT;
        else
            return false;
    }

    private static boolean isValidPoint(int x, int y){
        return x >= 0 && x < fieldSizeX && y >= 0 && y < fieldSizeY;
    }

    private static boolean isFieldFull(){
        for (int i = 0; i < fieldSizeX; i++) {
            for (int j = 0; j < fieldSizeY ; j++) {
                if (isEmpty(i, j)) return false;
            }
        }
        return true;
    }

    private static boolean checkWin(int lastMoveX, int lastMoveY, int vectorX, int vectorY){
        char c = field[lastMoveX][lastMoveY];
        int counter = 0;
        int i = 0;
        while (field[lastMoveX + vectorX * i][lastMoveY + vectorY * i] == c) {
            i++;
            counter++;
            if (!isValidPoint(lastMoveX + vectorX * i, lastMoveY + vectorY * i)) break;
        }
        i = 0;
        counter--;
        while (field[lastMoveX + vectorX * i][lastMoveY + vectorY * i] == c) {
            i--;
            counter++;
            if (!isValidPoint(lastMoveX + vectorX * i, lastMoveY + vectorY * i)) break;
        }
        return counter >= WIN_LENGTH;
    }

    private static boolean checkWin(int lastMoveX, int lastMoveY){
        return checkWin(lastMoveX, lastMoveY, 1, 1) || checkWin(lastMoveX, lastMoveY, 1, 0) ||
                checkWin(lastMoveX, lastMoveY, 1, -1) || checkWin(lastMoveX, lastMoveY, 0, 1);
    }

    private static void aiMove(){
        int value;
        ArrayList<Integer> listX = new ArrayList<>();
        ArrayList<Integer> listY = new ArrayList<>();
        int maxRank = 0;
        // при помощи array можно смотреть как комп расставляет приоритеты
       //int[][] array = new int[fieldSizeX][fieldSizeY];

        for (int i = 0; i < fieldSizeX ; i++) {
            for (int j = 0; j < fieldSizeY; j++) {
                if (isEmpty(i, j)) {
                    value = getValue(i, j);
                    //array[i][j] = value;
                    if (value > maxRank){
                        listX.clear();
                        listY.clear();
                        maxRank = value;
                        listX.add(i);
                        listY.add(j);
                    }
                    else if (value == maxRank && value > 0){
                        listX.add(i);
                        listY.add(j);
                    }
                }
            }
        }

//        for (int i = 0; i < fieldSizeY; i++) {
//            for (int j = 0; j < fieldSizeX; j++) {
//                System.out.print(array[j][i] + " ");
//            }
//            System.out.println();
//        }

        if (maxRank == 0){
            System.out.println("Ничья! Не осталось результативных ходов");
            printField();
            while (!isFieldFull())
                aiMoveRnd();
            return;
        }
        int move = rnd.nextInt(listX.size());

//        for (int i = 0; i < listX.size(); i++) {
//            System.out.println(listX.get(i) + " " + listY.get(i));
//        }

        lastMoveX = listX.get(move);
        lastMoveY = listY.get(move);
        field[lastMoveX][lastMoveY] = AI_DOT;
        System.out.println("Компьютер сходил в " + (lastMoveX + 1) + " " + (lastMoveY + 1));
    }

    private static int getValue(int x, int y){
        int[] rank = {getValueVector(x, y, 1, 1), getValueVector(x, y, 1, 0),
                getValueVector(x, y, 1, -1), getValueVector(x, y, 0, -1)};
        int max = rank[0];
        int maxCount = 0;
        for (int i = 1; i < rank.length ; i++) {
            max = rank[i] > max ? rank[i] : max;
        }
        for (int i = 0; i < rank.length ; i++)
            if (max == rank[i]) maxCount++;
        if (max > 6) return max;
        else if (max == 6 && maxCount > 1) return 8;
        else if (max == 6) return 7;
        else if (max == 5 && maxCount > 1) return 6;
        else return max;
    }

    private static int getValueVector(char c, int x, int y, int vX, int vY){
        int length = 1;
        int i = 1;
        int fr = 0;
        int fl = 0;
        while (isValidPoint(x + i*vX, y + i*vY)){
            if (field[x + i*vX][y + i*vY] == c)
                if (fr == 0)
                    length++;
                else
                    fr++;
            else if (isEmpty(x + i*vX, y + i*vY)) fr++;
            else break;
            i++;
        }
        i = -1;
            while (isValidPoint(x + i*vX, y + i*vY)){
            if (field[x + i*vX][y + i*vY] == c)
                if (fl == 0)
                    length++;
                else
                    fl++;
            else if (isEmpty(x + i*vX, y + i*vY)) fl++;
            else break;
            i--;
        }
        if (length >= WIN_LENGTH) return 10;
        else if (length == WIN_LENGTH - 1 && fr > 0 && fl > 0) return 9;
        else if (length == WIN_LENGTH - 1 && (fr > 0 || fl > 0)) return 3;
        else if (length == WIN_LENGTH - 2 && fr > 0 && fl > 0) return 3;
        else if (length == WIN_LENGTH - 2 && (fr > 0 || fl > 0)) return 2;
        else if (length + fr + fl >= WIN_LENGTH) return 1;
        else return 0;
        }

    private static int getValueVector (int x, int y, int vX, int vY){
        return Math.max(getValueVector(AI_DOT, x, y, vX, vY)*2,getValueVector(HUMAN_DOT, x, y, vX, vY)*2 - 1);
    }



}
