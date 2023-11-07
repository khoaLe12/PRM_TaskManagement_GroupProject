package com.example.taskmanagement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import adapter.BillAdapter;
import constants.Constants;
import database.TaskStoreDatabase;
import executors.AppExecutors;
import models.Bill;

public class BillActivity extends AppCompatActivity {

    EditText etSearch;
    ImageView imgSearch;
    RecyclerView rcvBills;
    LinearLayout dashboardTab, projectTab, taskTab;
    TaskStoreDatabase db;
    List<Bill> bills;
    BillAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        initView();
        initDb();
        loadData();
    }

    private void initDb() {
        db = Room.databaseBuilder(getApplicationContext(), TaskStoreDatabase.class, Constants.DB_NAME).build();
    }

    private void initView() {
        etSearch = findViewById(R.id.etSearch_bill);
        imgSearch = findViewById(R.id.ivSearch_bill);
        rcvBills = findViewById(R.id.rcvProject_bill);
        dashboardTab = findViewById(R.id.dashboardTab_bill);
        projectTab = findViewById(R.id.projectTab_bill);
        taskTab = findViewById(R.id.taskTab_bill);
//        billTab = findViewById(R.id.billTab_bill);
    }

    private void loadData() {
        SharedPreferences shared = getSharedPreferences(Constants.LOGIN, MODE_PRIVATE);
        int userId = shared.getInt(Constants.USER_ID, 0);

        if (userId != 0) {
            bills = new ArrayList<>();
            AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                @Override
                public void run() {
                    // If user is admin
                    if (userId == -1) {
                        bills = db.billDAO().getAll();
                    }
                    else {
                        bills.addAll(db.billDAO().getBillsByRecipientId(userId));
                        bills.addAll(db.billDAO().getBillsBySenderId(userId));
                    }

                    if (bills.size() > 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter = new BillAdapter(bills);
                                rcvBills.setAdapter(adapter);
                                rcvBills.setLayoutManager(new LinearLayoutManager(BillActivity.this));
                            }
                        });
                    }
                }
            });
        } else {
            Toast.makeText(this, "Load bill failed", Toast.LENGTH_SHORT).show();
        }
    }
}