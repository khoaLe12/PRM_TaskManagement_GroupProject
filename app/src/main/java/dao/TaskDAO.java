package dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import models.Task;

@Dao
public interface TaskDAO {

    @Query("SELECT * FROM task")
    List<Task> getAllTasks();

    @Query("SELECT * FROM task WHERE creatorId IN (:userId) AND projectId IN (:projectId)")
    List<Task> getTasksByUserIdAndProjectId(int userId, int projectId);

    @Query("SELECT * FROM task WHERE taskId IN (:taskId) LIMIT 1")
    Task getTaskById(int taskId);

    @Query("SELECT * FROM task WHERE projectId IN (:projectId)")
    List<Task> getTasksByProjectId(int projectId);

    @Insert
    void insert(Task task);

    @Update
    void update(Task task);

    @Delete
    void delete(Task task);
}
