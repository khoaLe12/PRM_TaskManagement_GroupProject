package com.example.taskmanagement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import adapter.MemberAdapter;
import constants.Constants;
import database.TaskStoreDatabase;
import executors.AppExecutors;
import models.Task;
import models.relationships.ProjectWithMembers;

public class TaskCreateActivity extends AppCompatActivity implements MemberAdapter.OnSelectedMemberListener{

    EditText etTitle, etDescription, etDeadline;
    ImageView imgBack;
    RecyclerView rcvMembers;
    Button btnCreate;
    TaskStoreDatabase db;
    int memberId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_create);

        Intent intent = getIntent();
        int projectId = intent.getIntExtra(Constants.PROJECT_ID, 0);
        if(projectId != 0){
            memberId = -1;
            initView();
            initDb();
            loadData(projectId);

            imgBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TaskCreateActivity.this, ProjectDetailActivity.class);
                    intent.putExtra(Constants.PROJECT_ID, projectId);
                    startActivity(intent);
                    finish();
                }
            });
            
            btnCreate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(memberId == -1){
                        Toast.makeText(TaskCreateActivity.this, "Please choose assigned member", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        String title = etTitle.getText().toString();
                        String description = etDescription.getText().toString();
                        String deadline = etDeadline.getText().toString();

                        Date deadlineDate;

                        try{
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
                            deadlineDate = sdf.parse(deadline);
                        }
                        catch (Exception ex){
                            Log.e("Error when convert string to date", ex.toString());
                            Toast.makeText(TaskCreateActivity.this, "DateTime is not in correct format", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(validate(title, description, deadlineDate)){
                            AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    Task newTask = new Task(title, description, deadlineDate, 0, null, null, null, memberId, projectId, 0);
                                    db.taskDAO().insert(newTask);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(TaskCreateActivity.this, "Create new task successfully", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(TaskCreateActivity.this, ProjectDetailActivity.class);
                                            intent.putExtra(Constants.PROJECT_ID, projectId);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
            });
        }
        else{
            Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validate(String title, String description, Date deadline){
        boolean result = true;
        if(title == null || title.length() == 0){
            etTitle.setError(Constants.REQUIRE_MESSAGE);
            result = false;
        }

        if(description == null || description.length() == 0){
            etDescription.setError(Constants.REQUIRE_MESSAGE);
            result = false;
        }

        if(deadline.before(new Date())){
            etDeadline.setError("Deadline is not valid (deadline must be larger than current datetime)");
            result = false;
        }

        return result;
    }

    private void initView(){
        etTitle = findViewById(R.id.etTitle_task_create);
        etDescription = findViewById(R.id.etDescription_task_create);
        etDeadline = findViewById(R.id.etDeadline_task_create);
        imgBack = findViewById(R.id.imgArrowBack_task_create);
        rcvMembers = findViewById(R.id.rcvMembers_task_create);
        btnCreate = findViewById(R.id.btnCreate_task_create);
    }

    private void initDb(){
        db = Room.databaseBuilder(getApplicationContext(), TaskStoreDatabase.class, Constants.DB_NAME).build();
    }

    private void loadData(int projectId){
        AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                ProjectWithMembers result = db.projectDAO().getProjectWithMembersById(projectId);
                MemberAdapter adapter = new MemberAdapter(result.members, false);
                adapter.setCreateTask(true);
                rcvMembers.setAdapter(adapter);
                rcvMembers.setLayoutManager(new LinearLayoutManager(TaskCreateActivity.this));
            }
        });
    }

    @Override
    public void onSelectedMember(int userId) {
        if(userId > 0){
            memberId = userId;
        }
        else{
            Toast.makeText(this, "Can not get member information", Toast.LENGTH_SHORT).show();
        }
    }
}