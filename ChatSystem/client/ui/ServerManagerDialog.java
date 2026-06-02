package ChatSystem.client.ui;

import ChatSystem.client.util.ServerConfigManager;
import ChatSystem.client.util.ServerConfigManager.ServerInfo;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ServerManagerDialog extends JDialog {
    private DefaultListModel<ServerInfo> listModel;
    private JList<ServerInfo> serverList;
    private JTextField nameField, hostField, portField;
    private Runnable onUpdateCallback; // Gọi lại hàm này khi có thay đổi để LoginFrame cập nhật

    public ServerManagerDialog(JFrame parent, Runnable onUpdateCallback) {
        super(parent, "Quản lý Danh sách Server", true);
        this.onUpdateCallback = onUpdateCallback;
        
        setSize(450, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // --- Danh sách (Trái) ---
        listModel = new DefaultListModel<>();
        serverList = new JList<>(listModel);
        loadData();
        add(new JScrollPane(serverList), BorderLayout.CENTER);

        // --- Khu vực nhập liệu (Phải) ---
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        inputPanel.add(new JLabel("Tên Server:"));
        nameField = new JTextField();
        inputPanel.add(nameField);
        
        inputPanel.add(new JLabel("IP/Host:"));
        hostField = new JTextField();
        inputPanel.add(hostField);
        
        inputPanel.add(new JLabel("Port:"));
        portField = new JTextField("12345");
        inputPanel.add(portField);

        // Nút bấm thêm/sửa/xóa
        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        JButton addBtn = new JButton("Thêm");
        JButton editBtn = new JButton("Sửa");
        JButton delBtn = new JButton("Xóa");
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(inputPanel, BorderLayout.NORTH);
        rightPanel.add(btnPanel, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        setupEvents(addBtn, editBtn, delBtn);
    }

    private void loadData() {
        listModel.clear();
        for (ServerInfo s : ServerConfigManager.loadServers()) {
            listModel.addElement(s);
        }
    }

    private void saveData() {
        List<ServerInfo> servers = new java.util.ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            servers.add(listModel.getElementAt(i));
        }
        ServerConfigManager.saveServers(servers);
        if (onUpdateCallback != null) onUpdateCallback.run(); // Báo cho LoginFrame load lại combobox
    }

    private void setupEvents(JButton addBtn, JButton editBtn, JButton delBtn) {
        // Khi chọn 1 dòng, hiển thị lên text field
        serverList.addListSelectionListener(e -> {
            ServerInfo selected = serverList.getSelectedValue();
            if (selected != null) {
                nameField.setText(selected.name);
                hostField.setText(selected.host);
                portField.setText(String.valueOf(selected.port));
            }
        });

        addBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String host = hostField.getText().trim();
                int port = Integer.parseInt(portField.getText().trim());
                if (name.isEmpty() || host.isEmpty()) throw new Exception("Tên và Host không được rỗng!");
                
                listModel.addElement(new ServerInfo(name, host, port));
                saveData();
                clearFields();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        });

        editBtn.addActionListener(e -> {
            int index = serverList.getSelectedIndex();
            if (index != -1) {
                try {
                    String name = nameField.getText().trim();
                    String host = hostField.getText().trim();
                    int port = Integer.parseInt(portField.getText().trim());
                    listModel.set(index, new ServerInfo(name, host, port));
                    saveData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Port phải là số!");
                }
            }
        });

        delBtn.addActionListener(e -> {
            int index = serverList.getSelectedIndex();
            if (index != -1) {
                listModel.remove(index);
                saveData();
                clearFields();
            }
        });
    }

    private void clearFields() {
        nameField.setText("");
        hostField.setText("");
        portField.setText("12345");
    }
}