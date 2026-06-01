package ChatSystem.shared;

public class GroupMember {
    private String groupName;
    private String name;

    public GroupMember(String groupName, String name) {
        this.groupName = groupName;
        this.name = name;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getName() {
        return name;
    }
    public void setGroupName(String groupName){
        this.groupName = groupName;
    }
    public void setName(String name){
        this.name = name;
    }
}
