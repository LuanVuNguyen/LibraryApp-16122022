package com.example.libraryapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.libraryapp.R;
import com.example.libraryapp.Thread.Callable;
import com.example.libraryapp.Thread.Notificaiton;
import com.example.libraryapp.Thread.httpCreateProduct;
import com.example.libraryapp.common.Config;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class create_new_product extends AppCompatActivity implements View.OnClickListener {
    ImageButton btn_creat, btn_cancle;
    EditText pName,Respon,Rfid,pin;
    ImageView btn_sync;
    ListView lvProduct;
    ArrayAdapter adapter;
    private ArrayList<String> dataList;

    private static  ArrayList<String> myList;
    private resgister_product res = new resgister_product();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_product);
        Init();
        updatelistview();
        lvProduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                String itemValue = (String) lvProduct.getItemAtPosition(i);
                Rfid.setText(itemValue);
            }
        });
    }
    private void Init()
    {
        btn_creat = (ImageButton) findViewById(R.id.btn_res_product);
        btn_cancle = (ImageButton) findViewById(R.id.btn_cancle);
        btn_cancle.setOnClickListener(this);
        btn_creat.setOnClickListener(this);
        pName = (EditText) findViewById(R.id.txt_product_name);
        Respon = (EditText) findViewById(R.id.txt_responsible);
        Rfid= (EditText) findViewById(R.id.txt_cRFID);
        pin = (EditText) findViewById(R.id.txt_pincode);
        lvProduct = (ListView) findViewById(R.id.list_created);
        btn_sync = (ImageView) findViewById(R.id.btn_sync);
        btn_sync.setOnClickListener(this);
//      Ánh xạ ListView và set adapter
        dataList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        lvProduct.setAdapter(adapter);
    }

    private void create_new_product() {
        String pname = pName.getText().toString().toUpperCase();
        String pPincode = pin.getText().toString().toUpperCase();
        String pRfid = Rfid.getText().toString().toUpperCase();
        String Res = Respon.getText().toString();
        String[] words = Res.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1);
            builder.append(capitalizedWord).append(" ");
        }
        String pRes = builder.toString().trim();

        System.out.println(pRes);
        if (pname.isEmpty() && pPincode.isEmpty() && pRfid.isEmpty() && pRes.isEmpty()) {
            Toast.makeText(this, "Please enter full data", Toast.LENGTH_LONG).show();

        }
            else {
            Callable callable = new Callable() {
                @Override
                public void onCallback(String content) {
                    Notificaiton noti = new Notificaiton(create_new_product.this);
                    noti.showMessage(content);
                }
            };
            new httpCreateProduct(this, callable).execute(Config.CODE_LOGIN, Config.HTTP_SERVER_SHOP + Config.API_ODOO_CREATENEWPRODUCT,pRes, pname,pPincode,pRfid );

        }
    }
    private void updatelistview()
    {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        dataList = bundle.getStringArrayList("list");
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        lvProduct.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }


    private BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String newData = intent.getStringExtra("newData");
                System.out.println(newData);
                dataList.add(newData);
 // Cập nhật giá trị của ListView 2 với dữ liệu mới được truyền từ BroadcastReceiver
            }
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(dataReceiver, new IntentFilter("com.example.NEW_DATA_ACTION"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(dataReceiver);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_cancle:
                onBackPressed();
                break;
            case R.id.btn_res_product:
                create_new_product();
                break;
            case R.id.btn_sync:
            {
                updatelistview();
            }
        }
    }
}