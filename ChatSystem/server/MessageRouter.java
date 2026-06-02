package ChatSystem.server;

import ChatSystem.server.dao.*;
import ChatSystem.shared.*;

import java.util.ArrayList;
import java.nio.file.*;
import java.util.stream.Collectors;
import java.util.List;

public class MessageRouter {
    final SessionManager sessionManager;
    final UserDAO userDAO;
    final GroupDAO groupDAO;
    final MessageDAO messageDAO;

    public MessageRouter(SessionManager sessionManager, UserDAO userDAO, GroupDAO groupDAO, MessageDAO messageDAO) {
        this.sessionManager = sessionManager;
        this.userDAO = userDAO;
        this.groupDAO = groupDAO;
        this.messageDAO = messageDAO;
    }

    // Called by ClientHandler inside its run() loop
    public void routeMessage(Message message, ClientHandler senderHandler) {
        switch (message.getMessageType()) {
            case LOGIN:
                handleLogin(message, senderHandler);
                break;
            case REGISTER:
                handleRegister(message, senderHandler);
                break;
            case MESSAGE:
                handlePrivateMessage(message, senderHandler);
                break;
            case GROUP_MESSAGE:
                handleGroupMessage(message, senderHandler);
                break;
            case CREATE_GROUP:
                handleCreateGroup(message, senderHandler);
                break;
            case HISTORY_DELETE:
                handleHistoryDelete(message, senderHandler);
                break;
            case HISTORY_REQUEST:
                handleHistoryRequest(message, senderHandler);
                break;
            case FILE_MESSAGE:
                handleFileMessage(message, senderHandler);
                break;
            case LOGOUT:
                handleLogout(message, senderHandler);
                break;

            case JOIN_GROUP:
                handleJoinGroup(message, senderHandler);
                break;

            case USER_LIST:
                handleUserList(message, senderHandler);
                break;

            case FILE_DOWNLOAD:
                handleDownloadFile(message, senderHandler);
                break;

            case FILE_LIST:
                handleFileList(message, senderHandler);
                break;

            default:
                System.err.println("Unknown message type: " + message.getMessageType());
                Message errorMessage = new Message(MessageType.ERROR);
                errorMessage.setSuccess(false);
                errorMessage.setInfo("Unknown message type");
                senderHandler.sendMessage(errorMessage);
                break;
        }
    }

