package com.example.libraryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.libraryapp.R;
import com.example.libraryapp.common.Constants;

public class ChooseDeviceActivity extends AppCompatActivity implements View.OnClickListener,RadioGroup.OnCheckedChangeListener{
    private RadioGroup radiogroupdevice;
    private RadioButton radiobtn_device1,radiobtn_device2;
    private Button btn_next,btn_cancle;
    private String deviceName;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settingmac);
        initView();
    }

    private void initView() {
        //Radio
        radiogroupdevice=(RadioGroup) findViewById(R.id.rdgroup_devide);
        radiobtn_device1=(RadioButton) findViewById(R.id.rdbutton_device1);
        radiobtn_device2=(RadioButton) findViewById(R.id.rdbutton_device2);
        //Button
        btn_next=(Button) findViewById(R.id.btn_next_st);
        btn_cancle=(Button) findViewById(R.id.btn_cancle_st);
        //setOnlick
        radiogroupdevice.setOnCheckedChangeListener(this);
        btn_next.setOnClickListener(this);
        btn_cancle.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_cancle_st:
                finish();
                break;
            case R.id.btn_next_st:
                if (deviceName.equals("ATS100-SG UHF Reader")){
                    startActivity(new Intent(ChooseDeviceActivity.this, SettingMacActivity.class));

                }else if(deviceName.equals("Toshiba Tec")){
                    startActivity(new Intent(ChooseDeviceActivity.this, MenuDeviceActivity.class));
                }
                break;
        }

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        RadioButton radioButton = findViewById(i);
        deviceName=radioButton.getText().toString();
        Constants.CONFIG_DEVICE_NAME = deviceName;
    }
}
