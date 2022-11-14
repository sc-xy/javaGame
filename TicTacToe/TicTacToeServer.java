package TicTacToe;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class TicTacToeServer implements TicTacToeConstants {
    public static void main(String[] args) {
        new TicTacToeServer();
    }

    public TicTacToeServer() {

        try {
            // 创建服务器端口
            ServerSocket server = new ServerSocket(8000);
            System.out.println("服务器开始运行" + new Date() + " at port 8000");

            // 游戏回合
            int sessionNo = 1;
            while (sessionNo != 0) {
                // 等待玩家加入游戏
                System.out.print(new Date() + " 等待玩家加入游戏" + sessionNo + '\n');

                // 1p加入游戏
                Socket player1 = server.accept();
                System.out.println(
                        new Date() + " 1P ip: " + player1.getInetAddress().getHostAddress() + " 加入游戏" + sessionNo);
                DataOutputStream toplayer1 = new DataOutputStream(player1.getOutputStream());
                toplayer1.writeInt(PLAYER1);

                // 2p加入游戏
                Socket player2 = server.accept();
                System.out.println(
                        new Date() + " 2P ip: " + player2.getInetAddress().getHostAddress() + " 加入游戏" + sessionNo);
                DataOutputStream toplayer2 = new DataOutputStream(player2.getOutputStream());
                toplayer2.writeInt(PLAYER2);

                // 开始游戏
                System.out.println(new Date() + ": 开始游戏 回合" + sessionNo++);
                HandleASession task = new HandleASession(player1, player2);
                new Thread(task).start();
            }
            server.close();
        } catch (IOException e) {
            System.err.println("服务器创建失败");
            e.printStackTrace();
        }
    }

    public class HandleASession implements Runnable, TicTacToeConstants {
        // 两个玩家
        private Socket player1;
        private Socket player2;

        // 输入输出流
        DataOutputStream toPlayer1;
        DataOutputStream toPlayer2;
        DataInputStream fromPlayer1;
        DataInputStream fromPlayer2;

        // 棋盘
        char[][] cell = new char[3][3];
        private boolean canMove = false;

        public HandleASession(Socket player1, Socket player2) {
            // 初始化进程
            this.player1 = player1;
            this.player2 = player2;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    cell[i][j] = ' ';
                }
            }
        }

        @Override
        public void run() {
            try {
                // 初始化输入输出
                toPlayer1 = new DataOutputStream(player1.getOutputStream());
                toPlayer2 = new DataOutputStream(player2.getOutputStream());
                fromPlayer1 = new DataInputStream(player1.getInputStream());
                fromPlayer2 = new DataInputStream(player2.getInputStream());

                // 通知1P开始游戏
                toPlayer1.writeInt(1);

                while (true) {
                    int row = 0, clown = 0;
                    // 1P走棋
                    while (!canMove) {
                        row = fromPlayer1.readInt();
                        clown = fromPlayer1.readInt();
                        canMove = changeCell(row, clown, 'X');
                        if (!canMove) {
                            // 通知无法走这步
                            toPlayer1.writeInt(CANTMOVE);
                        }
                    }
                    toPlayer1.writeInt(CANMOVE);
                    canMove = false;

                    if (isWon('X')) {
                        // 1P赢了
                        toPlayer1.writeInt(PLAYER1_WON);
                        toPlayer2.writeInt(PLAYER1_WON);
                        sendMove(toPlayer2, row, clown);
                        break;
                    } else if (isFull()) {
                        // 平局
                        toPlayer1.writeInt(DRAW);
                        toPlayer2.writeInt(DRAW);
                        sendMove(toPlayer2, row, clown);
                        break;
                    } else {
                        // 继续游戏
                        toPlayer2.writeInt(CONTINUE);
                        sendMove(toPlayer2, row, clown);
                    }

                    // 2P走棋
                    while (!canMove) {
                        row = fromPlayer2.readInt();
                        clown = fromPlayer2.readInt();
                        canMove = changeCell(row, clown, 'O');
                        if (!canMove) {
                            // 通知无法走这步
                            toPlayer2.writeInt(CANTMOVE);
                        }
                    }
                    toPlayer2.writeInt(CANMOVE);
                    canMove = false;

                    if (isWon('O')) {
                        // 2P赢了
                        toPlayer1.writeInt(PLAYER2_WON);
                        toPlayer2.writeInt(PLAYER2_WON);
                        sendMove(toPlayer1, row, clown);
                        break;
                    } else {
                        // 继续游戏
                        toPlayer1.writeInt(CONTINUE);
                        sendMove(toPlayer1, row, clown);
                    }

                }
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        public void sendMove(DataOutputStream out, int row, int clown) throws IOException {
            out.writeInt(row);
            out.writeInt(clown);
        }

        public boolean isFull() {
            // 棋盘是否已满
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (cell[i][j] == ' ') {
                        return false;
                    }
                }
            }
            return true;
        }

        public boolean isWon(char c) {
            // 检查所有行
            for (int i = 0; i < 3; i++) {
                if (cell[i][0] == c && cell[i][1] == c && cell[i][2] == c) {
                    return true;
                }
            }
            // 检查所有列
            for (int i = 0; i < 3; i++) {
                if (cell[0][i] == c && cell[1][i] == c && cell[2][i] == c) {
                    return true;
                }
            }
            // 检查对角线
            if (cell[0][0] == c && cell[1][1] == c && cell[2][2] == c) {
                return true;
            }
            if (cell[0][2] == c && cell[1][1] == c && cell[2][0] == c) {
                return true;
            }

            return false;
        }

        public boolean changeCell(int row, int clown, char c) {
            if (cell[row - 1][clown - 1] == ' ') {
                cell[row - 1][clown - 1] = c;
                return true;
            }
            return false;
        }

    }

}
