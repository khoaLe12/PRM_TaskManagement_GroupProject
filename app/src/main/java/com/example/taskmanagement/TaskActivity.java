package com.example.taskmanagement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.taskmanagement.user.UserDashboardActivity;

import java.util.ArrayList;
import java.util.List;

import adapter.TaskAdapter;
import constants.Constants;
import database.TaskStoreDatabase;
import executors.AppExecutors;
import models.Project;
import models.Task;
import models.relationships.UserWithTasks;

public class TaskActivity extends AppCompatActivity {

    EditText etSearch;
    ImageView imgSearch;
    RecyclerView rcvTasks;
    LinearLayout dashboardTab, projectTab, billTab;
    TaskStoreDatabase db;
    List<Task> taskList;
    TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        SharedPreferences shared = getSharedPreferences(Constants.LOGIN, MODE_PRIVATE);
        int userId = shared.getInt(Constants.USER_ID, 0);
        if(userId != 0){
            taskList = new ArrayList<>();
            initView();
            initDb();
            loadData(userId);

            imgSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String search = etSearch.getText().toString();
                    if(search != null && taskList.size() > 0){
                        List<Task> searchTasks = new ArrayList<>();
                        for(Task task : taskList){
                            if(task.getTitle().contains(search)){
                                searchTasks.add(task);
                            }
                        }
                        adapter.setList(searchTasks);
                    }
                }
            });

            dashboardTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TaskActivity.this, UserDashboardActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

            projectTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TaskActivity.this, ProjectActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

            billTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TaskActivity.this, BillActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
        else{
            Toast.makeText(this, "Load information fail", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView(){
        etSearch = findViewById(R.id.etSearch_task);
        imgSearch = findViewById(R.id.ivSearch_task);
        rcvTasks = findViewById(R.id.rcvTask_task);
        dashboardTab = findViewById(R.id.dashboardTab_task);
        projectTab = findViewById(R.id.projectTab_task);
        billTab = findViewById(R.id.billTab_task);
    }

    private void initDb(){
        db = Room.databaseBuilder(getApplicationContext(), TaskStoreDatabase.class, Constants.DB_NAME).build();
    }

    private void loadData(int userId){
        AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Task> tasks = new ArrayList<>();
                if(userId == -1){
                    tasks = db.taskDAO().getAllTasks();
                }
                else{
                    UserWithTasks result = db.userDAO().getUserWithTasksById(userId);
                    tasks = result.tasks;
                }

                if(tasks.size() > 0){
                    taskList = tasks;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new TaskAdapter(taskList, false);
                            rcvTasks.setAdapter(adapter);
                            rcvTasks.setLayoutManager(new LinearLayoutManager(TaskActivity.this));
                        }
                    });
                }
            }
        });
    }
}