package models;

import androidx.room.Entity;

@Entity(primaryKeys = {"userId", "projectId"})
public class UserProjectCrossRef {
    public int userId;
    public int projectId;

    public UserProjectCrossRef(int userId, int projectId){
        this.userId = userId;
        this.projectId = projectId;
    }
}
