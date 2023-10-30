package models.relationships;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import models.Project;
import models.User;

public class ManagerWithProjects {
    @Embedded public User manager;
    @Relation(
            parentColumn = "userId",
            entityColumn = "managerId"
    )
    public List<Project> projects;
}
