package dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import models.Project;
import models.relationships.ProjectWithMembers;
import models.relationships.ProjectWithTasks;

@Dao
public interface ProjectDAO {

    @Query("SELECT * FROM project WHERE projectId IN (:projectId) LIMIT 1")
    Project getProjectById(int projectId);

    @Query("SELECT * FROM project")
    List<Project> getProjects();

    @Transaction
    @Query("SELECT * FROM project WHERE projectId IN (:projectId) LIMIT 1")
    ProjectWithMembers getProjectWithMembersById(int projectId);

    @Transaction
    @Query("SELECT * FROM project WHERE projectId IN (:projectId) LIMIT 1")
    ProjectWithTasks getProjectWithTasksById(int projectId);

    @Insert
    void insert(Project project);

    @Update
    void update(Project project);

    @Delete
    void delete(Project project);
}
