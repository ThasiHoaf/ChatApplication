package ChatSystem.server.dao;
import java.util.List;
import java.util.ArrayList;
import ChatSystem.shared.Group;
import ChatSystem.shared.GroupMember;
import ChatSystem.database.DBConnection;
public class GroupDAO {
    final DBConnection dbConnection;
    private List<Group> groups;
    private List<GroupMember> members;

    public GroupDAO(DBConnection connection) {
        this.dbConnection = connection;
        groups = dbConnection.loadGroups();
        members = dbConnection.loadGroupMembers();
    }

    public List<Group> getGroups() {
        groups = dbConnection.loadGroups();
        return groups;
    }

    public void addMember(String groupName, String UserName){
        // Avoid adding duplicate members for the same group
        for (GroupMember m : members) {
            if (m.getGroupName().equals(groupName) && m.getName().equals(UserName)) {
                return; // already a member
            }
        }
        GroupMember member = new GroupMember(groupName, UserName);
        members.add(member);
        dbConnection.saveGroupMember(groupName, UserName);
    }

    public List<GroupMember> getMembers() {
        members = dbConnection.loadGroupMembers();
        return members;
    }

    public void addGroup(Group group) {
        groups.add(group);
        dbConnection.saveGroup(group);
    }

    // public void removeGroup(Group group) {
    //     groups.remove(group);
    // }

}
