package ChatSystem.server.dao;

import ChatSystem.shared.User;
import ChatSystem.database.DBConnection;


import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private List<User>  users;
    private DBConnection dbConnection;
    public UserDAO(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
        this.users = dbConnection.loadUsers();
    }

    public List<User> getUsers() {
        return dbConnection.loadUsers();
    }

    public void addUser(User user) {
        this.users.add(user);
        dbConnection.saveUser(user);
    }

    public void removeUser(User user) {
        this.users.remove(user);
        dbConnection.removeUser(user.getUserName());
    }


    public User getUser(String username){
        for(User u : users){
            if(u.getUserName().equals(username)){
                return u;
            }
        }
        return null;
    }
    
}
