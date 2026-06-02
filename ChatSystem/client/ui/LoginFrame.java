package ChatSystem.client.ui;
import ChatSystem.shared.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import ChatSystem.client.ClientManager;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private ClientManager clientManager;

    public LoginFrame(ClientManager clientManager) {
        this.clientManager = clientManager;
        
        // 1. Kết nối tới Server ngay khi cửa sổ ứng dụng mở lên
        if (clientManager.connect("localhost", 12345)) {
            // 2. Tách vòng lặp lắng nghe tin nhắn sang một Thread mới để chống treo GUI
            new Thread(() -> clientManager.listenMessage()).start();
        } else {
            JOptionPane.showMessageDialog(null, "Không thể kết nối tới Server!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        setTitle("Chat System - Login");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2, 5, 5));

        add(new JLabel("Username:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        add(loginButton);
        add(registerButton);

        addLoginListener(null);
        addRegisterListener(null);

    }

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getPassword() {
        return new String(passwordField.getPassword()).trim();
    }

    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(e -> {
            String username = getUsername();
            String password = getPassword();
            if(username.isBlank() || password.isBlank()){
                JOptionPane.showMessageDialog(this, "Please enter username and password.", "Login", JOptionPane.WARNING_MESSAGE);
            } else {
                Message message = new Message(MessageType.LOGIN);
                message.setSender(username);
                message.setContent(password);

                clientManager.sendMessage(message);
            }
        });
    }

    public void addRegisterListener(ActionListener listener) {
        registerButton.addActionListener(e -> {
            String username = getUsername();
            String password = getPassword();
            if(username.isBlank() || password.isBlank()){
                JOptionPane.showMessageDialog(this, "Please enter username and password.", 
                    "Register", JOptionPane.WARNING_MESSAGE);
            } else {
                Message message = new Message(MessageType.REGISTER);
                message.setSender(username);
                message.setContent(password);
                clientManager.sendMessage(message);
            }
        });
    }

    

}
