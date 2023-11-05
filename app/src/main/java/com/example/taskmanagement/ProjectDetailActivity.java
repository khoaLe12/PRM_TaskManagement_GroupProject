package com.example.taskmanagement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskmanagement.project.ProjectUpdateActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import adapter.AttachmentAdapter;
import adapter.MemberAdapter;
import adapter.TaskAdapter;
import constants.Constants;
import database.TaskStoreDatabase;
import executors.AppExecutors;
import models.Project;
import models.Task;
import models.User;
import models.UserProjectCrossRef;
import models.relationships.ProjectWithMembers;

public class ProjectDetailActivity extends AppCompatActivity {

    TextView tvManager, tvStatus, tvTitle, tvDate,  tvContent, tvAttachmentEmpty;
    RecyclerView rcvAttachments, rcvMembers, rcvTasks;
    ImageView imgAddMember, imgCreateTask, imgBack, imgOptions;
    TaskStoreDatabase db;
    AttachmentAdapter attachmentAdapter;
    MemberAdapter memberAdapter;
    TaskAdapter taskAdapter;
    int ProjectId = 0;
    List<User> members;
    Project project;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        Intent intent = getIntent();
        int projectId = intent.getIntExtra(Constants.PROJECT_ID, -1);
        if(projectId != -1){
            ProjectId = projectId;
            initView();
            initDb();
            loadData(projectId);
        }
        else{
            Toast.makeText(this, "Load project fail", Toast.LENGTH_SHORT).show();
        }

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectDetailActivity.this, ProjectActivity.class);
                startActivity(intent);
                finish();
            }
        });

        imgAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Integer> userIds = members.stream()
                        .map(User::getUserId)
                        .collect(Collectors.toList());
                DialogEnterInviteCode(userIds);
            }
        });

        imgCreateTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectDetailActivity.this, TaskCreateActivity.class);
                /*Bundle bundle = new Bundle();
                bundle.putInt(Constants.PROJECT_ID, ProjectId);
                intent.putExtras(bundle);*/
                intent.putExtra(Constants.PROJECT_ID, ProjectId);
                startActivity(intent);
                finish();
            }
        });

        imgOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(ProjectDetailActivity.this, imgOptions);
                popupMenu.getMenuInflater().inflate(R.menu.menu_project_popup, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.menu_project_update){
                            Intent intent = new Intent(ProjectDetailActivity.this, ProjectUpdateActivity.class);
                            intent.putExtra(Constants.PROJECT_ID, ProjectId);
                            startActivity(intent);
                            finish();
                        }
                        else if(item.getItemId() == R.id.menu_project_delete){
                            new AlertDialog.Builder(ProjectDetailActivity.this)
                                    .setTitle("Confirmation")
                                    .setMessage("Are you sure you want to delete this item?")
                                    .setIcon(R.drawable.baseline_warning_24)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    db.projectDAO().delete(project);
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(ProjectDetailActivity.this, "Delete successfully", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(ProjectDetailActivity.this, ProjectActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                            dialog.dismiss();
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    })
                                    .setNegativeButton("No", null).show();
                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });
    }

    private void initView(){
        tvManager = findViewById(R.id.tvManager_project_detail);
        tvStatus = findViewById(R.id.tvStatus_project_detail);
        tvTitle = findViewById(R.id.tvTitle_project_detail);
        tvDate = findViewById(R.id.tvDate_project_detail);
        tvContent = findViewById(R.id.tvContent_project_detail);
        tvAttachmentEmpty = findViewById(R.id.tvEmptyAttachments_project_detail);
        rcvAttachments = findViewById(R.id.rcvAttachments_project_detail);
        rcvMembers = findViewById(R.id.rcvMembers_project_detail);
        rcvTasks = findViewById(R.id.rcvTasks_project_detail);
        imgAddMember = findViewById(R.id.imgGroupAdd_project_detail);
        imgCreateTask = findViewById(R.id.imgMemberAdd_project_detail);
        imgBack = findViewById(R.id.imgArrowBack_project_detail);
        imgOptions = findViewById(R.id.imgOptions_project_detail);
    }

    private void initDb(){
        db = Room.databaseBuilder(getApplicationContext(), TaskStoreDatabase.class, Constants.DB_NAME).build();
    }

    private void loadData(int projectId){
        AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                ProjectWithMembers result = db.projectDAO().getProjectWithMembersById(projectId);
                project = result.project;
                members = result.members;
                List<Task> tasks = db.taskDAO().getTasksByProjectId(projectId);
                User manager = db.userDAO().getUserById(project.getManagerId());
                boolean isManager = false;

                SharedPreferences shared = getSharedPreferences(Constants.LOGIN, MODE_PRIVATE);
                int userId = shared.getInt(Constants.USER_ID, 0);
                if(userId == -1 || userId == project.getManagerId()){
                    isManager = true;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imgAddMember.setVisibility(View.VISIBLE);
                            imgCreateTask.setVisibility(View.VISIBLE);
                            imgOptions.setVisibility(View.VISIBLE);
                        }
                    });
                }

                if(project.getStatus() == 1){
                    tvStatus.setText(Constants.IN_PROGRESS);
                    tvStatus.setBackground(ContextCompat.getDrawable(ProjectDetailActivity.this, R.drawable.custom_green_status));
                }
                else if(project.getStatus() == 0){
                    tvStatus.setText(Constants.END);
                    tvStatus.setBackground(ContextCompat.getDrawable(ProjectDetailActivity.this, R.drawable.custom_red_status));
                }
                else{
                    tvStatus.setText("Error");
                }

                if(manager != null){
                    tvManager.setText(manager.getName());
                }

                if(project.getFilePaths() == null || project.getFilePaths().size() == 0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvAttachmentEmpty.setVisibility(View.VISIBLE);
                        }
                    });
                }
                else{
                    Set<String> keys = project.getFilePaths().keySet();
                    List<String> names = new ArrayList<>(keys);
                    attachmentAdapter = new AttachmentAdapter(names, project.getFilePaths());
                    rcvAttachments.setAdapter(attachmentAdapter);
                    rcvAttachments.setLayoutManager(new LinearLayoutManager(ProjectDetailActivity.this));
                }
                tvTitle.setText(project.getTitle());
                tvDate.setText(project.getCreatedDate().toString());
                tvContent.setText(project.getContent());

                memberAdapter = new MemberAdapter(members, isManager);
                memberAdapter.setProjectId(ProjectId);
                rcvMembers.setAdapter(memberAdapter);
                rcvMembers.setLayoutManager(new LinearLayoutManager(ProjectDetailActivity.this));

                taskAdapter = new TaskAdapter(tasks, isManager);
                rcvTasks.setAdapter(taskAdapter);
                rcvTasks.setLayoutManager(new LinearLayoutManager(ProjectDetailActivity.this));
            }
        });
    }

    private void DialogEnterInviteCode(List<Integer> userIds){
        Dialog dialog = new Dialog(ProjectDetailActivity.this);
        dialog.setContentView(R.layout.dialog_add_member);

        EditText etInviteCode = dialog.findViewById(R.id.etInviteCode_dialog);
        Button btnAdd = dialog.findViewById(R.id.btnAdd_dialog);
        Button btnBack = dialog.findViewById(R.id.btnBack_dialog);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inviteCode = etInviteCode.getText().toString();
                if(inviteCode != null && inviteCode.length() > 0){
                    AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            User user = db.userDAO().getUserByInviteCode(inviteCode);
                            if(user != null){
                                if(!userIds.contains(user.getUserId())){
                                    UserProjectCrossRef entity = new UserProjectCrossRef(user.getUserId(), ProjectId);
                                    db.userProjectDAO().insert(entity);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ProjectDetailActivity.this, "Add user successfully", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                            loadMembers();
                                        }
                                    });
                                }
                                else{
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(ProjectDetailActivity.this, "User already in project", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                            else{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(ProjectDetailActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(ProjectDetailActivity.this, "Invalid invite code", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void loadMembers(){
        AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                members = db.projectDAO().getProjectWithMembersById(ProjectId).members;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        memberAdapter.setMembers(members);
                    }
                });
            }
        });
    }
}