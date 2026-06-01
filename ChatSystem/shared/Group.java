package ChatSystem.shared;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Group implements Serializable {
   
    private String groupName;
    private String createdBy;
    private String password;
    private List<GroupMember> members;

    public Group( String name, String createdBy, String password){
        this.groupName = name;
        this.createdBy = createdBy;
        this.password = password;
        this.members = new ArrayList<>();
    }

    public void addMember(GroupMember member){
        if(member != null){
            members.add(member);
        }
    }

    

    public void setGroupName(String name){
        this.groupName = name;
    }

    public void setCreatedBy(String createdBy){
        this.createdBy = createdBy;
    }

    public void setPassword(String password){
        this.password = password;
    }


    

    public String getGroupName(){
        return groupName;
    }

    public String getCreatedBy(){
        return createdBy;
    }
    
    public String getPassword(){
        return password;
    }


}
