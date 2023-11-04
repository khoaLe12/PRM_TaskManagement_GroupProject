package models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "user")
public class User implements Serializable{
    @PrimaryKey(autoGenerate = true)
    private int userId;
    private String name;
    private String email;
    private String location;
    private String inviteCode;
    private String username;
    private String password;
    private String role;
    private String imageUrl;
    private boolean blocked;

    public User(String username, String password, String role, String inviteCode, String imageUrl, boolean blocked, String name, String email, String location) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.inviteCode = inviteCode;
        this.imageUrl = imageUrl;
        this.blocked = blocked;
        this.name = name;
        this.email = email;
        this.location = location;
    }

    @Ignore
    public User(int id, String username, String password, String role, String inviteCode, String imageUrl, boolean blocked, String name, String email, String location) {
        this.userId = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.inviteCode = inviteCode;
        this.imageUrl = imageUrl;
        this.blocked = blocked;
        this.name = name;
        this.email = email;
        this.location = location;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int id) {
        this.userId = id;
    }

    public String getLocation(){ return location; }

    public void setLocation(String location){ this.location = location; }

    public String getEmail(){ return email; }

    public void setEmail(String email){
        this.email = email;
    }

    public String getName(){ return name; }

    public void setName(String name){
        this.name = name;
    }

    public boolean getBlocked(){ return blocked; }

    public void setBlocked(boolean blocked){
        this.blocked = blocked;
    }

    public String getImageUrl(){
        return imageUrl;
    }

    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }

    public String getInviteCode(){ return inviteCode; }

    public void setInviteCode(String inviteCode){
        this.inviteCode = inviteCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
