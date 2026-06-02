package ChatSystem.client.ui;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import ChatSystem.client.*;
import ChatSystem.shared.*;


public class LobbyPanel extends JPanel {
        
    private ClientManager manager;
    private JButton createGroupBtn;
    private JButton joinGroupBtn;
    private JButton logoutBtn;
    private JList<String> memberList;
    private DefaultListModel<String> MemberListModel;
    private JList<String> fileList;
    private DefaultListModel<String> fileListModel;
    private JTextField messageField;
    private JButton sendBtn;
    private JTextArea chatArea;
    private JButton chooseFileBtn;
    private JButton historyBtn;
    private User user;

    public LobbyPanel(ClientManager manager, User user) {
        this.user = user;
        this.manager = manager;

        setLayout(new BorderLayout());
        
        MemberListModel = new DefaultListModel<>();
        memberList = new JList<>(MemberListModel);
        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);

        messageField = new JTextField();

        // initialize chat area before adding to scroll pane
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(new JLabel("Chat Frame"), BorderLayout.NORTH);
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        add(chatPanel, BorderLayout.CENTER);

        // User panel: top = online users, bottom = files/media
        JPanel UserPanel = new JPanel(new GridLayout(2,1));
        JPanel onlinePanel = new JPanel(new BorderLayout());
        onlinePanel.add(new JLabel("Online User"), BorderLayout.NORTH);
        onlinePanel.add(new JScrollPane(memberList), BorderLayout.CENTER);

        JPanel filesPanel = new JPanel(new BorderLayout());
        filesPanel.add(new JLabel("Files/Media"), BorderLayout.NORTH);
        filesPanel.add(new JScrollPane(fileList), BorderLayout.CENTER);

        UserPanel.add(onlinePanel);
        UserPanel.add(filesPanel);
        UserPanel.setPreferredSize(new Dimension(200, 0));

        add(UserPanel, BorderLayout.WEST);
//        add(ChatFrameLabel, BorderLayout.)

        sendBtn = new JButton("Send");
        chooseFileBtn = new JButton("Choose File");
        historyBtn = new JButton("History");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4));
        createGroupBtn = new JButton("Create Group");
        joinGroupBtn = new JButton("Join Group");
        logoutBtn = new JButton("Logout");
        buttonPanel.add(createGroupBtn);
        buttonPanel.add(joinGroupBtn);
        buttonPanel.add(logoutBtn);
        buttonPanel.add(historyBtn);
        
        add(buttonPanel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);
        inputPanel.add(chooseFileBtn, BorderLayout.WEST);
        add(inputPanel, BorderLayout.SOUTH);

        setupEventListeners();
    }



    private void setupEventListeners(){
        createGroupBtn.addActionListener(e -> {
            CreateGroupDialog createGroupDialog = new CreateGroupDialog(null, manager, user);
            createGroupDialog.setVisible(true);
        });

        joinGroupBtn.addActionListener(e -> {
            JoinGroupDialog joinGroupDialog = new JoinGroupDialog(null, manager, user);
            joinGroupDialog.setVisible(true);
            }
        );

        fileList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) { // Double-click detected
                    int index = fileList.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        String fileName = fileListModel.getElementAt(index);
                        Message message = new Message(MessageType.FILE_DOWNLOAD);
                        message.setFileName(fileName);
                        message.setContent(fileName);
                        message.setSender(user.getUserName());
                        manager.sendMessage(message);
                    }
                }
            }
        });
        
        logoutBtn.addActionListener(e->{
            Message message = new Message(MessageType.LOGOUT);
            message.setSender(user.getUserName());
            manager.sendMessage(message);
        });

        sendBtn.addActionListener(e->{
            String content = messageField.getText().trim();
            if(content.isBlank()){
                JOptionPane.showMessageDialog(this, "Please enter your message.");

            }
            else {
                Message message = new Message(MessageType.MESSAGE);
                message.setContent(content);
                message.setSender(user.getUserName());
                manager.sendMessage(message);
                messageField.setText("");
            }
            
        });

        sendBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    sendBtn.doClick();
                }
            }
        });
        historyBtn.addActionListener(e -> {
            // Clear global history view to avoid duplicates
            manager.clearHistoryView();
            Message message = new Message(MessageType.HISTORY_REQUEST);
            message.setSender(user.getUserName());
            
            manager.sendMessage(message);
        });

        chooseFileBtn.addActionListener(e->{
            JFileChooser fileChooser = new JFileChooser();
            
            int response = fileChooser.showOpenDialog(null);

            if(response == fileChooser.APPROVE_OPTION){
                File file = fileChooser.getSelectedFile();
                String fileName = file.getName();
                try {
                    byte[] fileData = Files.readAllBytes(file.toPath());
                    Message message = new Message(MessageType.FILE_MESSAGE);

                    message.setFileData(fileData);
                    message.setFileName(fileName); // include original filename metadata
                    message.setContent(fileName);
                    message.setSender(user.getUserName());

                    manager.sendMessage(message);
                } catch (IOException ex) {
                    System.err.println("Error : " + ex.getMessage());
                }
            }
        });

    }

    public void setChatArea(Message message){
        SwingUtilities.invokeLater(()->{
            chatArea.append(message.getSender() + ": " + message.getContent());
            chatArea.append("\n");
        });
    }

    public void clearMemberList(){
        // Clear the model (clears the JList view)
        MemberListModel.clear();
    }

    public void setMemberList(String listMember){
        MemberListModel.clear();
        if(listMember == null || listMember.isBlank()) return;
        String[] members = listMember.split(",");
        for(String m : members){
            MemberListModel.addElement(m.trim());
        }
    }

    public void setFileList(String listFile){
        fileListModel.clear();
        String[] files = listFile.split(",");
        for(String f : files){
            fileListModel.addElement(f.trim());
        }
    }

    public void clearFileList(){
        SwingUtilities.invokeLater(() -> {
            fileListModel.clear();
        });
    }
}