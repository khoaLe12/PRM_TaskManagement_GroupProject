package com.example.taskmanagement;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.taskmanagement.admin.AdminDashboardActivity;
import com.example.taskmanagement.user.UserDashboardActivity;

import constants.Constants;
import database.TaskStoreDatabase;
import executors.AppExecutors;
import models.User;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    TextView tvRegister, tvError;
    Button btnLogin;
    TaskStoreDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
        initDb();

        //Create admin and user account
//        createAccount();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                if(validate(username, password)){

                    // Run query using diskIO executor

                    AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            User user = db.userDAO().getUserByUsernameAndPassword(username, password);

                            // Run something that progress the view should use runOnUiThread

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(user == null){
                                        tvError.setText("Incorrect username or password");
                                    }
                                    else if(user.getBlocked()){
                                        tvError.setText("Account is blocked");
                                    }
                                    else{
                                        if(user.getRole().equals(Constants.USER)){
                                            // Store user data (id) like session
                                            SharedPreferences shared = getSharedPreferences(Constants.LOGIN, MODE_PRIVATE);
                                            SharedPreferences.Editor editor = shared.edit();
                                            editor.putInt(Constants.USER_ID, user.getUserId());
                                            editor.apply(); //Save data into a file

                                            Intent intent = new Intent(LoginActivity.this, UserDashboardActivity.class);
                                            startActivity(intent);
                                        }
                                        else if(user.getRole().equals(Constants.ADMIN)){

                                            SharedPreferences shared = getSharedPreferences(Constants.LOGIN, MODE_PRIVATE);
                                            SharedPreferences.Editor editor = shared.edit();
                                            editor.putInt(Constants.USER_ID, -1);
                                            editor.apply(); //Save data into a file

                                            Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                                            startActivity(intent);
                                        }
                                        else{
                                            tvError.setText("You don't have enough privileges to access this app");
                                        }
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private boolean validate(String username, String password){
        boolean result = true;

        if(username == null || username.length() == 0){
            etUsername.setError(Constants.REQUIRE_MESSAGE);
            result = false;
        }

        if(password == null || password.length() == 0){
            etPassword.setError(Constants.REQUIRE_MESSAGE);
            result = false;
        }

        if(password.length() < 5){
            etPassword.setError(Constants.PASSWORD_LENGTH_REQUIRE_MESSAGE);
            result = false;
        }

        return result;
    }

    private void initDb(){
        db = Room.databaseBuilder(getApplicationContext(), TaskStoreDatabase.class, Constants.DB_NAME).build();
    }

    private void initView(){
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvRegister = findViewById(R.id.tvRegister);
        tvError = findViewById(R.id.tvError);
        btnLogin = findViewById(R.id.btnLogin);
    }

    private void createAccount(){
        AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                User admin = new User("admin", "admin", Constants.ADMIN, null, Constants.DEFAULT_AVATAR, false, "I am admin", "admin@gmail.com", "District 12");
                User user = new User("user", "user1", Constants.USER, null, Constants.DEFAULT_AVATAR, false, "I am user", "user@gmail.com", "District 1");
                db.userDAO().insert(admin);
                db.userDAO().insert(user);
            }
        });
    }
}