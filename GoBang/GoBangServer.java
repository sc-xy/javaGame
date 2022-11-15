package GoBang;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class GoBangServer implements GoBangConstants {
    private int port = 8000;

    public GoBangServer() {
        try {
            // 创建服务器端口
            ServerSocket server = new ServerSocket(port);
            System.out.println(new Date() + ": 服务器开始运行，端口: " + port);

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

    public static void main(String[] args) {
        new GoBangServer();
    }

    public class HandleASession implements Runnable, GoBangConstants {
        // 玩家连接
        private Socket player1;
        private Socket player2;
        // 输入输出流
        private DataInputStream fromPlayer1;
        private DataInputStream fromPlayer2;
        private DataOutputStream toPlayer1;
        private DataOutputStream toPlayer2;
        // 棋盘
        private char[][] cell = new char[4 + 15 + 4][4 + 15 + 4];

        @Override
        public void run() {
            try {
                // 初始化输入输出流
                fromPlayer1 = new DataInputStream(this.player1.getInputStream());
                fromPlayer2 = new DataInputStream(this.player2.getInputStream());
                toPlayer1 = new DataOutputStream(this.player1.getOutputStream());
                toPlayer2 = new DataOutputStream(this.player2.getOutputStream());

                // 通知1P开始游戏
                toPlayer1.writeInt(CONTINUE);

                // 开始游戏
                int row = 0, clown = 0;
                while (true) {
                    // 1P走棋
                    row = fromPlayer1.readInt() + 4 - 1;
                    clown = fromPlayer1.readInt() + 4 - 1;
                    cell[row][clown] = 'X';
                    if (isWon(row, clown)) {
                        // 1P获胜
                        toPlayer1.writeInt(PLAYER1_WON);
                        toPlayer2.writeInt(PLAYER1_WON);
                        sendMove(toPlayer2, row, clown);
                        break;
                    } else if (isFUll()) {
                        // 平局
                        toPlayer1.writeInt(DRAW);
                        toPlayer1.writeInt(DRAW);
                        sendMove(toPlayer2, row, clown);
                        break;
                    } else {
                        // 继续游戏
                        toPlayer2.writeInt(CONTINUE);
                        sendMove(toPlayer2, row, clown);
                    }
                    // 2P走棋
                    row = fromPlayer2.readInt() + 4 - 1;
                    clown = fromPlayer2.readInt() + 4 - 1;
                    cell[row][clown] = 'O';
                    if (isWon(row, clown)) {
                        // 1P获胜
                        toPlayer1.writeInt(PLAYER1_WON);
                        toPlayer2.writeInt(PLAYER1_WON);
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
            out.writeInt(row + 1 - 4);
            out.writeInt(clown + 1 - 4);
        }

        public HandleASession(Socket player1, Socket player2) {
            this.player1 = player1;
            this.player2 = player2;
            // 初始化棋盘
            for (int i = 0; i < 23; i++) {
                for (int j = 0; j < 23; j++) {
                    cell[i][j] = ' ';
                }
            }
        }

        public boolean isWon(int row, int clown) {
            char token = cell[row][clown];
            int num = 0;
            // 竖直方向
            for (int i = row - 4; i <= row + 4; i++) {
                if (cell[i][clown] == token) {
                    num++;
                } else {
                    num = 0;
                }
                if (num == 5) {
                    return true;
                }
            }
            num = 0;
            // 水平方向
            for (int i = clown - 4; i <= clown + 4; i++) {
                if (cell[row][i] == token) {
                    num++;
                } else {
                    num = 0;
                }
                if (num == 5) {
                    return true;
                }
            }
            num = 0;
            // 主对角线方向
            for (int i = row - 4, j = clown - 4; i <= row + 4; i++, j++) {
                if (cell[i][j] == token) {
                    num++;
                } else {
                    num = 0;
                }
                if (num == 5) {
                    return true;
                }
            }
            num = 0;
            // 副对角线方向
            for (int i = row - 4, j = clown + 4; i <= row + 4; i++, j--) {
                if (cell[i][j] == token) {
                    num++;
                } else {
                    num = 0;
                }
                if (num == 5) {
                    return true;
                }
            }
            return false;
        }

        public boolean isFUll() {
            for (int i = 4; i < 15 + 4; i++) {
                for (int j = 4; j < 15 + 4; j++) {
                    if (cell[i][j] == ' ') {
                        return false;
                    }
                }
            }
            return true;
        }

        public void showCell() {
            for (int i = 4; i < 15 + 4; i++) {
                for (int j = 4; j < 15 + 4; j++) {
                    System.out.print(cell[i][j]);
                }
                System.out.println();
            }
        }
    }

}
