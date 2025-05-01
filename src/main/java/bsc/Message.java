/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package bsc;

public class Message {
    
    private int messageId;
    private String username;
    private String content;
    private boolean isRead;
    private String timestamp;
    private String toDept;
    private String file;
    private String file_type;
    
    public Message(int messageId, String username, String content, boolean isRead, String timestamp, String toDept, String file, String file_type) {
        this.messageId = messageId;
        this.username = username;
        this.content = content;
        this.isRead = isRead;
        this.timestamp = timestamp;
        this.toDept = toDept;
        this.file = file;
        this.file_type = file_type;
    }
    
    public String getFile_Type() {
        return file_type;
    }

    public String getFile() {
        return file;
    }
    public String getToDept() {
        return toDept;
    }
    public String getTS() {
        return timestamp;
    }
    public int getMessageId() {
        return messageId;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }
    
    @Override
    public String toString() {
        String message = username + " (" + timestamp + "): " + content + (isRead ? "" : " (*)");
    return truncateMessage(message, 40);
}
    
    public String truncateMessage(String message, int maxLength) {
   if (message.length() > maxLength) {
        return message.substring(0, maxLength) + "...";
    }
    return message;
}

}

