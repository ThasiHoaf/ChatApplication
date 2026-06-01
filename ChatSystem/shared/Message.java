package ChatSystem.shared;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageType type;
    private String sender;
    private String target;
    private String groupName;
    private byte[] fileData;
    private String fileName;
    private boolean success;
    private String info;
    private String content;
    private LocalDateTime timestamp;
    // DB primary key id (nullable for messages not yet persisted)
    private Long id;


    //Constructor
    public Message(MessageType type, String sender, String target, String groupName, byte[] fileData, String fileName, boolean success, String info, String content){
        this.type = type;
        this.sender = sender;
        this.target = target;
        this.groupName = groupName;
        this.fileData = fileData;
        this.fileName = fileName;
        this.success = success;
        this.info = info;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
     


    public Message(MessageType type) {
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }
    //Getters
    public long getSerialVersionUID(){
        return serialVersionUID;
    }
    public String getSender() {
        return sender;
    }   
    public String getTarget() {
        return target;
    }
    public String getGroupName() {
        return groupName;
    }
    public byte[] getFileData() {
        return fileData;
    }
    public String getFileName() {
        return fileName;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public boolean isSuccess() {
        return success;
    }
    public String getInfo() {
        return info;
    }
    public String getContent() {
        return content;
    }

    public MessageType getMessageType(){
        return type;
    }
    //Setters
    public void setSender(String sender) {
        this.sender = sender;   
    }
    public void setTarget(String target) {
        this.target = target;
    }
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public void setInfo(String info) {
        this.info = info;
    }
    public void setContent(String content) {
        this.content = content;
    }
    
    // id getter/setter for DB-backed messages
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
