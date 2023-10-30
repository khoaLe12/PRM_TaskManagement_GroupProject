package database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import dao.BillDAO;
import dao.ProjectDAO;
import dao.TaskDAO;
import dao.UserDAO;
import models.Bill;
import models.Project;
import models.Task;
import models.User;
import models.UserProjectCrossRef;
import utils.Converters;

@Database(entities = {User.class, Project.class, Task.class, UserProjectCrossRef.class, Bill.class}, version = 1)
@TypeConverters({Converters.class, })
public abstract class TaskStoreDatabase extends RoomDatabase {
    public abstract BillDAO billDAO();
    public abstract ProjectDAO projectDAO();
    public abstract TaskDAO taskDAO();
    public abstract UserDAO userDAO();
}
