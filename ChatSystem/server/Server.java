package ChatSystem.server;

import ChatSystem.database.DBConnection;
import ChatSystem.server.dao.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public class Server {
    private ServerSocket serverSocket;
    private volatile boolean isRunning = false;
    
    final SessionManager sessionManager;
    final MessageRouter messageRouter;
    final UserDAO userDAO;
    final GroupDAO groupDAO;
    final MessageDAO messageDAO;

    // (MỚI) Callback ghi log ra màn hình
    private Consumer<String> logListener;

    public Server() {
        DBConnection dbConnection = new DBConnection();
        this.userDAO = new UserDAO(dbConnection);
        this.groupDAO = new GroupDAO(dbConnection);
        this.messageDAO = new MessageDAO(dbConnection);
        this.sessionManager = new SessionManager();
        this.messageRouter = new MessageRouter(sessionManager, userDAO, groupDAO, messageDAO);
    }

    // (MỚI) Thêm lại hàm main cho chế độ Headless (Chạy ngầm trong Docker)
    public static void main(String[] args) {
        Server server = new Server();
        server.startServer(12345);
        System.out.println("Server is running in Headless mode (No GUI)...");
    }


    public void setLogListener(Consumer<String> logListener) {
        this.logListener = logListener;
    }

    private void log(String message) {
        if (logListener != null) {
            logListener.accept(message);
        } else {
            System.out.println(message);
        }
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    // (SỬA) Hàm start không còn chặn luồng
    public void startServer(int port) {
        if (isRunning) return;
        isRunning = true;
        
        // Đưa vòng lặp accept() vào Thread mới
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                log("Server is running on port " + port + "...");

                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    log("New client connected: " + clientSocket.getInetAddress());

                    ClientHandler clientHandler = new ClientHandler(clientSocket, messageRouter);
                    new Thread(clientHandler::listen).start();
                }
            } catch (IOException e) {
                if (isRunning) {
                    log("Server error: " + e.getMessage());
                } else {
                    log("Server stopped.");
                }
            }
        }).start();
    }

    // (MỚI) Hàm dừng server an toàn
    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Lệnh này sẽ ngắt vòng lặp accept() đang chờ
            }
        } catch (IOException e) {
            log("Error stopping server: " + e.getMessage());
        }
    }
}