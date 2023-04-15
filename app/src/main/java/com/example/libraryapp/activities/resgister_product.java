package com.example.libraryapp.activities;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import android.content.Intent;

import android.graphics.Insets;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.libraryapp.R;

import com.example.libraryapp.Thread.Callable;
import com.example.libraryapp.Thread.ConnectThreadScan;
import com.example.libraryapp.Thread.HttpPostRfid;
import com.example.libraryapp.Thread.HttpPostRfidScan;
import com.example.libraryapp.Thread.HttpRfidResponse;
import com.example.libraryapp.Thread.ScanRFID;
import com.example.libraryapp.Thread.httpPostRegister;
import com.example.libraryapp.Thread.httpShowInfo;
import com.example.libraryapp.adapter.ListViewScanAdapter;
import com.example.libraryapp.common.Config;
import com.example.libraryapp.Thread.Notificaiton;
import com.example.libraryapp.common.Constants;
import com.example.libraryapp.common.Message;
import com.example.libraryapp.common.entities.InforProductEntity;
import com.example.libraryapp.common.function.SupModRfidCommon;
import com.example.libraryapp.fragment.DialogYesNoFragment;
import com.google.android.material.navigation.NavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jp.co.toshibatec.TecRfidSuite;
import jp.co.toshibatec.callback.ResultCallback;


public class resgister_product extends AppCompatActivity implements View.OnClickListener, HttpRfidResponse {
    private ImageButton btn_create,btn_res, btn_search;
    private ImageView btn_del;

    private Context mContext;
    private EditText pincode,rfid;
    private ProgressBar mProgressBar = null;
    private Context mActivity = null;

    List<HttpPostRfid> listHttp = new ArrayList<>();
    Set<String> setCustomOutput = new HashSet<>();

    ScanRFID scanRFID = null;
    private ListView lvProduct;

    private boolean isShowProgress = false;
    private Handler mDissmissProgressHandler = new Handler(Looper.getMainLooper());
    /**
     * プログレスディスミス用ランナブル
     */
    private Runnable mDissmissProgressRunnable = null;
    /**
     * アクセス中のプログレス消去
     */

    private ArrayList<String> dataList;
    private ArrayAdapter<String> adapter;
    private String selectedValue;

