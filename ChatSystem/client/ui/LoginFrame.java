package ChatSystem.client.ui;

import ChatSystem.shared.*;
import ChatSystem.client.ClientManager;
import ChatSystem.client.util.ServerConfigManager;
import ChatSystem.client.util.ServerConfigManager.ServerInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class LoginFrame extends JFrame {
    private JComboBox<ServerInfo> serverCombo;
    private JButton manageServerBtn;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    
    private ClientManager clientManager;
    private boolean isConnected = false; // Cờ theo dõi trạng thái kết nối

    public LoginFrame(ClientManager clientManager) {
        this.clientManager = clientManager;
        
        // Chú ý: KHÔNG kết nối tự động ở đây nữa!

        setTitle("Chat System - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Giao diện (Lưới 4 hàng, 2 cột)
        setLayout(new GridLayout(4, 2, 5, 10));

        // Dòng 1: Chọn Server
        add(new JLabel(" Chọn Server:"));
        JPanel serverPanel = new JPanel(new BorderLayout());
        serverCombo = new JComboBox<>();
        loadServerList(); // Nạp dữ liệu từ file
        manageServerBtn = new JButton("Quản lý");
        serverPanel.add(serverCombo, BorderLayout.CENTER);
        serverPanel.add(manageServerBtn, BorderLayout.EAST);
        add(serverPanel);

        // Dòng 2 & 3: User/Pass
        add(new JLabel(" Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel(" Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        // Dòng 4: Buttons
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        add(loginButton);
        add(registerButton);

        setupEvents();
    }

    // Nạp lại danh sách server vào combobox
    private void loadServerList() {
        serverCombo.removeAllItems();
        List<ServerInfo> servers = ServerConfigManager.loadServers();
        for (ServerInfo s : servers) {
            serverCombo.addItem(s);
        }
    }

    public String getUsername() { return usernameField.getText().trim(); }
    public String getPassword() { return new String(passwordField.getPassword()).trim(); }

    // Hàm tạo kết nối trước khi gửi tin nhắn
    private boolean ensureConnection() {
        if (isConnected) return true; // Nếu đã kết nối rồi thì bỏ qua
        
        ServerInfo selectedServer = (ServerInfo) serverCombo.getSelectedItem();
        if (selectedServer == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Bắt đầu kết nối
        if (clientManager.connect(selectedServer.host, selectedServer.port)) {
            isConnected = true;
            new Thread(() -> clientManager.listenMessage()).start();
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến " + selectedServer.host, "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void setupEvents() {
        // Nút Quản lý Server mở Dialog lên
        manageServerBtn.addActionListener(e -> {
            ServerManagerDialog dialog = new ServerManagerDialog(this, () -> {
                // Callback: Được gọi khi bên Dialog bấm Save
                loadServerList();
                isConnected = false; // Ép kết nối lại nếu có thay đổi cấu hình
            });
            dialog.setVisible(true);
        });

        // Nút Đăng nhập
        loginButton.addActionListener(e -> {
            String username = getUsername();
            String password = getPassword();
            if (username.isBlank() || password.isBlank()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Username và Password.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Phải kết nối thành công mới cho gửi tin nhắn
            if (ensureConnection()) {
                Message message = new Message(MessageType.LOGIN);
                message.setSender(username);
                message.setContent(password);
                clientManager.sendMessage(message);
            }
        });

        // Nút Đăng ký
        registerButton.addActionListener(e -> {
            String username = getUsername();
            String password = getPassword();
            if (username.isBlank() || password.isBlank()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập Username và Password.", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Phải kết nối thành công mới cho gửi tin nhắn
            if (ensureConnection()) {
                Message message = new Message(MessageType.REGISTER);
                message.setSender(username);
                message.setContent(password);
                clientManager.sendMessage(message);
            }
        });

        // Nếu người dùng đổi Server trong danh sách, ngắt kết nối hiện tại để làm mới
        serverCombo.addActionListener(e -> {
            isConnected = false;
        });
    }
}