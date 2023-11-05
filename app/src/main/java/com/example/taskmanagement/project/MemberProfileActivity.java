package com.example.taskmanagement.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskmanagement.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import adapter.TaskAdapter;
import constants.Constants;
import database.TaskStoreDatabase;
import executors.AppExecutors;
import models.Task;
import models.User;
import utils.CircleTransform;

public class MemberProfileActivity extends AppCompatActivity {

    ImageView imgAvatar, imgBack;
    TextView tvName, tvEmail;
    EditText etLocation, etCode;
    RecyclerView rcvTasks;
    Button btnPayment;
    TaskStoreDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_profile);

        Intent intent = getIntent();
        int projectId = intent.getIntExtra(Constants.PROJECT_ID, -1);
        int userId = intent.getIntExtra(Constants.USER_ID, 0);
        if(projectId != -1 && userId != 0){
            initView();
            initDb();
            loadData(userId, projectId);

            imgBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        else{
            Toast.makeText(this, "Load information fail", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView(){
        imgAvatar = findViewById(R.id.imgAvatar_member_profile);
        tvName = findViewById(R.id.tvName_member_profile);
        tvEmail = findViewById(R.id.tvEmail_member_profile);
        etLocation = findViewById(R.id.etLocation_member_profile);
        etCode = findViewById(R.id.etCode_member_profile);
        rcvTasks = findViewById(R.id.rcvTasks_member_profile);
        btnPayment = findViewById(R.id.btnPayment_member_profile);
        imgBack = findViewById(R.id.imgArrowBack_member_profile);
    }

    private void initDb(){
        db = Room.databaseBuilder(getApplicationContext(), TaskStoreDatabase.class, Constants.DB_NAME).build();
    }

    private void loadData(int userId, int projectId){
        AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                User user = db.userDAO().getUserById(userId);
                if(user != null){
                    List<Task> tasks = db.taskDAO().getTasksByUserIdAndProjectId(userId, projectId);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Picasso.get()
                                    .load(user.getImageUrl())
                                    .transform(new CircleTransform())
                                    .into(imgAvatar);
                            tvName.setText(user.getName());
                            tvEmail.setText(user.getEmail());
                            etLocation.setText(user.getLocation());
                            etCode.setText(user.getInviteCode());

                            TaskAdapter adapter = new TaskAdapter(tasks, false);
                            rcvTasks.setAdapter(adapter);
                            rcvTasks.setLayoutManager(new LinearLayoutManager(MemberProfileActivity.this));
                        }
                    });
                }
            }
        });
    }
}