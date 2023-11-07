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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import adapter.ProjectAdapter;
import constants.Constants;
import database.TaskStoreDatabase;
import executors.AppExecutors;
import models.Project;
import models.relationships.ManagerWithProjects;
import models.relationships.MemberWithProjects;

public class ProjectActivity extends AppCompatActivity {

    EditText etSearch;
    ImageView imgSearch;
    RecyclerView rcvProjects;
    LinearLayout dashboardTab, taskTab, billTab;
    FloatingActionButton fabCreate;
    TaskStoreDatabase db;
    List<Project> projects;
    ProjectAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        projects =  new ArrayList<>();

        initView();
        initDb();
        loadData();

        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String search = etSearch.getText().toString();
                if(search != null && projects.size() > 0){
                    List<Project> searchProjects = new ArrayList<>();
                    for(Project project : projects){
                        if(project.getTitle().contains(search)){
                            searchProjects.add(project);
                        }
                    }
                    adapter.setList(searchProjects);
                }
            }
        });

        fabCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectActivity.this, ProjectCreateActivity.class);
                startActivity(intent);
                finish();
            }
        });

        dashboardTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectActivity.this, UserDashboardActivity.class);
                startActivity(intent);
                finish();
            }
        });

        taskTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectActivity.this, TaskActivity.class);
                startActivity(intent);
                finish();
            }
        });

        billTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectActivity.this, BillActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initDb(){
        db = Room.databaseBuilder(getApplicationContext(), TaskStoreDatabase.class, Constants.DB_NAME).build();
    }
    private void initView(){
        etSearch = findViewById(R.id.etSearch_project);
        imgSearch = findViewById(R.id.ivSearch_project);
        rcvProjects = findViewById(R.id.rcvProject_project);
        dashboardTab = findViewById(R.id.dashboardTab_project);
        taskTab = findViewById(R.id.taskTab_project);
        billTab = findViewById(R.id.billTab_project);
        fabCreate = findViewById(R.id.fabCreateProject);
    }
    private void loadData(){
        SharedPreferences shared = getSharedPreferences(Constants.LOGIN, MODE_PRIVATE);
        int userId = shared.getInt(Constants.USER_ID, 0);

        if(userId != 0){
            AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                @Override
                public void run() {
                    List<Project> projectList = new ArrayList<>();
                    // If user is admin
                    if(userId == -1){
                        projectList = db.projectDAO().getProjects();
                    }
                    else{
                        ManagerWithProjects manger = db.userDAO().getManagerWithProjectsById(userId);
                        MemberWithProjects member = db.userDAO().getMemberWithProjectsById(userId);
                        projectList.addAll(manger.projects);
                        projectList.addAll(member.projects);
                    }

                    if(projectList.size() > 0){
                        projects = projectList;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter = new ProjectAdapter(projects);
                                rcvProjects.setAdapter(adapter);
                                rcvProjects.setLayoutManager(new LinearLayoutManager(ProjectActivity.this));
                            }
                        });
                    }
                }
            });
        }
        else{
            Toast.makeText(this, "Load data failed", Toast.LENGTH_SHORT).show();
        }
    }
}