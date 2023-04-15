package com.example.libraryapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.libraryapp.R;
import com.example.libraryapp.Thread.HttpGetTypeTransfer;
import com.example.libraryapp.Thread.HttpPostTypeTransfer;
import com.example.libraryapp.Thread.HttpRfidResponse;
import com.example.libraryapp.adapter.TransferAdapter;
import com.example.libraryapp.common.Config;
import com.example.libraryapp.common.Constants;
import com.example.libraryapp.common.function.SupModRfidCommon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TransferActivity extends AppCompatActivity implements View.OnClickListener, HttpRfidResponse {
    private Spinner spn_contact,spn_type,spn_source,spn_dest,spn_type_transfer;
    private TextView txt_contact,txt_type,txt_source,txt_dest,txt_document,txt_type_transfer;
    private Button btn_cancle, btn_next;
    private String[] typeTransferString={"Choose operation Type", "incoming", "outgoing", "internal"};
    private String[] operationTypeString={"Choose operation Type","San Francisco: Receipts","San Francisco: Internal Transfers","San Francisco: Delivery Orders","San Francisco: Returns"};
    private String[] contactString={"","Azure Interior","Azure Interior, Brandon Freeman","Azure Interior, Colleen Diaz","Azure Interior, Nicole Ford","Azure Interior, tommy","Deco Addict","Deco Addict"};
    private String[] sourceLocationString={"WH/Stock","Partner Locations","Partner Locations/Customers","Partner Locations/Vendors","Physical Locations","Virtual Locations","Virtual Locations/Inventory adjustment","Virtual Locations/Production"};
    private String[] destinationLocationString={"WH/Stock","Partner Locations","Partner Locations/Customers","Partner Locations/Vendors","Physical Locations","Virtual Locations","Virtual Locations/Inventory adjustment","Virtual Locations/Production"};
    private String typetext="";
    private String operationtypetext="";
    private String contacttext="";
    private String sourcetext="";
    private String desttext="";
    private String documenttext;
    private EditText edt_source;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        innitView();

    }

    private void innitView() {
        spn_contact=(Spinner) findViewById(R.id.spinner_transfer2);
        spn_type=(Spinner) findViewById(R.id.spinner_transfer1);
        spn_type_transfer=(Spinner) findViewById(R.id.spinner_type);
        spn_source=(Spinner) findViewById(R.id.spinner_transfer3);
        spn_dest=(Spinner) findViewById(R.id.spinner_transfer4);
        txt_type=(TextView) findViewById(R.id.txt_transfer1);
        txt_type_transfer=(TextView) findViewById(R.id.txt_type);
        txt_contact=(TextView) findViewById(R.id.txt_transfer2);
        txt_source=(TextView) findViewById(R.id.txt_transfer3);
        txt_dest=(TextView) findViewById(R.id.txt_transfer4);
        // button
        btn_cancle=(Button) findViewById(R.id.btn_transfer_return);
        btn_next=(Button) findViewById(R.id.btn_transfer_next);
        btn_cancle.setOnClickListener(this);
        btn_next.setOnClickListener(this);
        // edittext
        edt_source=(EditText) findViewById(R.id.edt_sourcedocument_transfer);
        txt_document=(TextView) findViewById(R.id.txt_document) ;
        new HttpGetTypeTransfer(TransferActivity.this).execute(Config.CODE_LOGIN,"http://192.168.1.59:8069/inventory/transfer/gettypetransfer");
        TransferAdapter typeAdapter=new TransferAdapter(getApplicationContext(),typeTransferString);
        set_type_view(typeAdapter);
        spn_type_transfer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i!=0){
                    typetext=adapterView.getItemAtPosition(i).toString();
                    new HttpPostTypeTransfer(TransferActivity.this).execute(Config.CODE_LOGIN,"http://192.168.1.59:8069/inventory/transfer/gettype",typetext);
                    operationTypeView();
                }
                switch (typetext){
                    case "incoming":{
                        receiptsView();
                        break;
                    }
                    case "outgoing":{
                        deliveryOrdersView();
                        break;
                    }
                    case "internal":{
                        internalTransfersView();
                        break;
                    }
                    case "mrp operation":{
                        internalTransfersView();
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }
    private void reloadTpeview(String[] operationTypeString){
        TransferAdapter transferAdapterType=new TransferAdapter(getApplicationContext(),operationTypeString);
        setedt_view(transferAdapterType);
    }
    private void reloadTypeTransferView(String[] typeTransferString){
        TransferAdapter typeAdapter=new TransferAdapter(getApplicationContext(),typeTransferString);
        set_type_view(typeAdapter);
    }
    private void reloadContactview(String[] contactString){
        TransferAdapter contactAdapter=new TransferAdapter(getApplicationContext(),contactString);
        spn_contact.setAdapter(contactAdapter);
    }
    private void reloadSourceview(String[] sourceLocationString){
        TransferAdapter sourceAdapter=new TransferAdapter(getApplicationContext(),sourceLocationString);
        spn_source.setAdapter(sourceAdapter);
    }
    private void reloadDestview(String[] destinationLocationString){
        TransferAdapter destinationAdapter=new TransferAdapter(getApplicationContext(),destinationLocationString);
        spn_dest.setAdapter(destinationAdapter);
    }
    private void setedt_view(TransferAdapter transferAdapterType){
        spn_type.setAdapter(transferAdapterType);
    }
    private void set_type_view(TransferAdapter typeAdapter){
        spn_type_transfer.setAdapter(typeAdapter);
    }
    private void operationTypeView(){
        txt_type.setVisibility(View.VISIBLE);
        spn_type.setVisibility(View.VISIBLE);
        spn_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i!=0) {
                    operationtypetext=adapterView.getItemAtPosition(i).toString();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    private void contactView(){
        new HttpGetTypeTransfer(TransferActivity.this).execute(Config.CODE_LOGIN,"http://192.168.1.59:8069/inventory/transfer/getcontact");
        spn_contact.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //  contacttext=adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }
    private void sourceLocationView(){
        new HttpGetTypeTransfer(TransferActivity.this).execute(Config.CODE_LOGIN,"http://192.168.1.59:8069/inventory/transfer/getwarehouse");
        spn_source.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //   desttext=adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    private void destinationLocationView(){
        new HttpGetTypeTransfer(TransferActivity.this).execute(Config.CODE_LOGIN,"http://192.168.1.59:8069/inventory/transfer/getwarehousedest");
        spn_dest.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //desttext=adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_transfer_return:{
                finish();
                return;
            }
            case R.id.btn_transfer_next:{
                contacttext=spn_contact.getSelectedItem().toString();
                desttext=spn_source.getSelectedItem().toString();
                sourcetext=spn_source.getSelectedItem().toString();
                documenttext=edt_source.getText().toString();
                if (typetext=="incoming"){
                    desttext=sourcetext;
                    sourcetext="";
                }
                System.out.println(documenttext);
                Intent intent = new Intent(TransferActivity.this, ScanTransferActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("type",operationtypetext);
                bundle.putString("contact",contacttext);
                bundle.putString("source",sourcetext);
                bundle.putString("dest",desttext);
                bundle.putString("sourcedocument",documenttext);
                intent.putExtras(bundle);
                startActivity(intent);

                return;
            }
        }
    }
    private void receiptsView(){
        txt_contact.setVisibility(View.VISIBLE);
        txt_source.setVisibility(View.VISIBLE);
        spn_contact.setVisibility(View.VISIBLE);
        spn_source.setVisibility(View.VISIBLE);
        txt_dest.setVisibility(View.GONE);
        spn_dest.setVisibility(View.GONE);
        txt_contact.setText("Receive From");
        txt_source.setText("Destination Location");
        txt_document.setVisibility(View.VISIBLE);
        edt_source.setVisibility(View.VISIBLE);
        contactView();
        sourceLocationView();
    }
    private void internalTransfersView() {
        txt_document.setVisibility(View.VISIBLE);
        edt_source.setVisibility(View.VISIBLE);
        txt_contact.setVisibility(View.VISIBLE);
        txt_source.setVisibility(View.VISIBLE);
        spn_contact.setVisibility(View.VISIBLE);
        spn_source.setVisibility(View.VISIBLE);
        txt_dest.setVisibility(View.VISIBLE);
        spn_dest.setVisibility(View.VISIBLE);
        txt_contact.setText("Contact");
        txt_source.setText("Source Location");
        txt_dest.setText("Destination Location");
        contactView();
        sourceLocationView();
        destinationLocationView();
    }
    private void deliveryOrdersView() {
        txt_contact.setVisibility(View.VISIBLE);
        txt_source.setVisibility(View.VISIBLE);
        spn_contact.setVisibility(View.VISIBLE);
        spn_source.setVisibility(View.VISIBLE);
        txt_dest.setVisibility(View.GONE);
        spn_dest.setVisibility(View.GONE);
        txt_contact.setText("Delivery Address");
        txt_source.setText("Source Location");
        txt_document.setVisibility(View.VISIBLE);
        edt_source.setVisibility(View.VISIBLE);
        contactView();
        sourceLocationView();

    }
    List<HttpGetTypeTransfer> listHttp = new ArrayList<>();
    @Override
    public void progressRfidFinish(String output, int typeRequestApi, String fileName) {
        if(output.contains("Exception")){
            for(HttpGetTypeTransfer http : listHttp){
                http.cancel(true);
            }
        }
        System.out.println(output);
        System.out.println(output);
        try {
            JSONObject jsonObject = new JSONObject(output);
            if (SupModRfidCommon.isStatusHttpOk(output)) {
                if (jsonObject.getString(Constants.KEY_CODE).equals(Constants.VALUE_CODE_OK)) {
                    JSONArray jArray = jsonObject.getJSONArray(Constants.KEY_DATA);
                    System.out.println("array "+jArray);
                    if (jArray.get(0).equals("typetransfer")){
                        String[] typeTransferString=new String[jArray.length()+1];
                        typeTransferString[0]="Choose Type";
                        for (int j=1;j<jArray.length();j++) {
                            try {
                                typeTransferString[j]=jArray.get(j).toString();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            reloadTypeTransferView(typeTransferString);
                        }
                    }
                    else if (jArray.get(0).equals("type")){
                        String[] operationTypeString=new String[jArray.length()+1];
                        operationTypeString[0]="Choose operation Type";
                        for (int j=1;j<jArray.length();j++) {
                            try {
                                operationTypeString[j]=jArray.get(j).toString();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            reloadTpeview(operationTypeString);
                        }
                    }else if (jArray.get(0).equals("contact")){
                        String[] contactString=new String[jArray.length()+1];
                        contactString=new String[jArray.length()];
                        for (int j=1;j<jArray.length();j++) {
                            try {
                                contactString[j] = jArray.get(j).toString();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        reloadContactview(contactString);
                    }else if (jArray.get(0).equals("source")){
                        String[] sourceLocationString=new String[jArray.length()+1];
                        for (int j=1;j<jArray.length();j++) {
                            try {
                                sourceLocationString[j] = jArray.get(j).toString();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        reloadSourceview(sourceLocationString);
                    }else if (jArray.get(0).equals("dest")){
                        String[] destinationLocationString=new String[jArray.length()+1];
                        for (int j=1;j<jArray.length();j++) {
                            try {
                                destinationLocationString[j] = jArray.get(j).toString();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        reloadDestview(destinationLocationString);
                    }


                }
                }
            } catch (JSONException e) {
            e.printStackTrace();
        }
    }
//    private void newTypeView(JSONArray jArray){
//        String[] operationTypeString=new String[jArray.length()+1];
//        operationTypeString[0]="Choose operation Type";
//        for (int j=1;j<jArray.length();j++) {
//            try {
//                operationTypeString[j]=jArray.get(j).toString();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//        }
//        reloadview(operationTypeString,contactString);
//    }
//    private void newContactView(JSONArray jArray){
//        String[] contactString=new String[jArray.length()+1];
//        contactString=new String[jArray.length()];
//        for (int j=1;j<jArray.length();j++) {
//            try {
//                contactString[j] = jArray.get(j).toString();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        reloadview(operationTypeString,contactString);
//    }
    }


