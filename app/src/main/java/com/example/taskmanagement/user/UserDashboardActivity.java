package com.example.taskmanagement.user;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskmanagement.BillActivity;
import com.example.taskmanagement.ProjectActivity;
import com.example.taskmanagement.R;
import com.example.taskmanagement.TaskActivity;
import com.squareup.picasso.Picasso;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import constants.Constants;
import database.TaskStoreDatabase;
import executors.AppExecutors;
import models.User;
import models.relationships.UserWithTasks;
import utils.CircleTransform;

public class UserDashboardActivity extends AppCompatActivity {

    LinearLayout projectTab, taskTab, billTab;
    User loginUser;
    ImageView imgAvatar, imgLogout;
    TextView txtName, txtEmail, txtToday;
    TaskStoreDatabase db;
    PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        initView();
        initDb();

        imgLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences shared = getSharedPreferences(Constants.LOGIN, MODE_PRIVATE);
                SharedPreferences.Editor editor = shared.edit();
                editor.remove(Constants.USER_ID);
                editor.apply();
                finish();
            }
        });

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfile();
            }
        });

        txtEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfile();
            }
        });

        txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfile();
            }
        });

        projectTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboardActivity.this, ProjectActivity.class);
                startActivity(intent);
                finish();
            }
        });

        taskTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboardActivity.this, TaskActivity.class);
                startActivity(intent);
                finish();
            }
        });

        billTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDashboardActivity.this, BillActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setChartData(float ongoingPercent, float donePercent, float overduePercent) {
        pieChart.addPieSlice(new PieModel("Ongoing", ongoingPercent, Color.parseColor("#FFEB3B")));
        pieChart.addPieSlice(new PieModel("Done", donePercent, Color.parseColor("#8BC34A")));
        pieChart.addPieSlice(new PieModel("Overdue", overduePercent, Color.parseColor("#F44336")));
    }

    @Override
    protected void onResume(){
        super.onResume();
        loadUserInformation();
    }

    private void initView(){
        imgAvatar = findViewById(R.id.imgAvatar_dashboard);
        imgLogout = findViewById(R.id.imgLogout_dashboard);
        txtName = findViewById(R.id.tvName_dashboard);
        txtEmail = findViewById(R.id.tvEmail_dashboard);
        txtToday = findViewById(R.id.tvToday_dashboard);
        projectTab = findViewById(R.id.projectTab_user_dashboard);
        taskTab = findViewById(R.id.taskTab_user_dashboard);
        billTab = findViewById(R.id.billTab_user_dashboard);
        pieChart = findViewById(R.id.piechart);
    }

    private void initDb(){
        db = Room.databaseBuilder(getApplicationContext(), TaskStoreDatabase.class, Constants.DB_NAME).build();
    }

    private void loadUserInformation(){

        SharedPreferences shared = getSharedPreferences(Constants.LOGIN, MODE_PRIVATE);
        int userId = shared.getInt(Constants.USER_ID, 0);

        AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                loginUser = db.userDAO().getUserById(userId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(loginUser != null){
                            UserWithTasks userTask = db.userDAO().getUserWithTasksById(loginUser.getUserId());
                            if(userTask != null){
                                int ongoing = 0;
                                int done = 0;
                                int overdue = 0;
                                for(int i = 0; i < userTask.getTasks().size(); i++) {
                                    if(userTask.getTasks().get(i).getStatus() == 0){
                                        ongoing++;
                                    }
                                    else if(userTask.getTasks().get(i).getStatus() == 1){
                                        done++;
                                    }
                                    else{
                                        overdue++;
                                    }
                                }
                                float total = ongoing + done + overdue;
                                float ongoingPercent = ongoing/total;
                                float donePercent = done/total;
                                float overduePercent = overdue/total;
                                setChartData(ongoingPercent, donePercent, overduePercent);
                            }
                            Picasso.get()
                                    .load(loginUser.getImageUrl())
                                    .transform(new CircleTransform())
                                    .into(imgAvatar);
                            txtName.setText(loginUser.getName());
                            txtEmail.setText(loginUser.getEmail());
                        }
                        else{
                            Toast.makeText(UserDashboardActivity.this, "Load data failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void openProfile(){
        Intent intent = new Intent(UserDashboardActivity.this, UserProfileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", loginUser);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}