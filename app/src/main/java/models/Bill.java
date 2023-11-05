package models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "bill")
public class Bill {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String content;
    private float money;
    private Date createdDate;
    private int recipientId;
    private int senderId;
    private int projectId;

    @Ignore
    public Bill(int id, String title, String content, float money, Date createdDate, int recipientId, int senderId, int projectId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.money = money;
        this.createdDate = createdDate;
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.projectId = projectId;
    }

    public Bill(String title, String content, float money, Date createdDate, int recipientId, int senderId, int projectId) {
        this.title = title;
        this.content = content;
        this.money = money;
        this.createdDate = createdDate;
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.projectId = projectId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProjectId(){
        return projectId;
    }

    public void setProjectId(int projectId){
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float getMoney() {
        return money;
    }

    public void setMoney(float money) {
        this.money = money;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }
}
