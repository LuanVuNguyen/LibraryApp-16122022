package com.example.libraryapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.libraryapp.R;
import com.example.libraryapp.common.Config;
import com.example.libraryapp.common.Constants;

public class SettingMacActivity extends AppCompatActivity implements View.OnClickListener {
    public static String MAC_HANDWARE = null;
    EditText edt_mac,edt_ip,edt_port,edt_power_level;
    Button btn_save,btn_cancel,btn_goto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_1);

        initViews();

    }

    private void initViews(){
        edt_mac = (EditText) findViewById(R.id.edt_mac);
        edt_ip = (EditText) findViewById(R.id.edt_ip);
        edt_port = (EditText) findViewById(R.id.edt_port);
        edt_power_level = (EditText) findViewById(R.id.edt_power_level);
        btn_save = (Button) findViewById(R.id.btn_yes);
        btn_cancel = (Button) findViewById(R.id.btn_no);

        btn_save.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);//LOAD CONFIG
        if(!confRead("MAC").isEmpty())
            edt_mac.setText(confRead("MAC"));
        if(!confRead("IP").isEmpty())
            edt_ip.setText(confRead("IP"));
        if(!confRead("PORT").isEmpty())
            edt_port.setText(confRead("PORT"));
        if(!confRead("POWER").isEmpty())
            edt_power_level.setText(confRead("POWER"));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btn_yes:
                String mac = edt_mac.getText().toString();
                //System.out.println("tommymac"+mac);
                String ip = edt_ip.getText().toString();
                String port = edt_port.getText().toString();
                String power = edt_power_level.getText().toString();
                if(mac.isEmpty()){
                    showToast("MAC DEVICE IS EMPTY");
                    return;
                }

                if(ip.isEmpty()){
                    showToast("IP ADDRESS IS EMPTY");
                    return;
                }

                if(port.isEmpty()){
                    showToast("PORT IS EMPTY");
                    return;
                }
                if(power.isEmpty()){
                    showToast("POWER IS EMPTY");
                    return;
                }
                if(Integer.parseInt(power)>33||Integer.parseInt(power)<0){
                    showToast("POWER NOT in 0-33");
                    return;
                }

                //Write to config file
                confWrite("IP",ip);
                confWrite("PORT",port);
                confWrite("MAC",mac);
                confWrite("POWER",power);
                //grant to constants
                System.out.println("tommymac"+mac);
                Constants.CONFIG_MAC_HANDWARE = mac;
                System.out.println("tommymaccon"+Constants.CONFIG_MAC_HANDWARE);
                Constants.CONFIG_IP_ADDRESS = ip;
                Constants.CONFIG_PORT = port;
                Constants.CONFIG_POWER_LEVEL = power;

                //config http
                Config.HTTP_SERVER_SHOP = "http://"+ip+":"+port;
                Toast.makeText(this, "SAVE SUCCESS!!!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MenuBussinessActivity.class);
                startActivity(intent);
                return;

            case R.id.btn_no:
                onBackPressed();
                return;
        }
    }
    @Override
    public void onBackPressed(){
        Intent intent= new Intent(this,MenuAppActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
    public static final String PREFS_NAME = "MyPrefsFile";
    public void confWrite(String type, String value) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(type, value);
        // Commit the edits!
        editor.commit();
    }
    public String confRead( String type) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String obj = settings.getString(type, "");
        return obj;
        // String port = settings.getString("port", "");
    }
    private void showToast(String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SettingMacActivity.this,s+"",Toast.LENGTH_LONG).show();
            }
        });
    }
}