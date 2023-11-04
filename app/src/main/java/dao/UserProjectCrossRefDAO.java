package dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;

import models.UserProjectCrossRef;

@Dao
public interface UserProjectCrossRefDAO {

    @Insert
    void insert(UserProjectCrossRef entity);

    @Delete
    void delete(UserProjectCrossRef entity);
}
