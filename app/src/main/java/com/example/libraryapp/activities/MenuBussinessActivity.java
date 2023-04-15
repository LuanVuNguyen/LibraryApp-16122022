package com.example.libraryapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.libraryapp.R;
//import com.example.libraryapp.common.constants.Message;
import com.example.libraryapp.common.Constants;
//import com.example.libraryapp.fragment.DialogYesNoFragment;
//import com.example.libraryapp.fragment.InFragment;
//import com.example.libraryapp.fragment.OutFragment;


public class MenuBussinessActivity extends AppCompatActivity implements View.OnClickListener {
    ImageButton btn_inventory,btn_in,btn_out,btn_issue,btn_transfer,btn_search_rada, btn_res;
    ImageView btn_back_menu;

    //SQLiteDatabaseHandler db;

    TextView inventory_number,incoming_number,outgoing_number;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productmanager);
        initViews();
    }

    private void initViews(){

        //db = new SQLiteDatabaseHandler(this);
        btn_back_menu = (ImageView) findViewById(R.id.btn_back_menu);
        btn_inventory = (ImageButton) findViewById(R.id.btn_inventory);
        inventory_number = (TextView) findViewById(R.id.inventory_number);
        btn_transfer=(ImageButton) findViewById(R.id.btn_transfer);
        btn_search_rada=(ImageButton) findViewById(R.id.btn_search_rada);
        btn_res = (ImageButton) findViewById(R.id.btn_res_product);
//        outgoing_number = (TextView) findViewById(R.id.outgoing_number);
     //   btn_in=(ImageButton) findViewById(R.id.btn_in);
     //   btn_out=(ImageButton) findViewById(R.id.btn_out);
        btn_issue=(ImageButton) findViewById(R.id.btn_issue);
        btn_transfer.setOnClickListener(this);
        btn_back_menu.setOnClickListener(this);
        btn_issue.setOnClickListener(this);
        btn_inventory.setOnClickListener(this);
        btn_search_rada.setOnClickListener(this);
        btn_res.setOnClickListener(this);
        System.out.println("tommymaccon"+ Constants.CONFIG_MAC_HANDWARE);
/*        btn_in.setOnClickListener(this);
        btn_out.setOnClickListener(this);*/

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_inventory:
                           startActivity(new Intent(MenuBussinessActivity.this,ScanDataActivity.class));
                break;
            case R.id.btn_issue:
                           startActivity(new Intent(MenuBussinessActivity.this,IssueActivity.class));
                break;
            case R.id.btn_back_menu:
                onBackPressed();
                break;
            case R.id.btn_transfer:
                startActivity(new Intent(this,TransferActivity.class));
                break;
            case R.id.btn_search_rada:
                startActivity(new Intent(this,SearchRadaMenuActivity.class));
                break;
            case R.id.btn_res_product:
                startActivity(new Intent(this, resgister_product.class));
                break;
            //case R.id.btn_export:
        }
    }



/*    @Override
    protected void onResume() {
        super.onResume();
        if(db.getProductsinvbyTypeCount("inventory")==0)
            inventory_number.setText("(0)");
        else    inventory_number.setText("("+db.getProductsinvbyTypeCount("inventory")+")");
        if(db.getProductsinvbyTypeCount("incoming")==0)
            incoming_number.setText("(0)");
        else    incoming_number.setText("("+db.getProductsinvbyTypeCount("incoming")+")");
        if(db.getProductsinvbyTypeCount("outgoing")==0)
            outgoing_number.setText("(0)");
        else    outgoing_number.setText("("+db.getProductsinvbyTypeCount("outgoing")+")");

    }*/
    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_bussiness);
    }*/


}