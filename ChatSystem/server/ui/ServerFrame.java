package ChatSystem.server.ui;

import ChatSystem.server.Server;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ServerFrame extends JFrame {
    private Server server;
    
    // Components
    private JTextField portField;
    private JButton startBtn;
    private JButton stopBtn;
    private JTextArea logArea;
    private DefaultListModel<String> clientListModel;
    private JList<String> clientList;

    public ServerFrame() {
        server = new Server();
        
        setTitle("Chat Server Management");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- PANEL CẤU HÌNH (Bắc) ---
        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        configPanel.add(new JLabel("Port:"));
        portField = new JTextField("12345", 10);
        configPanel.add(portField);

        startBtn = new JButton("Start Server");
        stopBtn = new JButton("Stop Server");
        stopBtn.setEnabled(false); // Ban đầu chưa chạy nên nút Stop bị mờ

        configPanel.add(startBtn);
        configPanel.add(stopBtn);
        add(configPanel, BorderLayout.NORTH);

        // --- PANEL LOG (Trung tâm) ---
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Server Logs"));
        add(logScroll, BorderLayout.CENTER);

        // --- PANEL CLIENT ĐANG KẾT NỐI (Đông) ---
        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        JScrollPane clientScroll = new JScrollPane(clientList);
        clientScroll.setPreferredSize(new Dimension(200, 0));
        clientScroll.setBorder(BorderFactory.createTitledBorder("Online Clients"));
        add(clientScroll, BorderLayout.EAST);

        setupListeners();
    }

    private void setupListeners() {
        // Lắng nghe log từ Server logic để in lên giao diện
        server.setLogListener(message -> {
            SwingUtilities.invokeLater(() -> {
                logArea.append(message + "\n");
                // Tự động cuộn xuống dòng mới nhất
                logArea.setCaretPosition(logArea.getDocument().getLength());
            });
        });

        // Lắng nghe thay đổi danh sách client từ SessionManager
        server.getSessionManager().setOnUserListChanged(users -> {
            SwingUtilities.invokeLater(() -> {
                clientListModel.clear();
                for (String u : users) {
                    clientListModel.addElement(u);
                }
            });
        });

        startBtn.addActionListener(e -> {
            try {
                int port = Integer.parseInt(portField.getText().trim());
                server.startServer(port);
                startBtn.setEnabled(false);
                portField.setEnabled(false);
                stopBtn.setEnabled(true);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Port phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        stopBtn.addActionListener(e -> {
            server.stopServer();
            startBtn.setEnabled(true);
            portField.setEnabled(true);
            stopBtn.setEnabled(false);
            clientListModel.clear();
        });
    }

    public static void main(String[] args) {
        // Chạy giao diện trên Event Dispatch Thread (Tiêu chuẩn của Swing)
        SwingUtilities.invokeLater(() -> {
            new ServerFrame().setVisible(true);
        });
    }
}