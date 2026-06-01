package ChatSystem.shared;

public class User {
    private String userName;

    private String password;

    public User(String name, String password){
        this.userName = name;
        this.password = password;
    }

    public String getUserName(){
        return userName;    
    }
    public String getPassword(){
        return password;
    }
}
