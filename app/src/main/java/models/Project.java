package models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.List;

@Entity(tableName = "project")
public class Project {
    @PrimaryKey(autoGenerate = true)
    private int projectId;
    private String title;
    private String content;
    private Date createdDate;
    private String location;

    //In Progress, End
    private int status;
    private List<String> filePaths;
    private int managerId;

    public Project(int status, String title, String content, Date createdDate, List<String> filePaths, int managerId, String location) {
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
        this.filePaths = filePaths;
        this.managerId = managerId;
        this.status = status;
        this.location = location;
    }

    @Ignore
    public Project(int status, int projectId, String title, String content, Date createdDate, List<String> filePaths, int managerId, String location) {
        this.projectId = projectId;
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
        this.filePaths = filePaths;
        this.managerId = managerId;
        this.status = status;
        this.location = location;
    }

    public int getStatus(){
        return status;
    }

    public void setStatus(int status){
        this.status = status;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public List<String> getFilePaths() {
        return filePaths;
    }

    public void setFilePaths(List<String> filePaths) {
        this.filePaths = filePaths;
    }

    public int getManagerId(){
        return managerId;
    }

    public void setManagerId(int id){
        managerId = id;
    }

    public String getLocation(){
        return location;
    }

    public void setLocation(String location){
        this.location = location;
    }
}
