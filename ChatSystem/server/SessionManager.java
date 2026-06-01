package ChatSystem.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import ChatSystem.shared.Message;

public class SessionManager {
    
    private Map<String, ClientHandler> activeSessions = new ConcurrentHashMap<>();

    public void addSession(String username, ClientHandler handler) {
        activeSessions.put(username, handler);
    }

    public void removeSession(String username) {
        activeSessions.remove(username);
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
}
