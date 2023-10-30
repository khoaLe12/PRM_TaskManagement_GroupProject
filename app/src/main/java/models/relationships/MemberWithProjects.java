package models.relationships;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

import models.Project;
import models.User;
import models.UserProjectCrossRef;

public class MemberWithProjects {
    @Embedded public User member;
    @Relation(
            parentColumn = "userId",
            entityColumn = "projectId",
            associateBy = @Junction(UserProjectCrossRef.class)
    )
    public List<Project> projects;
}
