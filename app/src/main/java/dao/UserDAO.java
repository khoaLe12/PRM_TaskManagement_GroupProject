package dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import constants.Constants;
import models.User;
import models.relationships.ManagerWithProjects;
import models.relationships.MemberWithProjects;
import models.relationships.UserWithTasks;

@Dao
public interface UserDAO {

    @Query("SELECT * FROM user WHERE inviteCode LIKE :inviteCode LIMIT 1")
    User getUserByInviteCode(String inviteCode);

    @Query("SELECT * FROM user WHERE username LIKE :username AND password LIKE :password LIMIT 1")
    User getUserByUsernameAndPassword(String username, String password);

    @Query("SELECT * FROM user WHERE username LIKE :username LIMIT 1")
    User getUserByUsername(String username);

    @Query("SELECT * FROM user WHERE email LIKE :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM user WHERE role LIKE :role")
    List<User> getAll(String role);

    @Query("SELECT * FROM user WHERE userId IN (:userId) LIMIT 1")
    User getUserById(int userId);

    @Transaction
    @Query("SELECT * FROM user WHERE userId IN (:userId) LIMIT 1")
    ManagerWithProjects getManagerWithProjectsById(int userId);

    @Transaction
    @Query("SELECT * FROM user WHERE userId IN (:userId) LIMIT 1")
    MemberWithProjects getMemberWithProjectsById(int userId);

    @Transaction
    @Query("SELECT * FROM user WHERE userId IN (:userId) LIMIT 1")
    UserWithTasks getUserWithTasksById(int userId);

    @Insert
    void insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);
}