    public static JSONArray RFID_list;
    public static JSONArray jArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        setContentView(R.layout.activity_resgister_product);
        Init();
        if(Constants.CONFIG_DEVICE_NAME.equals(Constants.CONFIG_DEVICE_ATS100)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        showProgress();
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    initDeviceScanVN();
                }
            });
        }
        //Listview();
        lvProduct.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvProduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                String itemValue = (String) lvProduct.getItemAtPosition(i);
                rfid.setText(itemValue);
            }
        });

    }

    private void initDeviceScanVN(){
        //bluetoothDeviceConnect();
        ConnectThreadScan connectThreadScan = new ConnectThreadScan();
        scanRFID = new ScanRFID();
        new Thread(new Runnable() {
            @Override
            public void run() {
                scanRFID.connect(bluetoothDeviceConnected2(), resgister_product.this, new com.example.libraryapp.common.interfaces.Callable() {
                    @Override
                    public void call(boolean result) {
                        showToast("起動中…");
                        dismissProgress();
                    }
                });
            }
        }).start();
    }
    private void Init()
    {
        btn_create = (ImageButton) findViewById(R.id.btn_new_product);
        btn_create.setOnClickListener(this);
        btn_res = (ImageButton) findViewById(R.id.btn_register_product);
        btn_res.setOnClickListener(this);
        btn_search = (ImageButton) findViewById(R.id.btn_search_info);
        btn_search.setOnClickListener(this);
        pincode = (EditText) findViewById(R.id.txt_reg_pincode);
        rfid = (EditText) findViewById(R.id.txt_reg_rfid);
        btn_del = (ImageView) findViewById(R.id.btn_delete_reg);
        btn_del.setOnClickListener(this);
        lvProduct = (ListView) findViewById(R.id.lv_RFID);
        dataList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        // Ánh xạ ListView và set adapter
        lvProduct.setAdapter(adapter);
    }
    private void register() {

        String pincode_reg = pincode.getText().toString().toUpperCase();
        String rfid_reg = rfid.getText().toString().toUpperCase();


        if (pincode_reg.isEmpty() && rfid_reg.isEmpty())
            Toast.makeText(this, "PLEASE INPUT PINCODE AND RFID", Toast.LENGTH_SHORT).show();
        else {
            Callable callable = new Callable() {
                @Override
                public void onCallback(String content) {
                    Notificaiton noti = new Notificaiton(resgister_product.this);
                    noti.showMessage(content);
                }
            };
            new httpPostRegister(this, callable).execute(Config.CODE_LOGIN, Config.HTTP_SERVER_SHOP + Config.API_ODOO_REGISTERRFID, pincode_reg, rfid_reg);
        }
    }
    private void showInfo()
    {
        String pincode_reg = pincode.getText().toString().toUpperCase();
        String rfid_reg = selectedValue;
        System.out.println("Gia chi RFID chon: "+rfid_reg);
        if (pincode_reg.isEmpty())
            Toast.makeText(this, "PLEASE ENTER PINCODE", Toast.LENGTH_SHORT).show();
        else {
            Callable callable = new Callable() {
                @Override
                public void onCallback(String content) {
                    Notificaiton noti = new Notificaiton(resgister_product.this);
                    noti.showMessage(content);
                }
            };
        new httpShowInfo(this, callable).execute(Config.CODE_LOGIN,Config.HTTP_SERVER_SHOP+Config.API_ODOO_GETINFOPRODUCT,pincode_reg);
        }

    }
    protected void showProgress() {
        if (mProgressBar == null) {
            mProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
            //スクリーンサイズを取得する
            int width;
            int height;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowMetrics display = this.getWindowManager().getCurrentWindowMetrics();
                // 画面サイズ取得
                Insets insets = display.getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars() | WindowInsets.Type.displayCutout());
                width = display.getBounds().width() - (insets.right + insets.left);
                height = display.getBounds().height() - (insets.top + insets.bottom);
            } else {
                Display display = this.getWindowManager().getDefaultDisplay();
                Point point = new Point();
                display.getSize(point);
                width = point.x;
                height = point.y;
            }
            //ルートビューにProgressBarを貼り付ける
            ViewGroup rootView = (ViewGroup) getWindow().getDecorView();
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mProgressBar.setPadding(width * 3 / 8, height * 3 / 8, width * 3 / 8, height * 3 / 8);
            rootView.addView(mProgressBar, params);

        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        isShowProgress = true;
    }
    private BluetoothDevice bluetoothDeviceConnected2(){
        BluetoothDevice deviceTemp = null;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            int i=0;
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if(i==0)
                    deviceTemp=device;
                if(deviceHardwareAddress.equals(Constants.CONFIG_MAC_HANDWARE)) {
//                    System.out.println("okok");
//                    System.out.println(deviceHardwareAddress);
//                    System.out.println(Constants.CONFIG_MAC_HANDWARE);
                    return device;
                }
                i++;
            }
        }
        return deviceTemp;
    }
    private void showToast(String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(resgister_product.this,s+"",Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void dismissProgress() {
        mDissmissProgressRunnable = new Runnable() {
            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                if (null != mProgressBar) {
                    mProgressBar.setVisibility(ProgressBar.GONE);
                }
            }
        };
        if (null != mDissmissProgressHandler) {
            mDissmissProgressHandler.post(mDissmissProgressRunnable);
            isShowProgress = true;
        }
    }
    @Override
    public void progressRfidFinish(String output, int typeRequestApi, String fileName) {
        // KILL ALL HTTP
        if(output.contains("Exception")){
            for(HttpPostRfid http : listHttp){
                http.cancel(true);
            }
        }
        try {
            JSONObject jsonObject = new JSONObject(output);
            if (SupModRfidCommon.isStatusHttpOk(output)) {
                if (jsonObject.getString(Constants.KEY_CODE).equals(Constants.VALUE_CODE_OK)) {
                    jArray = jsonObject.getJSONArray(Constants.KEY_DATA);
                    RFID_list = jArray.getJSONArray(1);
                    if (RFID_list != null) {
                        for (int i = 0; i < RFID_list.length(); i++) {
                            updateListView(RFID_list.get(i).toString());

                        }
                    }
                } else {
                    SupModRfidCommon.showNotifyErrorDialog(resgister_product.this).show();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Hàm cập nhật giá trị lên ListView
    public void updateListView(String newValue) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Thêm giá trị mới vào danh sách
                if (!dataList.contains(newValue)) {
                    dataList.add(newValue);
                    Intent intent = new Intent("com.example.NEW_DATA_ACTION");
                    intent.putExtra("newData", newValue);
                    sendBroadcast(intent);

                    // Cập nhật lại adapter
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
    @Override
    public void onDestroy() {
        if(Constants.CONFIG_DEVICE_NAME.equals(Constants.CONFIG_DEVICE_ATS100)) {
            scanRFID.cancel();
        }
        super.onDestroy();
    }
    private void Deletedata(){
        ScanRFID.Rfid.clear();
        adapter.clear();
        adapter.notifyDataSetChanged();
    }
    public void sendDatatoNextActivity()
    {
        ArrayList<String> getdataList = new ArrayList<String>();
        Intent intent = new Intent(this, create_new_product.class);
        Bundle bundle = new Bundle();
        for (int i = 0; i < lvProduct.getAdapter().getCount(); i++) {
            getdataList.add(lvProduct.getAdapter().getItem(i).toString());
        }
        bundle.putStringArrayList("list", getdataList);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_register_product:
                register();
                break;
            case R.id.btn_new_product:
                //startActivity(new Intent(this, create_new_product.class));
                sendDatatoNextActivity();
                break;
            case R.id.btn_search_info:
                showInfo();
                break;
            case R.id.btn_delete_reg:
                Deletedata();
                break;
        }
    }

}
