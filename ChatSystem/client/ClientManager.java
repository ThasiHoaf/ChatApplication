package ChatSystem.client;
import java.net.*;
import java.io.*;
import ChatSystem.shared.*;
import ChatSystem.client.ui.*;
import javax.swing.*;

import javax.swing.*;
import java.time.format.DateTimeFormatter;

public class ClientManager {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private MainFrame mainFrame;
   
 
    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            return true;
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            return false;
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
    
    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }

    }

    public void clearHistoryView() {
        SwingUtilities.invokeLater(() -> {
            if (mainFrame != null) {
                mainFrame.clearHistoryPanel();
            }
        });
    }

    public void clearMemberList(){
        SwingUtilities.invokeLater(()->{
            if(mainFrame != null){
                mainFrame.clearMemberList();
            }
        });
    }

    public Message receiveMessage() {
        try {
            return (Message) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error receiving message: " + e.getMessage());
            return null;
        }
    }

    public void listenMessage() {
        try {
            while(true){
                Message receivedMessage = (Message) in.readObject();
                if(receivedMessage == null) break;
                handleMessage(receivedMessage);
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }

    public void handleMessage(Message message){
        // Debug incoming messages to help detect duplicates
        System.out.println("Received message: type=" + message.getMessageType() + ", sender=" + message.getSender() + ", group=" + message.getGroupName() + ", content=" + message.getContent() + ", timestamp=" + message.getTimestamp());
        switch(message.getMessageType()){
            case LOGIN:
                if (message.isSuccess()) {
                    SwingUtilities.invokeLater(() -> {

                        Message request = new Message(MessageType.USER_LIST);
                        request.setSender(message.getSender());
                        sendMessage(request);

                        Message fileListReq = new Message(MessageType.FILE_LIST);
                        fileListReq.setSender(message.getSender());
                        sendMessage(fileListReq);

                        User user = new User(message.getSender(), message.getContent());
                        mainFrame = new MainFrame(this, user);
                        mainFrame.setVisible(true);
                        JOptionPane.showMessageDialog(null, "Login successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

                        // CLEANUP: Find and close the LoginFrame
                        for (java.awt.Window window : java.awt.Window.getWindows()) {
                            if (window instanceof ChatSystem.client.ui.LoginFrame) {
                                window.dispose();
                            }
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, message.getInfo(), "Login Failed", JOptionPane.ERROR_MESSAGE);
                    });
                }
                break;

            case REGISTER:
                SwingUtilities.invokeLater(()->{
                    if(message.isSuccess()){
                        JOptionPane.showMessageDialog(null, "Registration successful! You can now login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, message.getInfo(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
                    }
                });
                break;
            case CREATE_GROUP:
                SwingUtilities.invokeLater(()->{
                    if(message.isSuccess()){
                        // mainFrame = new MainFrame();
                        JOptionPane.showMessageDialog(null,
                                "Group created successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);

                    } else {
                        JOptionPane.showMessageDialog(null,
                                message.getInfo(),
                                "Couldn't create Group",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                });
                break;
            case USER_LIST:
                if(message.isSuccess()){
                    SwingUtilities.invokeLater(() -> {
                        if(message.getGroupName() == null){
                            mainFrame.clearMemberList();
                            mainFrame.setMemberList(message.getContent());
                        }
                        else {
                            mainFrame.setGroupMemberList(message.getGroupName(), message.getContent());
                        }
                    });
                }
                break;
            case LOGOUT:
                SwingUtilities.invokeLater(()->{
                    if(message.isSuccess()){
                        this.close();
                        mainFrame.dispose();
//                    LoginFrame loginFrame = new LoginFrame();
//                    loginFrame.setVisible(true);
                    }
                });
                break;
            case MESSAGE:
                SwingUtilities.invokeLater(()->{
                    String content = message.getContent();
                    String target = message.getTarget();
                    if (content != null && !content.isBlank() && target == null) {
                        mainFrame.printMessage(message);
                    } else if(target != null && !target.isBlank()){
                        // Xác định tên Tab: 
                        // - Nếu mình là người gửi (Server dội lại), tab sẽ mang tên người nhận (target).
                        // - Nếu mình là người nhận, tab sẽ mang tên người gửi (sender).
                        String tabName = message.getSender().equals(mainFrame.getCurrentUser().getUserName()) ? target : message.getSender();
                        
                        mainFrame.addPrivateTab(tabName); // Mở tab nếu chưa có
                        mainFrame.printPrivateMessage(message, tabName); // In tin nhắn
                    } else {
                        System.out.println("Empty MESSAGE received...");
                    }
                });
                break;
            case GROUP_MESSAGE:
                SwingUtilities.invokeLater(()->{
                    if(message.isSuccess()){
                        // Định tuyến tin nhắn đến MainFrame để tìm đúng tab xử lý
                        mainFrame.printGroupMessage(message);
                    }
                });
                break;
            case HISTORY_REQUEST:
                SwingUtilities.invokeLater(()->{
                    if(message.isSuccess()){

                        // If server signals CLEAR, ensure UI is cleared and skip adding this item
                        if (message.getInfo() != null && message.getInfo().equals("CLEAR")){
                            String gName = message.getGroupName();
                            if (gName != null) {
                                mainFrame.clearGroupHistory(gName);
                            } else {
                                mainFrame.clearHistoryPanel();
                            }
                            return;
                        }

                        String content = message.getContent();

                        // Format timestamp to be user-friendly
                        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
                        String time = "";
                        if (message.getTimestamp() != null) {
                            try {
                                time = message.getTimestamp().format(fmt);
                            } catch (Exception ex) {
                                time = String.valueOf(message.getTimestamp());
                            }
                        }

                        String groupName = message.getGroupName();

                        if(groupName != null){
                            mainFrame.setGroupHistory(groupName, content, time);
                        }
                        else {
                            // Use DB id propagated from server (fallback to 0)
                            long id = message.getId() != null ? message.getId() : 0L;
                            mainFrame.setHistory(id, content, time);
                            
                        }

                    }
                });
                break;
            case HISTORY_DELETE:
                if(message.isSuccess()){
                    // When a deletion occurs, refresh the relevant history view by requesting it from server.
                    String groupName = message.getGroupName();
                    // Clear existing UI to avoid duplicates before requesting updated history
                    SwingUtilities.invokeLater(() -> {
                        if (groupName == null) {
                            if (mainFrame != null) mainFrame.clearHistoryPanel();
                        } else {
                            if (mainFrame != null) mainFrame.clearGroupHistory(groupName);
                        }
                    });

                    // Send HISTORY_REQUEST for the affected scope
                    Message req = new Message(MessageType.HISTORY_REQUEST);
                    if (mainFrame != null && mainFrame.getCurrentUser() != null) {
                        req.setSender(mainFrame.getCurrentUser().getUserName());
                    }
                    req.setGroupName(groupName); // may be null for lobby
                    sendMessage(req);
                }
                break;
            case FILE_MESSAGE:
                SwingUtilities.invokeLater(()->{
                    if(message.isSuccess()){
                        try {
                            byte[] data = message.getFileData();
                            String fileName = message.getFileName() != null ? message.getFileName() : message.getContent();
                            if (data != null && fileName != null) {
                                java.nio.file.Path downloads = java.nio.file.Paths.get("downloads");
                                java.nio.file.Files.createDirectories(downloads);
                                String safeName = System.currentTimeMillis() + "_" + fileName;
                                java.nio.file.Path outPath = downloads.resolve(safeName);
                                java.nio.file.Files.write(outPath, data);

                                // store saved path in message.info for possible later use
                                message.setInfo(outPath.toString());

                                String display = "sent file: " + fileName + " (saved to " + outPath.toString() + ")";

                                if (message.getGroupName() == null) {
                                    Message m = new Message(MessageType.MESSAGE);
                                    m.setSender(message.getSender());
                                    m.setSuccess(true);
                                    m.setContent(display);
                                    mainFrame.printMessage(m);
                                    // update Files/Media panel for lobby
                                    if (mainFrame != null) {
                                        Message fileListReq = new Message(MessageType.FILE_LIST);
                                        fileListReq.setSender(message.getSender());
                                        fileListReq.setGroupName(message.getGroupName()); // null for lobby
                                        sendMessage(fileListReq);
                                    }
                                } else {
                                    message.setContent(display);
                                    mainFrame.printGroupMessage(message);
                                }

                                // Offer to open or preview the file
                                int r = JOptionPane.showConfirmDialog(null, "File saved to:\n" + outPath.toString() + "\nOpen now?", "File received", JOptionPane.YES_NO_OPTION);
                                if (r == JOptionPane.YES_OPTION) {
                                    try {
                                        String mime = java.nio.file.Files.probeContentType(outPath);
                                        if (mime != null && mime.startsWith("text")) {
                                            String text = new String(java.nio.file.Files.readAllBytes(outPath), java.nio.charset.StandardCharsets.UTF_8);
                                            JTextArea ta = new JTextArea(text);
                                            ta.setEditable(false);
                                            ta.setLineWrap(true);
                                            ta.setWrapStyleWord(true);
                                            JScrollPane sp = new JScrollPane(ta);
                                            sp.setPreferredSize(new java.awt.Dimension(600, 400));
                                            JOptionPane.showMessageDialog(null, sp, "Preview: " + fileName, JOptionPane.PLAIN_MESSAGE);
                                        } else {
                                            if (java.awt.Desktop.isDesktopSupported()) {
                                                java.awt.Desktop.getDesktop().open(outPath.toFile());
                                            } else {
                                                JOptionPane.showMessageDialog(null, "Desktop not supported; can't open file.", "Open failed", JOptionPane.ERROR_MESSAGE);
                                            }
                                        }
                                    } catch (IOException ex2) {
                                        JOptionPane.showMessageDialog(null, "Couldn't open file: " + ex2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                }

                            } else {
                                // fallback: just display filename
                                if (message.getGroupName() == null) {
                                    Message m = new Message(MessageType.MESSAGE);
                                    m.setSender(message.getSender());
                                    m.setSuccess(true);
                                    m.setContent("sent file: " + message.getContent());
                                    mainFrame.printMessage(m);
                                } else {
                                    message.setContent("sent file: " + message.getContent());
                                    mainFrame.printGroupMessage(message);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Error handling incoming file: " + e.getMessage());
                        }
                    }
                });
                break;
            case JOIN_GROUP:
                SwingUtilities.invokeLater(()->{
                    if(message.isSuccess()){
                        // Use MainFrame helper to create the GroupPanel and register listeners
                        mainFrame.addGroupTab(message.getGroupName());
                        JOptionPane.showMessageDialog(
                                mainFrame,
                                "Join Successfully!",
                                "SUCCESS",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        Message getUserMessage = new Message(MessageType.USER_LIST);
                        getUserMessage.setSender(message.getSender());
                        getUserMessage.setGroupName(message.getGroupName());
                        sendMessage(getUserMessage);
                    } else {
                        JOptionPane.showMessageDialog(
                                mainFrame,
                                "Couldn't join group",
                                "Failure",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                });

                break;
            case FILE_LIST:
                SwingUtilities.invokeLater(()->{
                    if(message.isSuccess()){
                        String groupName = message.getGroupName();
                        String content = message.getContent();
                        if(content != null && !content.isBlank()){
                            String[] files = content.split(",");
                            if(groupName != null){
                                if(mainFrame != null){
                                    mainFrame.setGroupFileList(groupName, content);
                                }
                            } else {
                                if(mainFrame != null){
                                    mainFrame.setLobbyFileList(content);
                                }
                            }
                        }
                    }
                });
                break;
            case FILE_DOWNLOAD:
                SwingUtilities.invokeLater(()->{
                    if(message.isSuccess()){
                        try {
                            byte[] data = message.getFileData();
                            String fileName = message.getFileName();
                            
                            if (data != null && fileName != null) {
                                // 1. Tạo thư mục chứa file tải về ở Client
                                java.nio.file.Path downloads = java.nio.file.Paths.get("client_downloads");
                                java.nio.file.Files.createDirectories(downloads);
                                
                                // 2. Lưu file với tiền tố thời gian để tránh trùng tên
                                String safeName = System.currentTimeMillis() + "_" + fileName;
                                java.nio.file.Path outPath = downloads.resolve(safeName);
                                java.nio.file.Files.write(outPath, data);

                                // 3. Mở file vừa lưu
                                if (java.awt.Desktop.isDesktopSupported()) {
                                    java.awt.Desktop.getDesktop().open(outPath.toFile());
                                } else {
                                    JOptionPane.showMessageDialog(null, "Desktop is not supported. File saved to: " + outPath.toString(), "Info", JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null, "Couldn't save or open file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        // Xử lý khi có lỗi từ Server trả về (ví dụ: File not found)
                        JOptionPane.showMessageDialog(null, message.getInfo(), "Download Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
                break;
            default:
                break;
        }
    }

    public void openPrivateChatTab(String targetUser) {
        SwingUtilities.invokeLater(() -> {
            if (mainFrame != null) {
                mainFrame.addPrivateTab(targetUser);
            }
        });
    }

    
}
