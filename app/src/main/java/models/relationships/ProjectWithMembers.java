package models.relationships;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

import models.Project;
import models.User;
import models.UserProjectCrossRef;

public class ProjectWithMembers {
    @Embedded public Project project;
    @Relation(
            parentColumn = "projectId",
            entityColumn = "userId",
            associateBy = @Junction(UserProjectCrossRef.class)
    )
    public List<User> members;
}
