package ChatSystem.server.dao;

import ChatSystem.shared.Message;
import ChatSystem.database.DBConnection;

import java.util.ArrayList;
import java.util.List;


public class MessageDAO {
    private DBConnection dbConnection;
    private List<Message> messages;
    public MessageDAO(DBConnection connection){
        this.dbConnection = connection;
        this.messages = new ArrayList<>();
    }

    public List<Message> getMessages() {
        messages = dbConnection.loadMessage();
        return messages;
    }

    public List<Message> loadMessage(){
        return dbConnection.loadMessage();
    }



    public void addMessage(Message message){
        // Server is authoritative for timestamps: set to now before persisting
//        message.setTimestamp(LocalDateTime.now());
        messages.add(message);
        dbConnection.saveMessage(message);
    }

    public void removeMessage(Message message){
        // Prefer id-based removal when available
        if (message.getId() != null) {
            removeMessageById(message.getId());
            return;
        }
        messages.remove(message);
        // Fallback: remove by content
        try {
            java.sql.Connection conn = DBConnection.getConnection();
            java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM messages WHERE content = ?");
            ps.setString(1, message.getContent());
            ps.executeUpdate();
            ps.close();
        } catch (java.sql.SQLException e){
            e.printStackTrace();
        }

    }

    public void removeMessageByContent(String content){
        // Remove from in-memory list
        messages.removeIf(m -> content != null && content.equals(m.getContent()));
        // Remove from DB
        try {
            java.sql.Connection conn = DBConnection.getConnection();
            java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM messages WHERE content = ?");
            ps.setString(1, content);
            ps.executeUpdate();
            ps.close();
        } catch (java.sql.SQLException e){
            e.printStackTrace();
        }
    }

    public void removeMessageById(long id){
        messages.removeIf(m -> m.getId() != null && m.getId() == id);
        try {
            java.sql.Connection conn = DBConnection.getConnection();
            java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM messages WHERE id = ?");
            ps.setLong(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (java.sql.SQLException e){
            e.printStackTrace();
        }
    }
    



}
