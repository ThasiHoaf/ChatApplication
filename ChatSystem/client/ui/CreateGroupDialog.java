package ChatSystem.client.ui;
import java.awt.*;
import javax.swing.*;
import ChatSystem.client.*;
import ChatSystem.shared.*;


public class CreateGroupDialog extends JDialog {

    private JTextField groupNameField;
    private JPasswordField passwordField;
    private JButton createButton;
    private JButton cancelButton;
    private ClientManager clientManager;
    private User user;

    public CreateGroupDialog(JFrame parent, ClientManager manager, User user){

        super(parent, "Create Group", true);
        this.clientManager = manager;
        this.user = user;

        groupNameField = new JTextField(15);
        passwordField = new JPasswordField(15);

        createButton = new JButton("Create");
        cancelButton = new JButton("Cancel");
        setLayout(new GridLayout(3, 2));
        add(new JLabel("Group Name:"));
        add(groupNameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(createButton);
        add(cancelButton);
        pack();
        setLocationRelativeTo(parent);
        addEventListener();

    }

    public String getGroupName(){
        return groupNameField.getText().trim();
    }

    public char[] getPassword(){
        return (passwordField.getPassword());
    }

    public void addEventListener(){
        createButton.addActionListener(e -> {
            String groupName = getGroupName();
            char[] password = getPassword();
            String sender = user.getUserName();

            if(groupName.isBlank()){
                JOptionPane.showMessageDialog(this, "Please enter group name.");
            } else {
                Message message = new Message(MessageType.CREATE_GROUP);
                message.setGroupName(groupName);
                message.setContent(String.copyValueOf(password));
                message.setSender(sender);

                clientManager.sendMessage(message);
                this.setVisible(false);
                dispose();

            }
            
        });

        cancelButton.addActionListener(e -> {
            this.setVisible(false);
            dispose();
        });
    }
}



