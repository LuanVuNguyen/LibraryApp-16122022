 package com.example.libraryapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.libraryapp.R;
import com.example.libraryapp.adapter.CustomAdapter;
import com.example.libraryapp.local.MyApp;

import java.util.Locale;

 public class MainActivity extends AppCompatActivity implements View.OnClickListener{
     private Button btn_login,btn_change;
     private Spinner spinner;
     Context context;
     public Resources resources;
     int flag =0;
     Configuration config ;
     String[] countryNames={"Select Language","English","Japanse"};
     int flags[] = {0,R.drawable.united_kingdom, R.drawable.japan};
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_login);
         initViews();
     }
     private void initViews(){

         btn_login = (Button) findViewById(R.id.btn_login);
         btn_login.setOnClickListener(this);
         spinner=(Spinner) findViewById(R.id.simpleSpinner);

         CustomAdapter customAdapter=new CustomAdapter(getApplicationContext(),flags,countryNames);
         spinner.setAdapter(customAdapter);
         spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                 Toast.makeText(getApplicationContext(), countryNames[i], Toast.LENGTH_SHORT).show();
                 //String selectLanguage= adapterView.getItemAtPosition(i).toString();
                 switch (i){
                     case 0:
                         break;
                     case 1:
                         config = new Configuration(getResources().getConfiguration());
                         config.locale = Locale.ENGLISH;
                         getResources().updateConfiguration(config, getResources().getDisplayMetrics());
                         finish();
                         startActivity(getIntent());
                         break;
                     case 2:
                         config = new Configuration(getResources().getConfiguration());
                         config.locale = Locale.JAPANESE;
                         getResources().updateConfiguration(config, getResources().getDisplayMetrics());
                         finish();
                         startActivity(getIntent());
                         break;
                 }
             }


             @Override
             public void onNothingSelected(AdapterView<?> adapterView) {

             }
         });

     }
     private void askForPermission(String permission, Integer requestCode) {
         if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
             if(permission.equals("android.permission.BLUETOOTH_CONNECT")) {
                 System.out.println("permission BLE COnnect");
                 flag =1 ;
             }
             if(flag==1)
                 return;
             if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                 ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
             } else {
                 ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
             }
         }

     }
     @Override
     protected void onResume() {
         super.onResume();
         askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 0);
         askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 0);
         askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, 0);
         askForPermission(Manifest.permission.ACCESS_COARSE_LOCATION, 0);
         askForPermission(Manifest.permission.CHANGE_CONFIGURATION, 0);
         askForPermission(Manifest.permission.BLUETOOTH_ADMIN, 0);
         askForPermission(Manifest.permission.BLUETOOTH_CONNECT, 0);
     }

     @Override
     public void onClick(View view) {
         switch (view.getId()) {
             case R.id.btn_login:
                 startActivity(new Intent(MainActivity.this, MenuAppActivity.class));
                 break;
         }
     }
 }


/*
                     switch (i) {
                             case 1: {
                             config = new Configuration(getResources().getConfiguration());
                             config.locale = Locale.JAPANESE;
                             getResources().updateConfiguration(config, getResources().getDisplayMetrics());
                             break;
                             }
                             case 0: {
                             config = new Configuration(getResources().getConfiguration());
                             config.locale = Locale.ENGLISH;
                             getResources().updateConfiguration(config, getResources().getDisplayMetrics());
                             break;
                             }
                             }

                             }*/
