package com.example.hana.imageservicemobileapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_PERMISSION = 1;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        if (ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        int storagePermission = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            storagePermission = ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE);
        }
        if (storagePermission == PackageManager.PERMISSION_GRANTED) {
            finish();
            startActivity(getIntent());
        } else {
            finish();
        }
    }

    public void startService(View view)
    {
        Intent intent = new Intent(this, ImageServiceService.class);
        startService(intent);
    }

    public void stopService(View view)
    {
        Intent intent = new Intent(this, ImageServiceService.class);
        stopService(intent);
    }

}
