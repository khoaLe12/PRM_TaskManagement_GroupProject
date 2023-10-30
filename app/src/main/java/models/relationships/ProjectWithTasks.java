package models.relationships;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

import models.Project;
import models.Task;

public class ProjectWithTasks {
    @Embedded public Project project;
    @Relation(
            parentColumn = "projectId",
            entityColumn = "projectId"
    )
    public List<Task> tasks;
}
