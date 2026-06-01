package ChatSystem.database;

import java.io.IOException;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.List;
import ChatSystem.shared.*;

import java.time.LocalDateTime;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;


public class DBConnection {
    public static Connection connection = null;

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Chỉ load config khi cần tạo mới
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream("ChatSystem/database/db.properties")) {
                    props.load(fis);
                }

                String url = props.getProperty("db.url");
                String username = props.getProperty("db.username").trim();
                String password = props.getProperty("db.password").trim();

                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(url, username, password);
            } catch (Exception e) {
                throw new SQLException("Cannot connect to DB", e);
            }
        }
        return connection;
    }
    
    public static void closeConnection() {
        if(connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Failed to close database connection: " + e.getMessage());
            }
        }
    }

    public List<User> loadUsers() {
        try {
            connection = getConnection();
            Statement st = connection.createStatement();

            String query = "select * from users";
            ResultSet rs = st.executeQuery(query);

            List<User> users = new ArrayList<>();
            while (rs.next()){
                String username = rs.getString("username");
                String password = rs.getString("password");
                // String id = rs.getString("id");

                User user = new User(username, password);
                users.add(user);
            }
            return users;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void saveUser(User user) {
        try {
            connection = getConnection();
            String query = "INSERT INTO users(username, password) VALUES (?, ?)";
            PreparedStatement ps = connection.prepareStatement(query);

            ps.setString(1, user.getUserName().trim());
            ps.setString(2, user.getPassword().trim());
            ps.executeUpdate();
        } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    
        
    public void removeUser(String username){
        try {
            connection = getConnection();
            String query = "DELETE FROM users WHERE username = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, username);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Group> loadGroups() {
        List<Group> groups = new ArrayList<>();
        String query = "SELECT group_name, created_by, password FROM groups";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                String name = rs.getString("group_name");
                Group group = new Group(name, rs.getString("created_by"), rs.getString("password"));

                groups.add(group);
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return groups;
    }
    

    public List<Message> loadMessage(){
        List<Message> messages = new ArrayList<>();
        try {
            connection = getConnection();
            Statement st = connection.createStatement();
            String query = "select * from messages";
            ResultSet rs = st.executeQuery(query);
            while(rs.next()){
                MessageType type = MessageType.valueOf(rs.getString("msg_type"));
                String sender = rs.getString("sender");
                String target = rs.getString("target");
                String content = rs.getString("content");
                String groupName = rs.getString("group_name");
                byte[] fileData = rs.getBytes("file_data");
                String fileName = rs.getString("file_name");
                boolean success = rs.getBoolean("success");
                String info = rs.getString("info");
                Message message = new Message(type, sender, target, groupName, fileData, fileName, success, info, content);
                // set DB id (handle SQL NULLs properly)
                long id = rs.getLong("id");
                if (rs.wasNull()) {
                    message.setId(null);
                } else {
                    message.setId(id);
                }
                // read timestamp from DB and set it on the message
                try {
                    Timestamp ts = rs.getTimestamp("timestamp");
                    if (ts != null) {
                        message.setTimestamp(ts.toLocalDateTime());
                    }
                } catch (Exception e) {
                    // ignore and leave existing timestamp
                }
                messages.add(message);
            }
        } catch (SQLException exx){
            System.err.println(exx.getMessage());
        }
        return messages;
    }

    public List<GroupMember> loadGroupMembers() {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("select * from group_members")) {
                ResultSet rs = ps.executeQuery();
                List<GroupMember> members = new ArrayList<>();
    
                while (rs.next()) {
                    String groupName = rs.getString("group_name");
                    String username = rs.getString("username");
    
                    GroupMember member = new GroupMember(groupName, username);
                    members.add(member);
                }
    
                return members;
            } catch (SQLException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
    }

    public void saveGroup(Group group) {
        try {
            connection = getConnection();
            Statement st = connection.createStatement();

            String query = String.format("insert into groups(group_name, created_by, password) values ('%s', '%s', '%s')", 
                group.getGroupName(), group.getCreatedBy(), group.getPassword());

                st.executeUpdate(query);
        } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    public void saveGroupMember(String groupName, String userName){
        String query  = "insert into group_members(group_name, username)" + "values(?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, groupName);
            ps.setString(2, userName);
            ps.execute();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    public void saveMessage(Message message) {
    // Use PostgreSQL RETURNING id to reliably get generated PK
    String query = "INSERT INTO messages (msg_type, sender, target, group_name, file_data, file_name, success, info, content, timestamp) "
                 + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

    try (Connection connection = getConnection();
         PreparedStatement ps = connection.prepareStatement(query)) {

        ps.setString(1, message.getMessageType() != null ? message.getMessageType().toString() : null);
        ps.setString(2, message.getSender());
        ps.setString(3, message.getTarget());
        ps.setString(4, message.getGroupName());
        ps.setBytes(5, message.getFileData());
        ps.setString(6, message.getFileName());
        ps.setBoolean(7, message.isSuccess());
        ps.setString(8, message.getInfo());
        ps.setString(9, message.getContent());
        // store timestamp (use message timestamp if provided, otherwise now)
        LocalDateTime ldt = message.getTimestamp() != null ? message.getTimestamp() : LocalDateTime.now();
        ps.setTimestamp(10, Timestamp.valueOf(ldt));

        try (ResultSet rs = ps.executeQuery()) {
            if (rs != null && rs.next()) {
                long id = rs.getLong("id");
                message.setId(id);
            }
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

}

    
}
  
