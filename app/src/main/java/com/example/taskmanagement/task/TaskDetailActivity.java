package com.example.taskmanagement.task;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskmanagement.ProjectDetailActivity;
import com.example.taskmanagement.R;
import com.example.taskmanagement.TaskActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import adapter.AttachmentAdapter;
import constants.Constants;
import database.TaskStoreDatabase;
import executors.AppExecutors;
import models.Task;
import models.User;
import utils.CircleTransform;

public class TaskDetailActivity extends AppCompatActivity {

    EditText etEvaluation;
    ImageView imgBack, imgAvatar;
    TextView tvTitle, tvDeadline, tvDescription, tvConfirmStatus, tvStatus, tvName, tvEmail, tvReportContent;
    Button btnReport, btnConfirm, btnReject;
    RecyclerView rcvFiles;
    LinearLayout llReport;
    TaskStoreDatabase db;
    Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        Intent intent = getIntent();
        int taskId = intent.getIntExtra(Constants.TASK_ID, 0);
        int projectId = intent.getIntExtra(Constants.PROJECT_ID, 0);
        boolean isManager = intent.getBooleanExtra(Constants.IS_MANAGER, false);
        if(taskId > 0){
            initView();
            initDb();
            loadData(taskId, isManager);

            imgBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(projectId != 0){
                        // Return to project detail
                        Intent intent = new Intent(TaskDetailActivity.this, ProjectDetailActivity.class);
                        intent.putExtra(Constants.PROJECT_ID, projectId);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        Intent intent = new Intent(TaskDetailActivity.this, TaskActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });
            btnReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(TaskDetailActivity.this, TaskReportActivity.class);
                    intent.putExtra(Constants.TASK_ID, taskId);
                    startActivity(intent);
                    finish();
                }
            });
            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            task.setConfirmStatus(1);
                            if(etEvaluation.getText() != null && etEvaluation.getText().toString().length() > 0){
                                task.setComments(etEvaluation.getText().toString());
                            }
                            db.taskDAO().update(task);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(TaskDetailActivity.this, "Update successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(TaskDetailActivity.this, ProjectDetailActivity.class);
                                    intent.putExtra(Constants.PROJECT_ID, projectId);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    });
                }
            });
            btnReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            task.setConfirmStatus(2);
                            if(etEvaluation.getText() != null && etEvaluation.getText().toString().length() > 0){
                                task.setComments(etEvaluation.getText().toString());
                            }
                            db.taskDAO().update(task);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(TaskDetailActivity.this, "Update successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(TaskDetailActivity.this, ProjectDetailActivity.class);
                                    intent.putExtra(Constants.PROJECT_ID, projectId);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    });
                }
            });
        }
        else{
            Toast.makeText(this, "Load information fail", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView(){
        imgBack = findViewById(R.id.imgArrowBack_task_detail);
        imgAvatar = findViewById(R.id.ivAvatar_task_detail);
        tvTitle = findViewById(R.id.tvTitle_task_detail);
        tvDeadline = findViewById(R.id.tvDeadline_task_item);
        tvDescription = findViewById(R.id.tvDescription_task_item);
        tvConfirmStatus = findViewById(R.id.tvConfirmStatus_task_detail);
        tvStatus = findViewById(R.id.tvTaskStatus_task_detail);
        tvName = findViewById(R.id.tvName_task_detail);
        tvEmail = findViewById(R.id.tvEmail_task_detail);
        etEvaluation = findViewById(R.id.etEvaluation_task_detail);
        tvReportContent = findViewById(R.id.tvReportContent_task_detail);
        llReport = findViewById(R.id.llReport_task_detail);
        btnReport = findViewById(R.id.btnReport_task_detail);
        btnConfirm = findViewById(R.id.btnConfirm_task_detail);
        btnReject = findViewById(R.id.btnReject_task_detail);
        rcvFiles = findViewById(R.id.rcvFiles_task_detail);
    }

    private void initDb(){
        db = Room.databaseBuilder(getApplicationContext(), TaskStoreDatabase.class, Constants.DB_NAME).build();
    }

    private void loadData(int taskId, boolean isManager){
        AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                task = db.taskDAO().getTaskById(taskId);
                if(task != null){
                    User user = db.userDAO().getUserById(task.getCreatorId());
                    if(user != null){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvTitle.setText(task.getTitle());
                                tvDeadline.setText(task.getDeadline().toString());
                                tvDescription.setText(task.getDescription());
                                tvName.setText(user.getName());
                                tvEmail.setText(user.getEmail());
                                Picasso.get()
                                        .load(user.getImageUrl())
                                        .transform(new CircleTransform())
                                        .into(imgAvatar);
                                setStatus(task.getConfirmStatus(), task.getStatus());
                                setReport(task);

                                if(task.getComments() != null && task.getComments().length() > 0){
                                    etEvaluation.setText(task.getComments());
                                }

                                setButton(isManager);
                            }
                        });
                    }
                    else{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(TaskDetailActivity.this, "Load information fail", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(TaskDetailActivity.this, "Load information fail", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void setStatus(int confirmStatus, int status){
        if(confirmStatus == 0){
            tvConfirmStatus.setText(Constants.WAITING);
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.baseline_hourglass_empty_24);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            tvConfirmStatus.setCompoundDrawables(drawable, null, null, null);
            tvConfirmStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.custom_blue_status));
        }
        else if(confirmStatus == 1){
            tvConfirmStatus.setText(Constants.CONFIRM);
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.baseline_check_24);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            tvConfirmStatus.setCompoundDrawables(drawable, null, null, null);
            tvConfirmStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.custom_green_status));
        }
        else if(confirmStatus == 2){
            tvConfirmStatus.setText(Constants.REJECT);
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.baseline_not_interested_24);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            tvConfirmStatus.setCompoundDrawables(drawable, null, null, null);
            tvConfirmStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.custom_red_status));
        }
        else{
            tvConfirmStatus.setText("Error");
        }

        if(status == 0){
            tvStatus.setText(Constants.IN_PROGRESS);
            tvStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.custom_blue_status));
        }
        else if(status == 1){
            tvStatus.setText(Constants.DONE);
            tvStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.custom_green_status));
        }
        else if(status == 2){
            tvStatus.setText(Constants.OVERDUE);
            tvStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.custom_red_status));
        }
        else{
            tvStatus.setText("Error");
        }
    }

    private void setReport(Task task){
        if(task.getReport() != null && task.getReport().length() > 0){
            tvReportContent.setText(task.getReport());
        }

        if(task.getFilePaths() != null && task.getFilePaths().size() > 0){
            float dpValue = 220;
            float d = TaskDetailActivity.this.getResources().getDisplayMetrics().density;
            int height = (int)(dpValue * d); // convert 220 dp to equivalent pixels
            rcvFiles.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    height
            ));

            Set<String> keys = task.getFilePaths().keySet();
            List<String> names = new ArrayList<>(keys);
            AttachmentAdapter adapter = new AttachmentAdapter(names, task.getFilePaths());
            rcvFiles.setAdapter(adapter);
            rcvFiles.setLayoutManager(new LinearLayoutManager(TaskDetailActivity.this));
        }
    }

    private void setButton(boolean isManager){
        SharedPreferences shared = getSharedPreferences(Constants.LOGIN, MODE_PRIVATE);
        int userId = shared.getInt(Constants.USER_ID, 0);
        if(userId != 0){
            if(task.getCreatorId() == userId){
                btnReport.setVisibility(View.VISIBLE);
            }
            else if(isManager){
                btnConfirm.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
                etEvaluation.setEnabled(true);
            }
        }
    }
}