package ChatSystem.client.ui;

import ChatSystem.shared.*;
import ChatSystem.client.*;
import ChatSystem.client.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {
    ClientManager clientManager;
    final User currentUser;
    final JTabbedPane tabbedPane;
    LobbyPanel lobbyPanel;
    HistoryPanel historyPanel;

    public User getCurrentUser(){
        return currentUser;
    }

    public void addTab(String namePanel, JPanel panel){
        tabbedPane.addTab(namePanel, panel);
    }
    public MainFrame(ClientManager clientManager, User currentUser) {
        this.clientManager = clientManager;
        this.currentUser = currentUser;

        // Không gọi connect ở đây nữa. Kết nối phải được thực hiện trước lúc đăng nhập.

        setTitle("Chat System - User: " + currentUser.getUserName());
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        
        // Add Lobby Panel
        lobbyPanel = new LobbyPanel(clientManager, currentUser);
        this.lobbyPanel = lobbyPanel;
        tabbedPane.addTab("Lobby", lobbyPanel);

        HistoryPanel historyPanel = new HistoryPanel(clientManager, currentUser);
        this.historyPanel = historyPanel;
        tabbedPane.addTab("History", historyPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    public void addGroupTab(String groupName) {
        GroupPanel groupPanel = new GroupPanel(clientManager, groupName, currentUser, true);
        tabbedPane.addTab(groupName, groupPanel);
    }

    public void addPrivateTab(String privateName){
        GroupPanel privatePanel = new GroupPanel(clientManager, privateName, currentUser, false);
        tabbedPane.addTab(privateName, privatePanel);
    }
    public void setLobbyFileList(String listFile){
        lobbyPanel.setFileList(listFile);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ClientManager clientManager = new ClientManager();
            LoginFrame loginFrame = new LoginFrame(clientManager);
            loginFrame.setVisible(true);
        });
    }
    public void printMessage(Message message){
        this.lobbyPanel.setChatArea(message);
    }

    public void printGroupMessage(Message message) {
        String groupName = message.getGroupName();
        String identifier = message.getTarget(); // target sẽ là groupName đối với tin nhắn nhóm, hoặc tên người gửi đối với tin nhắn riêng


        if(identifier == null || identifier.isBlank()){
            // Duyệt qua các tab để tìm đúng GroupPanel có tên tương ứng
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                if (tabbedPane.getTitleAt(i).equals(groupName)) {
                    Component comp = tabbedPane.getComponentAt(i);
                    if (comp instanceof GroupPanel) {
                        ((GroupPanel) comp).setChatArea(message.getContent(), message.getSender());
                    }
                    break;
                }
            }
        }
        else {
            // Đây là tin nhắn riêng, tìm tab có tên trùng với target (tên người gửi)
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                if (tabbedPane.getTitleAt(i).equals(identifier)) {
                    Component comp = tabbedPane.getComponentAt(i);
                    if (comp instanceof GroupPanel) {
                        ((GroupPanel) comp).setChatArea(message.getContent(), message.getSender());
                    }
                    break;
                }
            }
        }
    }
    public void setHistory(long messageId, String content, String time){
        historyPanel.setHistoryArea(messageId, content, time);
    }

    public void clearHistoryPanel(){
        historyPanel.clearHistory();
    }

    public void clearGroupHistory(String groupName){
        for(int i = 0; i < tabbedPane.getTabCount(); i ++){
            if(tabbedPane.getTitleAt(i).equals(groupName)){
                Component comp = tabbedPane.getComponentAt(i);
                if(comp instanceof GroupPanel){
                    ((GroupPanel) comp).clearHistoryArea();
                }
                break;
            }
        }
    }

    public void clearMemberList(){
        lobbyPanel.clearMemberList();
    }

    public void setMemberList(String listMember){
        lobbyPanel.setMemberList(listMember);
    }

    public void setGroupMemberList(String groupName, String listMember){
        for(int i = 0; i < tabbedPane.getTabCount(); i ++){
            if(tabbedPane.getTitleAt(i).equals(groupName)){
                Component comp = tabbedPane.getComponentAt(i);
                if(comp instanceof GroupPanel) {
                    ((GroupPanel) comp).setOnlineUser(listMember);
                }
                break;
            }
        }
    }

    public void setGroupFileList(String groupName, String listFile){
        for(int i = 0; i < tabbedPane.getTabCount(); i ++){
            if(tabbedPane.getTitleAt(i).equals(groupName)){
                Component comp = tabbedPane.getComponentAt(i);
                if(comp instanceof GroupPanel) {
                    ((GroupPanel) comp).setFileList(listFile);
                }
                break;
            }
        }
    }
    public void setGroupHistory(String groupName, String content, String time){
        for(int i = 0; i < tabbedPane.getTabCount(); i ++){
            if(tabbedPane.getTitleAt(i).equals(groupName)) {
                Component comp = tabbedPane.getComponentAt(i);
                if (comp instanceof GroupPanel) {
                    ((GroupPanel) comp).setHistoryArea(content, time);
                }
                break;
            }
        }
    }
}
