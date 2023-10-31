package com.example.taskmanagement;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

import configurations.CloudinaryConfig;
import constants.Constants;
import database.TaskStoreDatabase;
import executors.AppExecutors;
import models.User;
import utils.CircleTransform;

public class RegisterActivity extends AppCompatActivity {

    Uri imageUri;
    ImageView imgAvatar;
    EditText etUsername, etPassword, etName, etEmail;
    Button btnRegister;
    TaskStoreDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
        initDb();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iGallery = new Intent();
                iGallery.setAction(Intent.ACTION_PICK);
                iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(iGallery, Constants.GALLERY_REQUEST_CODE);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String name = etName.getText().toString();
                String email = etEmail.getText().toString();

                if(validate(username, password, name)){
                    AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            boolean checkUsername = db.userDAO().getUserByUsername(username) != null;
                            boolean checkEmail = db.userDAO().getUserByEmail(email) != null;

                            if(checkUsername || checkEmail){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(checkUsername){
                                            etUsername.setError(Constants.USERNAME_ALREADY_TAKEN);
                                        }
                                        if(checkEmail){
                                            etEmail.setError(Constants.EMAIL_ALREADY_TAKEN);
                                        }
                                    }
                                });
                            }
                            else{
                                String imageAvatarUrl = Constants.DEFAULT_AVATAR;
                                if(imageUri != null && imageUri.toString().length() > 0){
                                    Cloudinary cloudinary = CloudinaryConfig.getCloudinary();
                                    String path = getRealPathFromURI(imageUri);
                                    Map uploadResult = null;
                                    try {
                                        uploadResult = cloudinary.uploader().upload(new File(path), ObjectUtils.emptyMap());
                                        imageAvatarUrl = (String) uploadResult.get("url");
                                        Log.i("Url", (String) uploadResult.get("url"));
                                    } catch (IOException e) {
                                        Log.e("Error cloudinary", "Ko upload đc file");
                                        throw new RuntimeException(e);
                                    }
                                }

                                User newUser = new User(username, password, Constants.USER, generateRandomString(5), imageAvatarUrl, false, name, email);
                                db.userDAO().insert(newUser);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(RegisterActivity.this, "Register new account successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == Constants.GALLERY_REQUEST_CODE){
                // for gallery
                imageUri = data.getData();
                Picasso.get()
                        .load(data.getData())
                        .transform(new CircleTransform())
                        .into(imgAvatar);
            }
        }
    }

    private void initView(){
        imgAvatar = findViewById(R.id.imgAvatar_register);
        etUsername = findViewById(R.id.etUsername_register);
        etPassword = findViewById(R.id.etPassword_register);
        etName = findViewById(R.id.etEmail_register);
        etEmail = findViewById(R.id.etEmail_register);
        btnRegister = findViewById(R.id.btnRegister);

        Picasso.get()
                .load(Constants.DEFAULT_AVATAR)
                .transform(new CircleTransform())
                .into(imgAvatar);
    }

    private void initDb(){
        db = Room.databaseBuilder(getApplicationContext(), TaskStoreDatabase.class, Constants.DB_NAME).build();
    }

    private boolean validate(String username, String password, String name){
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

        if(name == null || name.length() == 0){
            etName.setError(Constants.PASSWORD_LENGTH_REQUIRE_MESSAGE);
            result = false;
        }

        return result;
    }

    private String generateRandomString(int length){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int charactersLength = characters.length();
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);

        for(int i = 0; i < length; i++){
            stringBuilder.append(characters.charAt(random.nextInt(charactersLength)));
        }

        return stringBuilder.toString();
    }

    private String getRealPathFromURI(Uri contentUri){
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}