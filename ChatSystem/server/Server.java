package ChatSystem.server;

import ChatSystem.database.DBConnection;
import ChatSystem.server.dao.GroupDAO;
import ChatSystem.server.dao.MessageDAO;
import ChatSystem.server.dao.UserDAO;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final int PORT = 12345;
    
    final SessionManager sessionManager;
    final MessageRouter messageRouter;
    final UserDAO userDAO;
    final GroupDAO groupDAO;
    final MessageDAO messageDAO;

    public Server() {
        // 1. Khởi tạo các thành phần chia sẻ (Shared Resources)
        DBConnection dbConnection = new DBConnection();
        this.userDAO = new UserDAO(dbConnection);
        this.groupDAO = new GroupDAO(dbConnection);
        this.messageDAO = new MessageDAO(dbConnection);
        this.sessionManager = new SessionManager();

        // 2. Khởi tạo MessageRouter để điều phối gói tin
        this.messageRouter = new MessageRouter(sessionManager, userDAO, groupDAO, messageDAO);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server is running on port " + PORT + "...");


            // 3. Vòng lặp vô hạn để liên tục tiếp nhận các kết nối mới từ Client
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // 4. Khởi tạo ClientHandler và chạy nó trên một Thread mới
                ClientHandler clientHandler = new ClientHandler(clientSocket, messageRouter);
                new Thread(clientHandler::listen).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
