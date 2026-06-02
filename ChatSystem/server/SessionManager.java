package ChatSystem.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import ChatSystem.shared.Message;
import java.util.function.Consumer;

public class SessionManager {
    
    private Map<String, ClientHandler> activeSessions = new ConcurrentHashMap<>();


    // (MỚI) Callback để cập nhật giao diện
    private Consumer<List<String>> onUserListChanged;

    public void setOnUserListChanged(Consumer<List<String>> onUserListChanged) {
        this.onUserListChanged = onUserListChanged;
    }

    public void addSession(String username, ClientHandler handler) {
        activeSessions.put(username, handler);
        notifyUI(); // Gọi cập nhật
    }

    public void removeSession(String username) {
        activeSessions.remove(username);
        notifyUI(); // Gọi cập nhật
    }
    

    public ClientHandler getHandler(String username) {
        return activeSessions.get(username);
    }

    public List<String> getOnlineUsers() {
        return new ArrayList<>(activeSessions.keySet());
    }
    
    public void broadcast(Message message) {
        for (ClientHandler handler : activeSessions.values()) {
            handler.sendMessage(message);
        }
    }

    private void notifyUI() {
        if (onUserListChanged != null) {
            onUserListChanged.accept(getOnlineUsers());
        }
    }
}
