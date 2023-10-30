package com.example.taskmanagement.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.taskmanagement.R;

import java.util.List;

import adapter.UserAdapter;
import constants.Constants;
import database.TaskStoreDatabase;
import executors.AppExecutors;
import models.User;

public class AdminDashboardActivity extends AppCompatActivity {

    Button btnLogout;
    RecyclerView rcvUsers;

    TaskStoreDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initView();
        initDb();
        loadData();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView(){
        btnLogout = findViewById(R.id.btnLogout_admin_dashboard);
        rcvUsers = findViewById(R.id.rcvUsers);
    }

    private void initDb(){
        db = Room.databaseBuilder(getApplicationContext(), TaskStoreDatabase.class, Constants.DB_NAME).build();
    }

    private void loadData(){
        AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<User> users = db.userDAO().getAll(Constants.USER);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UserAdapter adapter = new UserAdapter(users);
                        rcvUsers.setAdapter(adapter);
                        rcvUsers.setLayoutManager(new LinearLayoutManager(AdminDashboardActivity.this));
                    }
                });
            }
        });

    }
}