    private void handleFileList(Message message, ClientHandler handler) {
        String groupName = message.getGroupName();
        Message fileListMessage = new Message(MessageType.FILE_LIST);
        List<String> fileNames = new ArrayList<>();
        if (groupName == null) {
            // return files in lobby
            try {
                fileNames = Files.list(Paths.get("downloads"))
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toList());
            }
            catch (Exception e) {
                System.err.println("Error reading files: " + e.getMessage());
            }
        } else {
            // return files in the specified group
            for (Message m : messageDAO.getMessages()) {
                if (groupName.equals(m.getGroupName()) && m.getFileName() != null) {
                    fileNames.add(m.getFileName());
                }
            }
        }
        fileListMessage.setSuccess(true);
        fileListMessage.setContent(String.join(",", fileNames));
        fileListMessage.setGroupName(groupName);
        handler.sendMessage(fileListMessage);
    }

    private void handleFileMessage(Message message, ClientHandler handler) {
        // Save file message to DB
        messageDAO.addMessage(message);
        message.setSuccess(true);
        String groupName = message.getGroupName();

        if (groupName == null) {
            // broadcast to all online users
            for (String username : sessionManager.getOnlineUsers()) {
                ClientHandler h = sessionManager.getHandler(username);
                if (h != null) h.sendMessage(message);
            }
            return;
        }

        // send to members of the specified group (dedupe)
        List<Group> groups = groupDAO.getGroups();
        for (Group group : groups){
            if(group.getGroupName().equals(groupName)){
                List<GroupMember> members = groupDAO.getMembers();
                java.util.Set<String> sent = new java.util.HashSet<>();
                for (GroupMember member : members){
                    if (!groupName.equals(member.getGroupName())) continue;
                    String username = member.getName();
                    if (sent.contains(username)) continue;
                    sent.add(username);
                    ClientHandler targetHandler = sessionManager.getHandler(username);
                    if(targetHandler != null){
                        targetHandler.sendMessage(message);
                    }
                }
                return;
            }
        }
    }

    private void handleDownloadFile(Message message, ClientHandler handler){
        String fileName = message.getFileName();
        Path filePath = Paths.get("downloads", fileName);
        if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
            try {
                byte[] fileData = Files.readAllBytes(filePath);
                Message fileMessage = new Message(MessageType.FILE_DOWNLOAD);
                fileMessage.setFileData(fileData);
                fileMessage.setFileName(fileName);
                fileMessage.setContent(fileName);
                fileMessage.setSuccess(true);
                fileMessage.setSender("SERVER");
                handler.sendMessage(fileMessage);
            } catch (Exception e) {
                System.err.println("Error reading file: " + e.getMessage());
                Message errorMessage = new Message(MessageType.ERROR);
                errorMessage.setSuccess(false);
                errorMessage.setInfo("Error reading file: " + e.getMessage());
                handler.sendMessage(errorMessage);
            }
        } else {
            Message errorMessage = new Message(MessageType.ERROR);
            errorMessage.setSuccess(false);
            errorMessage.setInfo("File not found");
            handler.sendMessage(errorMessage);
        }
    }


    private void handleHistoryDelete(Message message, ClientHandler handler) {
        String identifier = message.getContent();
        try {
            // If identifier is numeric, delete by id; otherwise fallback to content-based deletion
            try {
                long id = Long.parseLong(identifier);
                messageDAO.removeMessageById(id);
            } catch (NumberFormatException nfe) {
                messageDAO.removeMessageByContent(identifier);
            }

            Message response = new Message(MessageType.HISTORY_DELETE);
            response.setSuccess(true);
            response.setContent(identifier);
            // Propagate group context so clients know which history to refresh
            response.setGroupName(message.getGroupName());
            // Notify requesting client
            handler.sendMessage(response);
            // Broadcast deletion event so clients can update their UI if desired
            sessionManager.broadcast(response);
        } catch (Exception e) {
            Message errorMessage = new Message(MessageType.HISTORY_DELETE);
            errorMessage.setSuccess(false);
            errorMessage.setInfo("Delete failed: " + e.getMessage());
            handler.sendMessage(errorMessage);
        }
    }

    private void handleHistoryRequest(Message message, ClientHandler handler) {
        String sender = message.getSender();
        String groupName = message.getGroupName();

        if (groupName != null) {
            // TRƯỜNG HỢP 1: Yêu cầu lịch sử tin nhắn của Group
            // Inform client to clear existing history before sending entries
            Message clearMsg = new Message(MessageType.HISTORY_REQUEST);
            clearMsg.setSuccess(true);
            clearMsg.setInfo("CLEAR");
            clearMsg.setGroupName(groupName);
            handler.sendMessage(clearMsg);

            for (Message his : messageDAO.getMessages()) {
                // Sử dụng groupName (đã chắc chắn khác null) để gọi .equals() nhằm tránh NPE
                if (groupName.equals(his.getGroupName()) && sender.equals(his.getSender())) {
                    Message response = new Message(MessageType.HISTORY_REQUEST);
                    response.setSuccess(true);
                    response.setSender(his.getSender()); // Lấy sender gốc của tin nhắn lịch sử
                    response.setGroupName(groupName);
                    response.setContent(his.getContent());
                    response.setTimestamp(his.getTimestamp());
                    // propagate DB id for client-side operations (deletion)
                    response.setId(his.getId());
                    // include filename so clients can populate Files/Media lists
                    response.setFileName(his.getFileName());
                    handler.sendMessage(response);
                }
            }
        } else {
            // TRƯỜNG HỢP 2: Yêu cầu lịch sử tin nhắn của Lobby (không có group)
            // Inform client to clear existing history before sending entries
            Message clearMsg = new Message(MessageType.HISTORY_REQUEST);
            clearMsg.setSuccess(true);
            clearMsg.setInfo("CLEAR");
            clearMsg.setGroupName(null);
            handler.sendMessage(clearMsg);

            for (Message his : messageDAO.getMessages()) {
                // Chỉ lấy những tin nhắn mà groupName là null (tin nhắn hệ thống/lobby)
                if (his.getGroupName() == null) {

                    if (sender.equals(his.getSender())) {
                        Message response = new Message(MessageType.HISTORY_REQUEST);
                        response.setSuccess(true);
                        response.setSender(sender); // Lấy sender gốc của tin nhắn lịch sử
                        response.setContent(his.getContent());
                        response.setTimestamp(his.getTimestamp());
                        // propagate DB id for client-side operations (deletion)
                        response.setId(his.getId());
                        // include filename for client-side Files/Media population
                        response.setFileName(his.getFileName());
                        handler.sendMessage(response);
                    }
                }
            }
        }
    }

    private void handleUserList(Message message, ClientHandler handler) {
        String groupName = message.getGroupName();
        Message userListMessage = new Message(MessageType.USER_LIST);
        List<String> onlineUser = sessionManager.getOnlineUsers();
        List<String> result = new ArrayList<>();
        if (groupName == null) {
            // return currently online users
            result = sessionManager.getOnlineUsers();
        } else {
            userListMessage.setGroupName(message.getGroupName());
            // return members of the given group
            for (GroupMember m : groupDAO.getMembers()){
                if (groupName.equals(m.getGroupName())){
                    result.add(m.getName());
                }
            }
        }
        userListMessage.setSuccess(true);
        userListMessage.setContent(String.join(",", result));
        for (int i = 0; i < onlineUser.size(); i++){
            ClientHandler handler1 = sessionManager.getHandler(onlineUser.get(i));
            handler1.sendMessage(userListMessage);
        }
    }

    private synchronized void handleCreateGroup(Message message, ClientHandler handler){

        String groupName = message.getGroupName();
        if(groupName == null || groupName.isBlank()){
            String password = message.getContent();
            String sender = message.getSender();
            for (Group group : groupDAO.getGroups()){
                if(group.getGroupName().equals(groupName)){
                    Message errorMessage = new Message(MessageType.CREATE_GROUP);
                    errorMessage.setSuccess(false);
                    errorMessage.setInfo("Group name already exists");
                    System.out.println("Group with name " + message.getGroupName() + " has already existed.");
                    handler.sendMessage(errorMessage);
                    return;
                }
            }
            Group newGroup = new Group(groupName, sender,password);
            groupDAO.addGroup(newGroup);
            Message successMessage = new Message(MessageType.CREATE_GROUP);
            successMessage.setSuccess(true);
            successMessage.setInfo("Group created successfully");
            System.out.println(message.getGroupName() + " created successfully");
            handler.sendMessage(successMessage);
            sessionManager.broadcast(successMessage);
            return;
        } else {
            String target = message.getTarget();
            Message privateMessage = new Message(MessageType.CREATE_GROUP);
            privateMessage.setSender(message.getSender());
            privateMessage.setTarget(target);
            privateMessage.setSuccess(true);
            privateMessage.setInfo("Private chat created successfully");
            ClientHandler targetHandler = sessionManager.getHandler(target);
            if (targetHandler != null) {
                targetHandler.sendMessage(privateMessage);
            } else {
                Message errorMessage = new Message(MessageType.CREATE_GROUP);
                errorMessage.setSuccess(false);
                errorMessage.setInfo("Target user is offline");
                handler.sendMessage(errorMessage);
            }

        }

    }

    private void handleLogout(Message message, ClientHandler handler) {
        try {
            String username = message.getSender();
            sessionManager.removeSession(username);
            Message successMessage = new Message(MessageType.LOGOUT);
            successMessage.setSuccess(true);
            successMessage.setInfo("Logged out successfully");
            System.out.println(message.getSender() + " Logged out successfully");
            sessionManager.broadcast(successMessage);
            handler.sendMessage(successMessage);

        } catch (Exception e){
            System.err.println("Error: " + e.getMessage());
            Message errorMessage = new Message(MessageType.LOGOUT);
            errorMessage.setSuccess(false);
            errorMessage.setInfo("Logout failed");
            handler.sendMessage(errorMessage);
        }

    }

    private synchronized void handleJoinGroup(Message message, ClientHandler handler) {
        try {
            String groupName = message.getGroupName();
            String userName = message.getSender();
            for (Group group : groupDAO.getGroups()){
                if(group.getGroupName().equals(groupName) && group.getPassword().equals(message.getContent())){
                    group.addMember(new GroupMember(groupName, userName));
                    groupDAO.addMember(groupName, userName);
                    Message successMessage = new Message(MessageType.JOIN_GROUP);
                    successMessage.setGroupName(groupName);

                    successMessage.setSuccess(true);
                    successMessage.setInfo("Joined group successfully");
                    System.out.println(message.getSender() + " joined " + message.getGroupName() + " successfully" );
                    handler.sendMessage(successMessage);
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            Message errorMessage = new Message(MessageType.JOIN_GROUP);
            errorMessage.setSuccess(false);
            errorMessage.setInfo("Group not found");
            handler.sendMessage(errorMessage);
        }
    }

    private synchronized void handleLogin(Message message, ClientHandler handler) {
        // 1. Check DB via userDAO to verify password
        // 2. If valid, register with SessionManager
        // 3. Send success response back to handler
        String name = message.getSender();
        String password = message.getContent();
        User user = userDAO.getUser(name);
        if (user != null && user.getPassword().equals(password.trim())) {
            sessionManager.addSession(name, handler);
            Message successMessage = new Message(MessageType.LOGIN);
            successMessage.setSender(name);
            successMessage.setContent(password);
            successMessage.setSuccess(true);
            handler.sendMessage(successMessage);
            System.out.println(message.getSender() + " logged in successfully!");
        } else {
            Message errorMessage = new Message(MessageType.LOGIN);
            errorMessage.setSuccess(false);
            errorMessage.setInfo("Invalid username or password");
            System.out.println("Log in: Invalid username or password");
            handler.sendMessage(errorMessage);
        }

    }

    private synchronized void handlePrivateMessage(Message message, ClientHandler handler) {
        // 1. Lưu tin nhắn vào Database
        messageDAO.addMessage(message);
        message.setSuccess(true);
        
        String target = message.getTarget();

        if (target == null || target.trim().isEmpty()) {
            // TRƯỜNG HỢP 1: Tin nhắn từ LobbyPanel (target rỗng) -> Gửi cho toàn bộ người dùng ĐANG ONLINE
            for (String onlineUser : sessionManager.getOnlineUsers()) {
                ClientHandler activeClient = sessionManager.getHandler(onlineUser);
                if (activeClient != null) {
                    activeClient.sendMessage(message);
                }
            }
            System.out.println(message.getSender() + " sent a message to Lobby.");
        } else {
            // TRƯỜNG HỢP 2: Tin nhắn riêng tư 1-1 -> Chỉ gửi cho đúng target
            ClientHandler targetHandler = sessionManager.getHandler(target);
            if (targetHandler != null) {
                targetHandler.sendMessage(message);
            }
            
            System.out.println(message.getSender() + " sent a private message to " + target);
        }
    }

    private synchronized void handleGroupMessage(Message message, ClientHandler handler) {
        // 1. Save to database using messageDAO
        // 2. Fetch group members from groupDAO
        // 3. Loop through members, check if they are online in SessionManager, and forward the message
        messageDAO.addMessage(message);
        message.setSuccess(true);
        String groupName = message.getGroupName();
        List<Group> groups = groupDAO.getGroups();

        for (Group group : groups){
            if(group.getGroupName().equals(groupName)){
                List<GroupMember> members = groupDAO.getMembers();
                java.util.Set<String> sent = new java.util.HashSet<>();
                for (GroupMember member : members){
                    // Only forward to members of this group
                    if (!groupName.equals(member.getGroupName())) continue;
                    String username = member.getName();
                    if (sent.contains(username)) continue; // avoid duplicate sends
                    sent.add(username);
                    ClientHandler targetHandler = sessionManager.getHandler(username);
                    if(targetHandler != null){
                        targetHandler.sendMessage(message);
                    }
                }
                return;
            }

        }



    }

    private synchronized void handleRegister(Message message, ClientHandler handler) {
        String userName = message.getSender();
        String password = message.getContent();
        User user = userDAO.getUser(userName);
        if(user == null){
            User newUser = new User(userName, password);
            userDAO.addUser(newUser);
            Message successMessage = new Message(MessageType.REGISTER);
            successMessage.setSuccess(true);
            successMessage.setInfo("Registered successfully");
            System.out.println(message.getSender() + " Registered successfully");
            handler.sendMessage(successMessage);
        } else {
            Message errorMessage = new Message(MessageType.REGISTER);
            errorMessage.setSuccess(false);
            errorMessage.setInfo("Username already exists");
            handler.sendMessage(errorMessage);  
        }

    }
}
