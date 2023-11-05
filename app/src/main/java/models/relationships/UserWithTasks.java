package models.relationships;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import models.Task;
import models.User;

public class UserWithTasks {
    @Embedded private User user;
    @Relation(
            parentColumn = "userId",
            entityColumn = "creatorId"
    )
    private List<Task> tasks;

    public User getUser() {
        return user;
    }

    public List<Task> getTasks() {
        return tasks;
    }
}
