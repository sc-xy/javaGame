package com.xygame;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class TicTacToeClient implements TicTacToeConstants {
    // 服务器配置信息
    private String host = "localhost";
    private int port = 8000;
    private DataInputStream fromServer;
    private DataOutputStream toServer;
    // 游戏配置
    private boolean continueToPlay = true;
    private char myToken = ' ';
    private char otherToken = ' ';
    private char[][] cell = new char[3][3];
    private int myNum = 0;
    private int row = 0;
    private int clown = 0;
    private Scanner input = new Scanner(System.in);
    private int singal = 0;
    private int status = 0;

    public static void main(String[] args) throws IOException {
        TicTacToeClient player = new TicTacToeClient();
        player.closeInput();
    }

    public TicTacToeClient() {
        try {
            // 连接服务器
            Socket player = new Socket(host, port);
            fromServer = new DataInputStream(player.getInputStream());
            toServer = new DataOutputStream(player.getOutputStream());

            // 初始化棋盘
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    cell[i][j] = ' ';
                }
            }

            // 等待玩家
            myNum = fromServer.readInt();
            if (myNum == PLAYER1) {
                myToken = 'X';
                otherToken = 'O';
                System.out.println("玩家一 使用 'X'\n等待玩家二加入游戏");
                fromServer.readInt();
                System.out.println("玩家二加入游戏，请走棋");
            } else if (myNum == PLAYER2) {
                myToken = 'O';
                otherToken = 'X';
                System.out.println("玩家二 使用'O'\n等待玩家一走棋");
            }

            // 开始游戏
            while (continueToPlay) {
                if (myNum == PLAYER1) {
                    sendMove();
                    showCell();
                    status = receiveFromServer();
                    showCell();
                } else if (myNum == PLAYER2) {
                    status = receiveFromServer();
                    showCell();
                    if (!continueToPlay) {
                        // 特判一下
                        break;
                    }
                    sendMove();
                    showCell();
                }
            }
            // 游戏结束
            if (status == PLAYER1_WON) {
                // 1P赢
                if (myToken == 'X') {
                    System.out.println("I Won! 'X'");
                } else if (myToken == 'O') {
                    System.out.println("1P 'X' Won!");
                }
            } else if (status == PLAYER2_WON) {
                if (myToken == 'X') {
                    System.out.println("2P 'O' Won!");
                } else if (myToken == 'O') {
                    System.out.println("I Won! 'O'");
                }
            } else if (status == DRAW) {
                // 平局
                System.out.println("No Winner!");
            }
            player.close();
        } catch (IOException e1) {
            System.out.println(e1);
            System.out.println("连接服务器失败！");
        }catch(InterruptedException e2){
            System.out.println(e2);
            System.out.println("控制台错误！");
        }
    }

    public void waitForPlayer() {
        // 等待玩家输入
        System.out.println("请输入下一步:");
        row = input.nextInt();
        clown = input.nextInt();
    }

    public void sendMove() throws IOException {
        // 将输入发送至服务器
        while (singal != CANMOVE) {
            waitForPlayer();
            toServer.writeInt(row);
            toServer.writeInt(clown);
            singal = fromServer.readInt();
            if (singal != CANMOVE) {
                System.out.println("无法走这一步");
            }
        }
        // 合理的一步
        cell[row - 1][clown - 1] = myToken;
        singal = CANTMOVE;
    }

    public int receiveFromServer() throws IOException {
        System.out.println("等待对手走棋ing...");
        int status = fromServer.readInt();
        if (status == PLAYER1_WON) {
            // 1P赢
            continueToPlay = false;
            if (myToken == 'O') {
                receiveMove();
            }
        } else if (status == PLAYER2_WON) {
            continueToPlay = false;
            if (myToken == 'X') {
                receiveMove();
            }
        } else if (status == DRAW) {
            // 平局
            continueToPlay = false;
            if (myToken == 'O') {
                // 平局只能是1P下最后一步
                receiveMove();
            }
        } else {
            receiveMove();
        }
        return status;
    }

    public void receiveMove() throws IOException {
        row = fromServer.readInt();
        clown = fromServer.readInt();
        cell[row - 1][clown - 1] = otherToken;
    }

    private void showCell()throws IOException, InterruptedException {
        // 打印棋盘
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        System.out.println("棋局为：");
        // 打印棋盘头
        for (int i = 0; i < 8; i++) {
            if (i % 2 == 0) {
                System.out.print(i / 2);
            } else {
                System.out.print("|");
            }
        }
        System.out.println();
        // 打印棋盘主体
        for (int i = 0; i < 3; i++) {
            System.out.println("--------\n");
            System.out.println(i+1 + "|" + cell[i][0] + "|" + cell[i][1] + "|" + cell[i][2] + "|");
        }
    }

    private void closeInput() {
        // 关闭输入流
        input.close();
    }

}
