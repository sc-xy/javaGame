package com.xygame;


/**
 * 井字棋常量
 */
public interface TicTacToeConstants {
    public static int PLAYER1 = 1; // 1p
    public static int PLAYER2 = 2; // 2p
    public static int PLAYER1_WON = 1; // 1p赢
    public static int PLAYER2_WON = 2; // 2p赢
    public static int DRAW = 3; // 平局
    public static int CONTINUE = 4; // 继续游戏
    public static int CANTMOVE = 5; // 无法走这步
    public static int CANMOVE = 6; // 能走这步
    public static int ERROR = -1; // 连接中断
}