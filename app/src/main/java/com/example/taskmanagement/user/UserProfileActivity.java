package com.example.taskmanagement.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.example.taskmanagement.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import configurations.CloudinaryConfig;
import constants.Constants;
import database.TaskStoreDatabase;
import executors.AppExecutors;
import models.User;
import utils.CircleTransform;

public class UserProfileActivity extends AppCompatActivity {

    User loginUser;
    ImageView imgBack, imgAvatar;
    EditText etUsername, etPassword, etName, etEmail, etCode;
    Button btnUpdate;
    Uri updateImageUri;
    TaskStoreDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        initView();
        initDb();
        loadData();

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.GALLERY_REQUEST_CODE);
                }
                else{
                    Intent iGallery = new Intent();
                    iGallery.setAction(Intent.ACTION_PICK);
                    iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(iGallery, Constants.GALLERY_REQUEST_CODE);
                }
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = etPassword.getText().toString().trim();
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();

                if(validate(password, name, email)){
                    AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            User checkUser = db.userDAO().getUserByEmail(email);
                            if(checkUser == null || checkUser.getUserId() == loginUser.getUserId()){

                                loginUser.setName(name);
                                loginUser.setEmail(email);
                                if(password != null){
                                    loginUser.setPassword(password);
                                }

                                if(updateImageUri != null && updateImageUri.toString().length() > 0){
                                    Cloudinary cloudinary = CloudinaryConfig.getCloudinary();
                                    String path = getRealPathFromURI(updateImageUri);
                                    try {
                                        Map uploadResult = cloudinary.uploader().upload(new File(path), ObjectUtils.emptyMap());
                                        loginUser.setImageUrl(uploadResult.get("secure_url").toString());
                                    } catch (IOException e) {
                                        Log.e("Error cloudinary", "Ko upload đc file");
                                        throw new RuntimeException(e);
                                    }
                                }

                                db.userDAO().update(loginUser);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(UserProfileActivity.this, "Update successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                            }
                            else{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        etEmail.setError(Constants.EMAIL_ALREADY_TAKEN);
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
                updateImageUri = data.getData();
                Picasso.get()
                        .load(data.getData())
                        .transform(new CircleTransform())
                        .into(imgAvatar);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.GALLERY_REQUEST_CODE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent iGallery = new Intent();
                iGallery.setAction(Intent.ACTION_PICK);
                iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(iGallery, Constants.GALLERY_REQUEST_CODE);
            } else {
                Toast.makeText(this, "Permission Denied - " + grantResults.length + " - " + grantResults[0], Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validate(String password, String name, String email){
        boolean result = true;

        if(email == null || email.length() == 0){
            etEmail.setError(Constants.REQUIRE_MESSAGE);
            result = false;
        }

        if(password != null && password.length() != 0){
            if(password.length() < 5){
                etPassword.setError(Constants.PASSWORD_LENGTH_REQUIRE_MESSAGE);
                result = false;
            }
        }

        if(name == null || name.length() == 0){
            etName.setError(Constants.PASSWORD_LENGTH_REQUIRE_MESSAGE);
            result = false;
        }

        return result;
    }

    private void initView(){
        imgBack = findViewById(R.id.imgArrowBack_profile);
        imgAvatar = findViewById(R.id.imgAvatar_profile);
        etUsername = findViewById(R.id.etUsername_profile);
        etPassword = findViewById(R.id.etPassword_profile);
        etName = findViewById(R.id.etName_profile);
        etEmail = findViewById(R.id.etEmail_profile);
        etCode = findViewById(R.id.etCode_profile);
        btnUpdate = findViewById(R.id.btnUpdate_profile);
    }

    private void initDb(){
        db = Room.databaseBuilder(getApplicationContext(), TaskStoreDatabase.class, Constants.DB_NAME).build();
    }

    private void loadData(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            loginUser = (User) bundle.getSerializable("user");
        }
        if(loginUser == null){
            Toast.makeText(this, "Load informatiom failed, please try again", Toast.LENGTH_SHORT).show();
        }
        else{
            Picasso.get()
                    .load(loginUser.getImageUrl())
                    .transform(new CircleTransform())
                    .into(imgAvatar);
            etUsername.setText(loginUser.getUsername());
            etName.setText(loginUser.getName());
            etEmail.setText(loginUser.getEmail());
            etCode.setText(loginUser.getInviteCode());
        }
    }

    private String getRealPathFromURI(Uri contentUri){
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}