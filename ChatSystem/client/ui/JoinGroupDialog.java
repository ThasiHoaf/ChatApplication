package ChatSystem.client.ui;

import javax.swing.*;
import java.awt.*;
import ChatSystem.client.ClientManager;
import ChatSystem.shared.*;



public class JoinGroupDialog extends JDialog {
    private JTextField idField;
    private JPasswordField passwordField;
    private JButton joinButton;
    private JButton cancelButton;
    private ClientManager manager; 
    private User user;

    public JoinGroupDialog(JFrame parent, ClientManager manager, User user) {
        super(parent, "Join Group", true);
        this.user = user;
        this.manager = manager;
        setLayout(new GridLayout(3, 2, 10, 10));

        add(new JLabel("Group Name:"));
        idField = new JTextField();
        add(idField);

        add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        add(passwordField);

        joinButton = new JButton("Join");
        cancelButton = new JButton("Cancel");
        add(joinButton);
        add(cancelButton);

        addActionListener();
        setLocationRelativeTo(null);
        this.pack();
    
    }

    public void addActionListener(){
        joinButton.addActionListener(e -> {
            String groupName = idField.getText().trim();
            String password = String.copyValueOf(passwordField.getPassword());

            if(groupName.isBlank() || password.isBlank()){
                JOptionPane.showMessageDialog(this, "Please enter group name and Password");
    
            } else {
                Message message = new Message(MessageType.JOIN_GROUP);
                message.setGroupName(groupName);
                message.setContent(password);
                message.setSender(user.getUserName());

                manager.sendMessage(message);

                this.setVisible(false);
                dispose();
            }
        });

        cancelButton.addActionListener(e->{
            this.setVisible(false);
            dispose();
        });
    }
}
