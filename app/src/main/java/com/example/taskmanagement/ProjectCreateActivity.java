package com.example.taskmanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import configurations.CloudinaryConfig;
import constants.Constants;
import database.TaskStoreDatabase;
import executors.AppExecutors;
import models.Project;

public class ProjectCreateActivity extends AppCompatActivity {

    ImageView imgBack;
    EditText etTitle, etContent;
    LinearLayout llChooseFile, llShowFiles;
    Button btnCreate;
    TaskStoreDatabase db;
    Map<String, Uri> fileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_create);

        initView();
        initDb();

        llChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.GALLERY_REQUEST_CODE);
                }
                else{
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    intent.addCategory(Intent.CATEGORY_OPENABLE);

                    try{
                        startActivityForResult(Intent.createChooser(intent, "Selecte a File to Upload"), Constants.FILE_SELECT_CODE);
                    }
                    catch (android.content.ActivityNotFoundException ex){
                        Toast.makeText(ProjectCreateActivity.this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProjectCreateActivity.this, ProjectActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences shared = getSharedPreferences(Constants.LOGIN, MODE_PRIVATE);
                        int userId = shared.getInt(Constants.USER_ID, 0);

                        if(userId != 0){
                            Map<String,String> filePaths = new HashMap<>();

                            if(fileList.size() > 0){
                                Cloudinary cloudinary = CloudinaryConfig.getCloudinary();
                                for(Map.Entry<String, Uri> entry : fileList.entrySet()){
                                    if(entry.getValue() != null){
                                        try{
                                            InputStream is = getContentResolver().openInputStream(entry.getValue());
                                            if(is == null){
                                                Log.e("Null InputStream", "Can not open input stream from provided stream");
                                                return;
                                            }
                                            Map uploadResult = cloudinary.uploader().upload(is, new HashMap());
                                            String fileUrl = uploadResult.get("secure_url").toString();
                                            filePaths.put(entry.getKey(), fileUrl);
                                        }
                                        catch (Exception ex){
                                            Log.e("Error cloudinary", "Ko upload đc file: " + ex.toString());
                                            throw new RuntimeException(ex);
                                        }
                                    }
                                }
                            }

                            Project newProject = new Project(1, etTitle.getText().toString(), etContent.getText().toString(), new Date(), null, userId);
                            if(filePaths.size() > 0){
                                newProject.setFilePaths(filePaths);
                            }

                            db.projectDAO().insert(newProject);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(ProjectCreateActivity.this, ProjectActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                        else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ProjectCreateActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.GALLERY_REQUEST_CODE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                try{
                    startActivityForResult(Intent.createChooser(intent, "Selecte a File to Upload"), Constants.FILE_SELECT_CODE);
                }
                catch (android.content.ActivityNotFoundException ex){
                    Toast.makeText(ProjectCreateActivity.this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Permission Denied - " + grantResults.length + " - " + grantResults[0], Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.FILE_SELECT_CODE){
            if(resultCode == RESULT_OK && data != null){
                Uri uri = data.getData();
                String uriString = uri.toString();
                String fileName = null;

                if(uriString.startsWith("content://")){
                    try(Cursor cursor = getContentResolver().query(uri,null,null,null,null)) {
                        if (cursor != null && cursor.moveToFirst()) {
                            //Get the index of the DISPLAY_NAME column
                            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                            //Get the file name
                            fileName = cursor.getString(nameIndex);
                            cursor.close();
                        }
                    }
                    catch (Exception ex){
                        Toast.makeText(this, "Get file name failed", Toast.LENGTH_SHORT).show();
                    }
                }
                else if(uriString.startsWith("file://")){
                    try{
                        File myFile = new File(uri.getPath());
                        fileName = myFile.getName();
                    }
                    catch (Exception ex){
                        Toast.makeText(this, "Error when load file: " + ex, Toast.LENGTH_SHORT).show();
                    }
                }

                // Create new file component and add to map
                if(fileName != null && uri != null){
                    
                    if(fileList.get(fileName) == null){
                        addNewFile(fileName);
                        fileList.put(fileName, uri);

                        if(fileList.size() == 3){
                            llChooseFile.setEnabled(false);
                        }
                    }
                    else{
                        Toast.makeText(this, "File is already chosen", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(this, "Failed to load file", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(this, "Result code is not ok, or data is null", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Request to get file not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView(){
        imgBack = findViewById(R.id.imgArrowBack_project_create);
        etTitle = findViewById(R.id.etTitle_project_create);
        etContent = findViewById(R.id.etContent_project_create);
        llChooseFile = findViewById(R.id.layoutFile_project_create);
        llShowFiles = findViewById(R.id.linearLayout_addedFile_create_project);
        btnCreate = findViewById(R.id.btnCreate_project_create);
        fileList = new HashMap<>();
    }

    private void initDb(){
        db = Room.databaseBuilder(getApplicationContext(), TaskStoreDatabase.class, Constants.DB_NAME).build();
    }

    private void addNewFile(String displayName){
        LinearLayout newFile = new LinearLayout(this);
        newFile.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        newFile.setBackground(ContextCompat.getDrawable(this, R.drawable.custom_layout_border));
        newFile.setWeightSum(8);
        newFile.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout emptyLeft = new LinearLayout(this);
        emptyLeft.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        ));

        LinearLayout emptyRight = new LinearLayout(this);
        emptyRight.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        ));

        TextView tvFileName = new TextView(this);
        tvFileName.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 5
        ));
        tvFileName.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
        tvFileName.setGravity(Gravity.CENTER_VERTICAL);
        tvFileName.setText(displayName);

        ImageView imgClear = new ImageView(this);
        imgClear.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1
        ));
        imgClear.setImageResource(R.drawable.baseline_clear_24);

        imgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fileList.size() == 3){
                    llChooseFile.setEnabled(true);
                }

                fileList.remove(displayName);
                llShowFiles.removeView(newFile);
            }
        });

        newFile.addView(emptyLeft);
        newFile.addView(tvFileName);
        newFile.addView(imgClear);
        newFile.addView(emptyRight);

        llShowFiles.addView(newFile);
    }
}