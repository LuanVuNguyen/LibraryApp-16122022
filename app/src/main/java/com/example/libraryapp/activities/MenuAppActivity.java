package com.example.libraryapp.activities;

import static com.example.libraryapp.activities.SettingMacActivity.PREFS_NAME;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.libraryapp.R;
import com.example.libraryapp.common.Constants;

public class MenuAppActivity extends AppCompatActivity implements View.OnClickListener{
    ImageButton btn_open_product_manage,btn_open_setting_scanner;
    ImageView btn_logout;
    int flag =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_app1);
        initViews();
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MenuAppActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            if(permission.equals("android.permission.BLUETOOTH_CONNECT")) {
                System.out.println("permission BLE COnnect");
                flag =1 ;
            }
            if(flag==1)
                return;
            if (ActivityCompat.shouldShowRequestPermissionRationale(MenuAppActivity.this, permission)) {
                //This is called if user has denied the permission before

                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MenuAppActivity.this, new String[]{permission}, requestCode);
            } else {
                ActivityCompat.requestPermissions(MenuAppActivity.this, new String[]{permission}, requestCode);
            }
        }
//        } else {
//            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        askForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 0);
        askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 0);
        askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, 0);
        askForPermission(Manifest.permission.ACCESS_COARSE_LOCATION, 0);
        //askForPermission(Manifest.permission.BLUETOOTH_ADMIN, 0);
        askForPermission(Manifest.permission.BLUETOOTH_CONNECT, 0);
    }

    private void initViews(){
        btn_logout = (ImageView) findViewById(R.id.btn_back_menu);
        btn_open_product_manage = (ImageButton) findViewById(R.id.btn_open_product_manage);
        btn_open_setting_scanner = (ImageButton) findViewById(R.id.btn_setting);

        btn_logout.setOnClickListener(this);
        btn_open_product_manage.setOnClickListener(this);
        btn_open_setting_scanner.setOnClickListener(this);

        String mac = "";
        String ip = "";
        String port = "";
        String power = "";
        if(!confRead("MAC").isEmpty()){
            mac = confRead("MAC");
            Constants.CONFIG_MAC_HANDWARE = mac;
        }
        if(!confRead("IP").isEmpty()){
            ip = confRead("IP");
            Constants.CONFIG_IP_ADDRESS = ip;
        }
        if(!confRead("PORT").isEmpty()) {
            port = confRead("PORT");
            Constants.CONFIG_PORT = port;
        }
        if(!confRead("POWER").isEmpty()) {
            power = confRead("POWER");
            Constants.CONFIG_POWER_LEVEL = power;
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_back_menu:
                onBackPressed();
                break;
            case R.id.btn_open_product_manage:
                Intent intent = new Intent(MenuAppActivity.this, MenuBussinessActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_setting:
                startActivity(new Intent(MenuAppActivity.this, ChooseDeviceActivity.class));
                break;
        }
    }
    public String confRead( String type) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String obj = settings.getString(type, "");
        return obj;
        // String port = settings.getString("port", "");
    }

}
