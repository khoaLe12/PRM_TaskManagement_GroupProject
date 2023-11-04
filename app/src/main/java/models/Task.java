package models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Entity(tableName = "task")
public class Task {
    @PrimaryKey(autoGenerate = true)
    private int taskId;
    private String title;
    private String description;
    private Date deadline;

    // In Progress, Done, Overdue
    private int status;
    // Đang chờ, xác nhận, chưa đạt
    private int confirmStatus;

    private String report;
    private Map<String,String> filePaths;
    private String comments;

    private int creatorId;
    private int projectId;

    @Ignore
    public Task(int id, String title, String description, Date deadline, int status, String report, Map<String,String> filePaths, String comments, int creatorId, int projectId, int confirmStatus) {
        this.taskId = id;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.status = status;
        this.report = report;
        this.filePaths = filePaths;
        this.comments = comments;
        this.creatorId = creatorId;
        this.projectId = projectId;
        this.confirmStatus = confirmStatus;
    }

    public Task(String title, String description, Date deadline, int status, String report, Map<String,String> filePaths, String comments, int creatorId, int projectId, int confirmStatus) {
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.status = status;
        this.report = report;
        this.filePaths = filePaths;
        this.comments = comments;
        this.creatorId = creatorId;
        this.projectId = projectId;
        this.confirmStatus = confirmStatus;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getConfirmStatus(){ return confirmStatus; }

    public void setConfirmStatus(int confirmStatus){ this.confirmStatus = confirmStatus; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public Map<String,String> getFilePaths() {
        return filePaths;
    }

    public void setFilePaths(Map<String,String> filePaths) {
        this.filePaths = filePaths;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }
}
