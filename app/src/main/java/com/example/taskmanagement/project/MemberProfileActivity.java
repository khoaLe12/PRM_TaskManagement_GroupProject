package com.example.taskmanagement.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskmanagement.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapter.TaskAdapter;
import constants.Constants;
import database.TaskStoreDatabase;
import executors.AppExecutors;
import models.Bill;
import models.Task;
import models.User;
import utils.CircleTransform;
import vn.momo.momo_partner.AppMoMoLib;

public class MemberProfileActivity extends AppCompatActivity {

    ImageView imgAvatar, imgBack;
    TextView tvName, tvEmail;
    EditText etLocation, etCode;
    RecyclerView rcvTasks;
    Button btnPayment;
    TaskStoreDatabase db;
    int recipientId, projectId;
    private String amount = "10000";
    private String fee = "0";
    private String merchantName = "Trả Lương";//lương
    private String merchantCode = "MOMOC2IC20220510";//lấy từ tài khoản momomerchant
    private String description = "Trả lương cho Nhân viên: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_profile);

        Intent intent = getIntent();
        int projectId = intent.getIntExtra(Constants.PROJECT_ID, -1);
        int userId = intent.getIntExtra(Constants.USER_ID, 0);
        if (projectId != -1 && userId != 0) {
            recipientId = userId;
            this.projectId = projectId;
            initView();
            initDb();
            loadData(userId, projectId);

            imgBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            //Payment Momo
            btnPayment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Khởi tạo MoMo SDK
                    AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT);
                    requestPayment();
                }
            });
        } else {
            Toast.makeText(this, "Load information fail", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        imgAvatar = findViewById(R.id.imgAvatar_member_profile);
        tvName = findViewById(R.id.tvName_member_profile);
        tvEmail = findViewById(R.id.tvEmail_member_profile);
        etLocation = findViewById(R.id.etLocation_member_profile);
        etCode = findViewById(R.id.etCode_member_profile);
        rcvTasks = findViewById(R.id.rcvTasks_member_profile);
        btnPayment = findViewById(R.id.btnPayment_member_profile);
        imgBack = findViewById(R.id.imgArrowBack_member_profile);
    }

    private void initDb() {
        db = Room.databaseBuilder(getApplicationContext(), TaskStoreDatabase.class, Constants.DB_NAME).build();
    }

    private void loadData(int userId, int projectId) {
        AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                User user = db.userDAO().getUserById(userId);
                if (user != null) {
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

    private void requestPayment() {
        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT);
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN);

        Map<String, Object> eventValue = new HashMap<>();
        eventValue.put("merchantname", "Trả Lương"); //Dịch vụ
        eventValue.put("merchantcode", merchantCode);
        eventValue.put("amount", amount);
        eventValue.put("orderId", "orderId123456789");
        eventValue.put("orderLabel", "Mã đơn hàng");
        eventValue.put("merchantnamelabel", "Dịch vụ");
        eventValue.put("fee", fee);
        eventValue.put("description", description + tvName.getText().toString());

        JSONObject objExtraData = new JSONObject();
        try {
            objExtraData.put("site_code", "008");
            objExtraData.put("site_name", "CGV Cresent Mall");
            objExtraData.put("screen_code", 0);
            objExtraData.put("screen_name", "Special");
            objExtraData.put("movie_name", "Kẻ Trộm Mặt Trăng 3");
            objExtraData.put("movie_format", "2D");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        eventValue.put("extraData", objExtraData.toString());

        eventValue.put("extra", "");
        AppMoMoLib.getInstance().requestMoMoCallBack(this, eventValue);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppMoMoLib.getInstance().REQUEST_CODE_MOMO && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getIntExtra("status", -1) == 0) {
                    AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            SharedPreferences shared = getSharedPreferences(Constants.LOGIN, MODE_PRIVATE);
                            int senderId = shared.getInt(Constants.USER_ID, 0);

                            //Create Bill
                            Bill bill = new Bill("Payment", description + tvName.getText().toString(), 10000, new Date(), recipientId, senderId, projectId);
                            db.billDAO().insert(bill);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MemberProfileActivity.this, "Payment success", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(this, "Payment failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Payment failed", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show();
        }
    }
}