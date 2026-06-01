package ChatSystem.client.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import ChatSystem.client.*;
import ChatSystem.server.dao.UserDAO;
import ChatSystem.shared.*;



public class GroupPanel extends JPanel {
    private String groupName;
    private JButton sendBtn; 
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton chooseFileBtn;
    private JButton historyBtn;
    private DefaultListModel<String> memberList;
    private JList<String> memberJList;
    private ClientManager clientManager;
    private JTextArea historyArea;

    public GroupPanel(ClientManager manager,String groupName, User user) {
        setLayout(new BorderLayout());
        this.groupName = groupName;
        this.clientManager = manager;

        // History area

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        JScrollPane historyScroll = new JScrollPane(historyArea);
        historyScroll.setPreferredSize(new Dimension(220, 0));
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.add(new JLabel("History"), BorderLayout.NORTH);
        historyPanel.add(historyScroll, BorderLayout.CENTER);

        add(historyPanel, BorderLayout.WEST);

        // Chat area

        chatArea = new JTextArea();
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setEditable(false);

        JPanel ChatPanel = new JPanel(new BorderLayout());
        ChatPanel.add(new JLabel("Chat Frame"), BorderLayout.NORTH);
        ChatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        add(ChatPanel, BorderLayout.CENTER);

        // Input area
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendBtn = new JButton("Send");
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);


        // Member list
        memberList = new DefaultListModel<>();
        memberJList = new JList<>(memberList);
        JPanel memberPanel = new JPanel(new BorderLayout());
        memberPanel.add(new JLabel("Online User"), BorderLayout.NORTH);
        memberPanel.add(new JScrollPane(memberJList), BorderLayout.CENTER);
        memberPanel.setPreferredSize(new Dimension(150, 0));
        add(memberPanel, BorderLayout.EAST);


        // Control buttons

        chooseFileBtn = new JButton("Choose File");
        historyBtn = new JButton("History");

        JPanel controlPanel = new JPanel(new GridLayout(2, 1));
        controlPanel.add(chooseFileBtn);
        controlPanel.add(historyBtn);

        JPanel functionPanel = new JPanel(new GridLayout(1, 2));
        functionPanel.add(controlPanel);
        functionPanel.add(inputPanel);

        add(functionPanel, BorderLayout.SOUTH);

        addEventListeners(user);
    }


    public void addEventListeners(User user){
        // Remove previous listeners to avoid duplicate registrations
        for (ActionListener a : sendBtn.getActionListeners()) {
            sendBtn.removeActionListener(a);
        }
        for (ActionListener a : historyBtn.getActionListeners()) {
            historyBtn.removeActionListener(a);
        }

        sendBtn.addActionListener(e->{
            String content = inputField.getText();
            inputField.setText("");
            Message message = new Message(MessageType.GROUP_MESSAGE);
            message.setContent(content);
            message.setSender(user.getUserName());
            message.setGroupName(groupName);
            clientManager.sendMessage(message);
        });

        historyBtn.addActionListener(e->{
            // Clear previous history to avoid duplicates when requesting again
            clearHistoryArea();
            Message request = new Message(MessageType.HISTORY_REQUEST);
            request.setGroupName(groupName);
            request.setSender(user.getUserName());
            clientManager.sendMessage(request);
        });

    }

    public void setHistoryArea(String content, String time){
        historyArea.append(time + ": " + content);
        historyArea.append("\n");
    };

    public void clearHistoryArea(){
        historyArea.setText("");
    }

    public void clearOnlineUser(){
        memberList.clear();
    }

    public void setOnlineUser(String listUser){
        memberList.clear();
        if(listUser == null || listUser.isBlank()) return;
        String[] users = listUser.split(",");
        for(String u : users){
            memberList.addElement(u.trim());
        }
    }

    public void setChatArea(String content, String sender){
        chatArea.append(sender + ": " + content);
        chatArea.append("\n");
    };
}
