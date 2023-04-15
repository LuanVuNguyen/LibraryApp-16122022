package com.example.libraryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Insets;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Base64;
import com.example.libraryapp.R;
import com.example.libraryapp.Thread.ConnectThreadScan;
import com.example.libraryapp.Thread.HttpPostIssue;
import com.example.libraryapp.Thread.HttpPostRfid;
import com.example.libraryapp.Thread.HttpPostTransfer;
import com.example.libraryapp.Thread.HttpRfidResponse;
import com.example.libraryapp.adapter.ListViewScanAdapter;
import com.example.libraryapp.common.Config;
import com.example.libraryapp.common.Constants;
import com.example.libraryapp.common.Message;
import com.example.libraryapp.common.entities.InforBookEntity;
import com.example.libraryapp.common.entities.InforProductEntity;
import com.example.libraryapp.common.function.CsvExport;
import com.example.libraryapp.common.function.SupModRfidCommon;
import com.example.libraryapp.common.interfaces.Callable;
import com.example.libraryapp.database.SQLiteDatabaseHandler;
import com.example.libraryapp.fragment.DialogYesNoFragment;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.co.toshibatec.TecRfidSuite;
import jp.co.toshibatec.callback.DataEventHandler;
import jp.co.toshibatec.callback.ErrorEventHandler;
import jp.co.toshibatec.callback.ResultCallback;
import jp.co.toshibatec.model.TagPack;

public class ScanTransferActivity extends AppCompatActivity implements View.OnClickListener, HttpRfidResponse {
    // #ADD
    // #ADD_ERROR
    Button btn_error;
    private int MODE_SCAN = 0;
    private int PAUSE_DEVICE = 0;
    private int IS_SHOW_DIALOG_LIMIT = 0;
    private TextView total_quantity, total_money,total_error;
    private EditText edt_receive_barcode_wireless; // #ADD_BARCODE
    private ImageView btnBack, btnSearch, btnDelete_all, btnDelete, btnSave;
    private ImageView btn_startscan=null;
    // #ADD_BARCODE
    private int flagCustom = 0;
    private int scan_size;
    // #TranVuHoangSon Set custom
    // SDK_ADD
    Set<String> setCustomInput = new HashSet<>();
    Set<String> setCustomOutput = new HashSet<>();

    private LinkedList<InforProductEntity> arrDataInList;
    private LinkedList<InforProductEntity> arrDataInListSum;
    private InforProductEntity inforProductEntity;
    private JSONArray jsonArraytoshiba=null;
    private boolean isKeepScanMagazine = false;
    private boolean isReadBackPress = false;
    private boolean mIsStartReadTags = false;
    private ListView lvProduct;
    private Toolbar nav_icon;
    private DrawerLayout drawer_layout;
    private NavigationView nav_views;
    private JSONArray type,contact,source,dest,sourcedocument =null;
    // #HUYNHQUANGVINH list rfid not found
    Set<String> setRfidNotFound = new HashSet<>();
    SQLiteDatabaseHandler db;
    List<HttpPostRfid> listHttp = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_transfer);
        db = new SQLiteDatabaseHandler(this);
        initViews();
        jsonArraytoshiba=new JSONArray();
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
        else if(Constants.CONFIG_DEVICE_NAME.equals((Constants.CONFIG_DEVICE_TOSHIBATEC))){
            btn_startscan.setImageResource(R.drawable.play);
            btn_startscan.setVisibility(View.VISIBLE);
        }


    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    private Callable callable = new Callable() {
        @Override
        public void call(boolean result) {
            if(result==true){
                dismissProgress();
                showToast("接続されたスキャナー!!");
            }
        }
    };
    ConnectThreadScan connectThreadScan = null;
    private void initDeviceScanVN(){
        //bluetoothDeviceConnect();
        connectThreadScan = new ConnectThreadScan();
        new Thread(new Runnable() {
            @Override
            public void run() {
                connectThreadScan.connect(bluetoothDeviceConnected2(), ScanTransferActivity.this, new Callable() {
                    @Override
                    public void call(boolean result) {
                        showToast("起動中…");
                        dismissProgress();
                    }
                });

            }
        }).start();

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
                    System.out.println("okok");
                    System.out.println(deviceHardwareAddress);
                    System.out.println(Constants.CONFIG_MAC_HANDWARE);
                    return device;
                }
                i++;
            }
        }
        return deviceTemp;
    }
    /**
     * プログレス表示フラグ
     */
    private boolean isShowProgress = false;
    /**
     * ライブラリアクセス中プログレス
     */
    private ProgressBar mProgressBar = null;

    // #SON_RECONNECT
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
    private static LinkedList<InforProductEntity> groupByCustom(LinkedList<InforProductEntity> listInfoProductEntity){
        Map<String, List<InforProductEntity>> map = new HashMap<String, List<InforProductEntity>>();

        for (InforProductEntity product : listInfoProductEntity) {
            String key  = product.getBarcodeCD1();
            if(map.containsKey(key)){
                List<InforProductEntity> list = map.get(key);
                list.add(product);

            }else{
                List<InforProductEntity> list = new ArrayList<InforProductEntity>();
                list.add(product);
                map.put(key, list);
            }

        }
        System.out.println(map);
        LinkedList<InforProductEntity> listReturn = new LinkedList<>();
        for(Map.Entry<String,List<InforProductEntity>> entry : map.entrySet()){
            int quantity = entry.getValue().size();
            InforProductEntity a = entry.getValue().get(0);
            a.setQuantity(quantity);
            listReturn.add(a);
        }

        return listReturn;
    }
    /**
     * プログレスディスミス用ハンドラー
     */
    private Handler mDissmissProgressHandler = new Handler(Looper.getMainLooper());
    /**
     * プログレスディスミス用ランナブル
     */
    private Runnable mDissmissProgressRunnable = null;
    /**
     * アクセス中のプログレス消去
     */
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
            isShowProgress = false;
        }
    }


    /**
     * Refresh list view
     */
    private void restartListView() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update list view
                ListViewScanAdapter adapterBook = new ListViewScanAdapter(ScanTransferActivity.this,
                        arrDataInList);
                lvProduct.setAdapter(adapterBook);
                // Show total number and price
                callTotalNumberAndPrice();

                // Get size of data scan in list view
                scan_size = arrDataInList.size();
            }
        });

    }
    /**
     * Update list view after scan
     * flag = 0 -> barcode Scan
     * flag = 1 -> RFID Scan -> will hide disableScanHoneyWell
     */
    private void updateCurrentView() {

        arrDataInList.add(0, inforProductEntity);
        inforProductEntity = new InforProductEntity();

        // Insert current record to database
        //checkAndInsert1RecordDatabase();

        restartListView();

    }



    private void initViews(){
        total_quantity = (TextView) findViewById(R.id.total_quantity_transfer);
        total_money = (TextView) findViewById(R.id.total_money_transfer);
        total_error = (TextView) findViewById(R.id.total_error_transfer);
        //edt_receive_barcode_wireless = (EditText) findViewById(R.id.edt_receive_barcode_wireless);
        inforProductEntity = new InforProductEntity();
        arrDataInList = new LinkedList<>();
        lvProduct = (ListView) findViewById(R.id.list_scan);

        btnBack = (ImageView) findViewById(R.id.btn_back_transfer);
        btnSearch = (ImageView) findViewById(R.id.btn_search);
        //btnDelete = (LinearLayout) findViewById(R.id.btn_delete);
        btnSave = (ImageView) findViewById(R.id.btn_save_data_transfer);
        btnDelete_all = (ImageView) findViewById(R.id.btn_delete_all_transfer);
        btn_startscan=(ImageView) findViewById(R.id.btn_startscan_transfer);
        //btnMode = (LinearLayout) findViewById(R.id.btn_mode);
        // #ADD_ERROR
        btn_error = (Button) findViewById(R.id.btn_error_transfer);
        //#Navigation
        nav_icon=(Toolbar) findViewById((R.id.nav_icon));
        drawer_layout=(DrawerLayout) findViewById((R.id.drawer_layout));
        nav_views=(NavigationView) findViewById(R.id.nav_views);
        btnBack.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        //btnDelete.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnDelete_all.setOnClickListener(this);
        btn_startscan.setOnClickListener(this);
        //btnMode.setOnClickListener(this);
        // #ADD_ERROR
        btn_error.setOnClickListener(this);
        //# navigation
        nav_icon.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer_layout.openDrawer(GravityCompat.START);
            }
        });
        View headerView = nav_views.getHeaderView(0);
        ImageView nav_back=(ImageView) headerView.findViewById(R.id.nav_back);
        nav_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer_layout.closeDrawer(GravityCompat.START);
                eventClickBack();
            }
        });

        nav_views.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id=item.getItemId();
                drawer_layout.closeDrawer(GravityCompat.START);
                switch (id)
                {
                    case R.id.nav_account:
                        Toast.makeText(ScanTransferActivity.this,"近日公開!",Toast.LENGTH_SHORT).show();break;
                    case R.id.nav_manager:
                        drawer_layout.closeDrawer((GravityCompat.START));
                        eventClickBack();
                    case R.id.nav_setting:
                        Toast.makeText(ScanTransferActivity.this,"近日公開!",Toast.LENGTH_SHORT).show();break;
                    case R.id.nav_themes:
                        Toast.makeText(ScanTransferActivity.this,"近日公開!",Toast.LENGTH_SHORT).show();break;
                    case R.id.nav_logout:
                        Toast.makeText(ScanTransferActivity.this,"近日公開!",Toast.LENGTH_SHORT).show();break;
                    default:
                        return true;
                }
                return true;
            }
        });
        reloadSQLiteData();

    }


    private void reloadSQLiteData(){
        setCustomOutput.clear();
        setCustomOutput.clear();
        //ADD SQLITE DATA
        for(InforProductEntity i : db.getAllProductsinvbyType("inventory")){
            setCustomInput.add(i.getRfidCode());
            setCustomOutput.add(i.getRfidCode());
        }
    }
    /**
     * Update Price + Number Return
     */
    private void callTotalNumberAndPrice() {

        int intQuantity = 0;
        double intMoney = 0;
        for (int i = 0; i < arrDataInList.size(); i++) {
            intQuantity += arrDataInList.get(i).getQuantity();
            intMoney += arrDataInList.get(i).getBasePrice();
        }
        total_quantity.setText(MessageFormat.format("{0} : {1}", String.valueOf(getText(R.string.total_quantity)), intQuantity));
        total_money.setText(MessageFormat.format("{0} : {1}", getText(R.string.total_amount), intMoney+""));

    }

    @Override
    public void onDestroy() {
        if(Constants.CONFIG_DEVICE_NAME.equals(Constants.CONFIG_DEVICE_ATS100)) {
            connectThreadScan.cancel();
        }
        else if (TecRfidSuite.OPOS_SUCCESS != mLib.stopReadTags(mStopReadTagsResultCallback)){
        }
        super.onDestroy();


    }
    //2002000005526
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return arrDataInList;
    }
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_back_transfer:
                eventClickBack();
                break;
            case R.id.btn_startscan_transfer:

                startReadtag();

                break;
/*            case R.id.btn_search:
                eventCLickSearch();
                //initDataCustom();
               break;*/
            case R.id.btn_delete_all_transfer:
                eventClickDeleteAll();
                //initOneDataCustom();
                break;
            case R.id.btn_save_data_transfer:
                transfer_product();
                //eventManualInput();
                break;
            //case R.id.btn_delete:
            //initDataCustom();
            //eventClickDelete();
            //break;
            case R.id.btn_error_transfer:
                showDialog();
                break;
           /* case R.id.btn_mode:
                eventClickMode();
                break;*/
        }
    }
    private void transfer_product(){
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        type=new JSONArray();
        contact=new JSONArray();
        source=new JSONArray();
        dest=new JSONArray();
        sourcedocument=new JSONArray();

        if (bundle != null) {
            type.put(bundle.getString("type", ""));
            contact.put(bundle.getString("contact", ""));
            source.put(bundle.getString("source", ""));
            dest.put(bundle.getString("dest", ""));
            sourcedocument.put(bundle.getString("sourcedocument",""));


        }
        System.out.println("type:"+type);

        JSONArray jsonArrayTransfer1=null;
        jsonArrayTransfer1=new JSONArray();
        db.insertAllProductsinv(arrDataInList);
        for(InforProductEntity p : db.getAllProductsinvbyType("inventory")) {
            jsonArrayTransfer1.put(p.getRfidCode());
        }
        new HttpPostTransfer(this).execute(Config.CODE_LOGIN,"http://192.168.1.59:8069/inventory_controller/inventory/create_transfer/transfer", type.toString(),contact.toString(),source.toString(),dest.toString(),sourcedocument.toString(),jsonArrayTransfer1.toString());
    }


    private void showToast(String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ScanTransferActivity.this,s+"",Toast.LENGTH_LONG).show();
            }
        });
    }



    /**
     * Setting list_view in register data screen
     */
    private void initListViewScreen() {

        // check array null
        arrDataInList = (LinkedList<InforProductEntity>) getLastCustomNonConfigurationInstance();

        if (arrDataInList == null) {
            arrDataInList = new LinkedList<>();
        }

        // Check if array is not null
        restartListView();

    }

    /**
     * Function click back
     */

    private void eventClickBack() {

        if (arrDataInList.size() > 0) {
            eventDisableButton();
            showDialogConfirmBack();
        } else {
            onBackPressed();
        }

    }
    /**
     * Function show dialog confirm back
     */
    private void showDialogConfirmBack(){
        DialogYesNoFragment dialogYesNoFragment=new DialogYesNoFragment(this, "再確認", Message.MESSAGE_CONFIRM_REGISTER_DATA, new Callable() {
            @Override
            public void call(boolean result) {
                if(result==true){
                    db.insertAllProductsinv(arrDataInList);
                    onBackPressed();
                }else{
                    onBackPressed();
                }
            }
        });
        loadFragment(dialogYesNoFragment);
    }

    @Override
    public void onBackPressed() {
        if (isShowProgress) {
            dismissProgress();
        }
        // 読取中の場合
        if (mIsStartReadTags) {
            mIsStartReadTags = false;
            isReadBackPress = true;
            if (TecRfidSuite.OPOS_SUCCESS != mLib.stopReadTags(mStopReadTagsResultCallback)){
            }
        } else{
            super.onBackPressed();
        }
    }
    /**sea
     * Function show dialog confirm back
     */
    private void showDialogMessageConfirmSaveToContinue() {
        if(IS_SHOW_DIALOG_LIMIT==0) {
            //STOP DEVICE SCAN
            IS_SHOW_DIALOG_LIMIT=1;
            PAUSE_DEVICE = 1;
            //Show message confirm
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ScanTransferActivity.this);
            alertDialog.setMessage(String.format(Message.MESSAGE_CONFIRM_OVER_DATA, Constants.LIMIT_ONCE));

            alertDialog.setCancelable(false);

            // Configure alert dialog button
            alertDialog.setPositiveButton(Message.YES_REGISTER_DATA, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Save list view to database
                    showProgressRunUi();
                    db.insertAllProductsinvCallBack(arrDataInList, new Callable() {
                        @Override
                        public void call(boolean result) {
                            if(result==true){
                                showToast(arrDataInList.size()+"");
                                //arrDataInList.clear();
                                restartListView();
                                eventEnableButton();
                                IS_SHOW_DIALOG_LIMIT=0;
                                dismissProgress();
                            }
                        }
                    });

                }
            });
            alertDialog.setNegativeButton(Message.NOT_REGISTER_DATA, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    eventEnableButton();
                    IS_SHOW_DIALOG_LIMIT=0;
                }
            });

            AlertDialog alert = alertDialog.show();
            eventDisableButton();
            //eventOpenButton(false);
            TextView messageText = (TextView) alert.findViewById(android.R.id.message);
            assert messageText != null;
            messageText.setGravity(Gravity.CENTER);
        }
    }

    private void showProgressRunUi(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress();
            }
        });
    }
    /**
     * Function click Export
     */
    private void eventClickExport() {

        //showDialogMessageConfirmExport();
        DialogYesNoFragment dialogYesNoFragment=new DialogYesNoFragment(this, "エクスポート", Message.MESSAGE_CONFIRM_EXPORT_DATA, new Callable() {
            @Override
            public void call(boolean result) {
                if(result==true){
                    eventExportYes();
                }else{
                    dismissProgress();
                }
            }
        });
        loadFragment(dialogYesNoFragment);
    }
    /**
     * Function eventExport
     */
    private void eventExportYes(){
        db.insertAllProductsinv(arrDataInList);

        if(!db.getAllProductsinvbyType("inventory").isEmpty()){
            showProgressRunUi();
            String[] header = new String[] {"rfid", "product_name", "quantity","barcode"};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CsvExport.writeData(this, header,Constants.TYPE_TABLE_INVENTORY,new Callable(){
                    @Override
                    public void call(boolean result) {
                        showToast("エクスポートに成功しました！！！");
                        dismissProgress();
                        showDialogMessageExportYes();
                    }
                });
            }

        }
        else showToast("削除レコードはありません！！！");
    }
    /**
     * Function showDialogMessageExport
     */
    private void showDialogMessageExportYes(){
        DialogYesNoFragment dialogYesNoFragment=new DialogYesNoFragment(this, "エクスポート", Message.MESSAGE_CONFIRM_REMOVE_ADD_DATA, new Callable() {
            @Override
            public void call(boolean result) {
                if(result==true){
                    db.deleteAllProductsinvbyTypeTable(Constants.TYPE_TABLE_INVENTORY);
                    onBackPressed();
                }else{
                    dismissProgress();
                }
            }
        });
        loadFragment(dialogYesNoFragment);
    }


    /**
     * loadfragment
     */
    private void loadFragment(Fragment fragment) {
// create a FragmentManager
        try {
            System.out.println("loadFragment: ");
            FragmentManager fm = getFragmentManager();
// create a FragmentTransaction to begin the transaction and replace the Fragment
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
// replace the FrameLayout with new Fragment
            fragmentTransaction.replace(R.id.linear_fragment, fragment);
            fragmentTransaction.commit(); // save the changes
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Function click delete all
     */
    private void eventClickDeleteAll() {
        //showDialogMessageConfirmExport();
        if (arrDataInList.size() > 0) {
            DialogYesNoFragment dialogYesNoFragment=new DialogYesNoFragment(this, "全て削除", Message.MESSAGE_CONFIRM_DELETE_ALL, new Callable() {
                @Override
                public void call(boolean result) {
                    if(result==true){
                        actionDeleteAll(Constants.TYPE_TABLE_INVENTORY);
                        showToast("削除に成功しました！！！");
                    }else{
                        dismissProgress();
                    }
                }
            });
            loadFragment(dialogYesNoFragment);
        } else {
            showToast("削除レコードはありません！！！");
        }
    }
    /**
     * Action delete all record
     */
    private void actionDeleteAll(String type) {
        if (!arrDataInList.isEmpty()) {
            setCustomInput.clear();
            setCustomOutput.clear();
            mReadData.clear();
            mShowReadData.clear();
            reloadSQLiteData();
            jsonArraytoshiba=new JSONArray();
            inforProductEntity = new InforProductEntity();
            db.deleteAllProductsinvbyTypeTable(type);
            initListViewScreen();
        }
    }
    /**
     * Action change mode
     */
    private void actionChangeMode() {
        if(MODE_SCAN==0){

            MODE_SCAN=1;
        }
        else if(MODE_SCAN==1){


            MODE_SCAN=0;
        }
    }

    /**
     * Function click delete
     */
    private void eventClickDelete() {


    }

    /**
     * Function click delete
     */
    private void eventClickMode() {


    }

    private void setDataEntity(JSONObject obj) {
        String bar1= null;
        String bar2= null;
        String rfid= null;
        String name = null;
        int quantity=0;
        int cost = 0;
        int tax = 0;
        try {
            bar1 = obj.getString(Constants.KEY_JANCODE_1);
            quantity=obj.getInt("quantity");
            name = obj.getString(Constants.KEY_GOOD_NAME);
            if(!obj.getString(Constants.KEY_TAX).equals("null"))
                tax = obj.getInt(Constants.KEY_TAX);
            else tax=0;
            if(!obj.getString(Constants.KEY_COST).equals("null"))
                cost = obj.getInt(Constants.KEY_COST);
            else cost=0;
            rfid = obj.getString(Constants.KEY_RFID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        inforProductEntity.setBarcodeCD1(bar1);
        inforProductEntity.setBasePrice(cost);
        inforProductEntity.setTypeProduct(Constants.TYPE_TABLE_INVENTORY);
        inforProductEntity.setQuantity(quantity);
        inforProductEntity.setTaxIncludePrice(tax);
        inforProductEntity.setGoodName(name);
        inforProductEntity.setRfidCode(rfid);
        processBarcode(bar1,cost);
/*        inforProductEntity.setBarcodeCD2(bar2);
        processBarcode(bar2);*/

    }
    private void processBarcode(String strBarcode,int cost){
        //CHECK OVER LIMIT ONCE
        if(arrDataInList.size()>=Constants.LIMIT_ONCE){
            showDialogMessageConfirmSaveToContinue();
            return;
        }
        if(strBarcode.isEmpty()){
            return;
        }int price = cost;

        digestBarcode(strBarcode,price);
    }
    /**
     * Digest type of barcode (magazine, japan magazine, others)
     */
    private void digestBarcode(String bar_code,int money) {
        addOtherBarcode(bar_code);


    }
    /**
     * Show Toast barcode in validate
     */
    private void toastBarcodeInvalidate() {

        //noReturnSound.start();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ScanTransferActivity.this, Constants.INVALID_BARCODE, Toast.LENGTH_SHORT).show();
            }
        });
    }
    /**
     * Add barcode japan magazine
     */
    private void addBarcodeJapanMagazine(String bar_code, int money) {

        if (money == 0) {
            inforProductEntity.setBarcodeCD1(bar_code);
            inforProductEntity.setBarcodeCD2(Constants.BLANK);
            inforProductEntity.setBasePrice(money);
            updateCurrentView();
            showDialogMessageInvalidBarcode();
        } else {
            inforProductEntity.setBarcodeCD1(bar_code);
            inforProductEntity.setBarcodeCD2(Constants.BLANK);
            inforProductEntity.setBasePrice(money);
            updateCurrentView();
        }

    }
    /**
     * Function show dialog message
     */
    private void showDialogMessageInvalidBarcode() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                eventDisableButton();
                //eventOpenButton(false);

                AlertDialog.Builder dialog =
                        new AlertDialog.Builder(ScanTransferActivity.this);
                dialog
                        .setMessage(Message.NOTIFICATION_BARCODE_INVALID)
                        .setCancelable(false)
                        .setNegativeButton(Message.MESSAGE_YES,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        eventClickDelete();
                                        eventEnableButton();
                                        //eventOpenButton(true);
                                    }
                                });

                AlertDialog alert = dialog.show();
                TextView messageText = (TextView) alert.findViewById(android.R.id.message);
                assert messageText != null;
                messageText.setGravity(Gravity.CENTER);
            }
        });

    }
    /**
     * Disable onclick action
     */
    private void eventDisableButton() {

        btnSearch.setClickable(false);
        btnBack.setClickable(false);
        //btnDelete.setClickable(false);
        btnDelete_all.setClickable(false);
        btnSave.setClickable(false);
        // #ADD_ERROR
        btn_error.setClickable(true);

    }
    /**
     * Enable onclick action
     */
    private void eventEnableButton() {

        btnSearch.setClickable(true);
        btnBack.setClickable(true);
        //btnDelete.setClickable(true);
        btnDelete_all.setClickable(true);
        btnSave.setClickable(true);
        // #ADD_ERROR
        btn_error.setClickable(true);

    }

    /**
     * Add barcode magazine
     */
    private void addBarcodeMagazine(String bar_code, int first3character, int money) {

        switch (first3character) {
            case Constants.CD1_978:
                inforProductEntity.setBarcodeCD1(bar_code);
                inforProductEntity.setBarcodeCD2(Constants.BLANK);
                break;
            case Constants.CD2_191:
            case Constants.CD2_192:
                inforProductEntity.setBarcodeCD1(Constants.BLANK);
                inforProductEntity.setBarcodeCD2(bar_code);
                inforProductEntity.setBasePrice(money);
                break;
        }
        updateCurrentView();

    }

    /**
     * Append barcode magazine when old barcode not finish
     */
    private void appendBarcodeMagazine(String bar_code, int first3character, int money) {

        switch (first3character) {
            case Constants.CD1_978:
                arrDataInList.get(0).setBarcodeCD1(bar_code);
                break;
            case Constants.CD2_191:
            case Constants.CD2_192:
                arrDataInList.get(0).setBarcodeCD2(bar_code);
                arrDataInList.get(0).setBasePrice(money);
                break;
        }
        restartListView();

    }
    /**
     * Add barcode others
     */
    ToneGenerator toneG;
    private void addOtherBarcode(String bar_code) {

        // SA-150 修正_UPC-A対応 EDIT START
//        inforProductEntity.setProductCode1(bar_code);
        int bar_code_length = bar_code.length();
        switch (bar_code_length) {
            case 8:
                inforProductEntity.setBarcodeCD1(bar_code + "     ");
                break;
            case 12:
                inforProductEntity.setBarcodeCD1("0" + bar_code);
                break;
            default:
                inforProductEntity.setBarcodeCD1(bar_code);
                break;
        }
        // SA-150 修正_UPC-A対応 EDIT END
        inforProductEntity.setBarcodeCD2(Constants.BLANK);
        // #SON_CLOSED
        //inforProductEntity.setBasePrice(0);
        //SOUND_CLOSED
        updateCurrentView();
        try {
            toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            //toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE, 200);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);

        }catch (RuntimeException e ){e.printStackTrace();}
        //CHECK OVER LIMIT ONCE
        if(arrDataInList.size()>=Constants.LIMIT_ONCE){
            showDialogMessageConfirmSaveToContinue();
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
        System.out.println(output);
        System.out.println(output);
        try {
            Log.d("OUTPUT", output);
            System.out.println("KKKS: "+output);
            JSONObject jsonObject = new JSONObject(output);
            if (SupModRfidCommon.isStatusHttpOk(output)) {
                if (jsonObject.getString(Constants.KEY_CODE).equals(Constants.VALUE_CODE_OK)) {
                    JSONArray jArray = jsonObject.getJSONArray(Constants.KEY_DATA);
                    JSONArray jArray1 = jArray.getJSONArray(0);
                    for (int j=0;j<jArray1.length();j++){
                        JSONObject obj2 = jArray1.getJSONObject(j);
                        String stringRfid= obj2.getString(Constants.KEY_RFID);
                        // #MARK_2
                        if(setCustomOutput.add(stringRfid)){

                            setDataEntity(obj2);
                        }
                    }

                    JSONArray err = jArray.getJSONArray(1);
                    if (err != null) {
                        for (int i = 0; i < err.length(); i++) {
                            // #MARK_4
                            setRfidNotFound.add(err.get(i).toString());
                            // #ADD_ERROR
                            if(btn_error.getVisibility()==View.INVISIBLE)
                                btn_error.setVisibility(View.VISIBLE);
                        }

                        total_error.setText(MessageFormat.format("{0} : {1}", getText(R.string.total_error), setRfidNotFound.size()+""));
                    }

                }
            } else {
                SupModRfidCommon.showNotifyErrorDialog(ScanTransferActivity.this).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // #ADD_ERROR
    private void showDialog() {
        PAUSE_DEVICE=1;
        String message = "" ;
        for(String i : setRfidNotFound){
            message+=i+"\r\n";
        }
        String title = "▲　RFID Invalid";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PAUSE_DEVICE=0;
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * startReadTags用引数
     */
    /** 読み取るタグを決定する際にfiltermaskと論理積したものとの比較を行うためのビットパターン */
    private String mFilterID = "00000000";
    /** 読み取るタグを決定する際に論理積を行うためのビットパターン */
    private String mFiltermask = "00000000";
    /** timeout */
    private int mStartReadTagsTimeout = 10000;
    /** ライブラリインスタンス */
    private String tommycheckkey="";
    //    private CustomUpdateReadTagDataTask mCustomUpdateReadTagDataTask = null;
    private ScanTransferActivity.UpdateReadTagDataTask mUpdateReadTagDataTask = null;


    private static final TecRfidSuite mLib = TecRfidSuite.getInstance();
    private ArrayList<String> mReadData = new ArrayList<String>();
    private void startReadtag(){
        if (!mIsStartReadTags){
            btn_startscan.setImageResource(R.drawable.play_u);
            showToast("Start scan!!!");
            mIsStartReadTags = true;
            if (TecRfidSuite.OPOS_SUCCESS != mLib.startReadTags(mFilterID, mFiltermask, mStartReadTagsTimeout, mDataEvent, mErrorEvent)){
                // エラー表示
                //showDialog(getString(R.string.title_error), getString(R.string.message_processfailed_stopReadTags), getString(R.string.btn_txt_ok), null);
            }
            // setDataEventEnabledを失敗した場合
            if (TecRfidSuite.OPOS_SUCCESS != mLib.setDataEventEnabled(true)){
                // エラー表示
                //showDialog(getString(R.string.title_error), getString(R.string.message_processfailed_setDataEventEnabled), getString(R.string.btn_txt_ok), null);
            }
        } else {
            btn_startscan.setImageResource(R.drawable.play);
            showToast("Stop scan!!!");
            // stopReadTagsを成功した場合
            if (TecRfidSuite.OPOS_SUCCESS == mLib.stopReadTags(mStopReadTagsResultCallback)) {
                // プログレスバーを表示
            } else{
                // エラー表示
                //showDialog(getString(R.string.title_error), getString(R.string.message_processfailed_stopReadTags), getString(R.string.btn_txt_ok), null);
            }
        }

    }
    private ResultCallback mStopReadTagsResultCallback = new ResultCallback() {
        @Override
        public void onCallback(int resultCode, int resultCodeExtended) {
            // stopReadTagsが失敗した場合
            if (TecRfidSuite.OPOS_SUCCESS != resultCode){
                // エラー表示
                //showDialog(getString(R.string.title_error), getString(R.string.message_processfailed_stopReadTags), getString(R.string.btn_txt_ok), null);
            }
            // プログレスバーを消去
            dismissProgress();
            mIsStartReadTags = false;
            // buttonValid();
            // 読取テスト中にバックキーが押下された場合
            if(isReadBackPress){
                isReadBackPress = false;
                finish();
            }
        }
    };

    private DataEventHandler mDataEvent = new DataEventHandler() {
        @Override
        public void onEvent(HashMap<String, TagPack> tagList) {
            for (Map.Entry<String, TagPack> e : tagList.entrySet()) {
                // 受信データからタグ情報を取得
                String key = e.getKey();
                // 追加


                mReadData.add(key);
                tommycheckkey=tommycheckkey+key;
                System.out.println("Tommycheckkey"+tommycheckkey);


            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
//                mCustomUpdateReadTagDataTask = new CustomUpdateReadTagDataTask();
//                mCustomUpdateReadTagDataTask.execute("");
            } else {
                mUpdateReadTagDataTask = new ScanTransferActivity.UpdateReadTagDataTask();
                mUpdateReadTagDataTask.execute("");
            }

        }
    };
    /** キャリアセンスエラー */
    private static final int CARRIERSENSEERROR = 19;
    /** 電波出力禁止エラー */
    private static final int WAVEOUTPUTBLOCKERROR = 21;
    /** タグデータバッファフルエラー */
    private static final int TAGDATAFULLBUFFERERROR = 65;
    /** エラーイベント用コールバック(startReadTags用) */
    private ErrorEventHandler mErrorEvent = new ErrorEventHandler() {
        @Override
        public void onEvent(int resultCode, int resultCodeExtended) {
            // startReadTagsが失敗した場合
            if (TecRfidSuite.OPOS_SUCCESS != resultCode){
                if (resultCodeExtended != CARRIERSENSEERROR && resultCodeExtended != WAVEOUTPUTBLOCKERROR && resultCodeExtended != TAGDATAFULLBUFFERERROR) {
                    // エラー表示
                    //showDialog(getString(R.string.title_error), getString(R.string.message_processfailed_startReadTags), getString(R.string.btn_txt_ok), null);
                }
            }
        }
    };
    private ArrayList<String> mShowReadData = new ArrayList<String>();
    private Boolean Check=true;
    private void soundBeep() {
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
        toneGenerator.release();
    }
    private class UpdateReadTagDataTask extends AsyncTask<String, String, Long> {

        @Override
        protected void onPostExecute(Long result) {
            // setDataEventEnabledを失敗した場合
            if (TecRfidSuite.OPOS_SUCCESS != mLib.setDataEventEnabled(true)){
                // エラー表示
                // showDialog(getString(R.string.title_error), getString(R.string.message_processfailed_setDataEventEnabled), getString(R.string.btn_txt_ok), null);
            }
            super.onPostExecute(result);
        }
        @Override
        protected void onProgressUpdate(String... values) {
            // 追加・更新しない
            int size = mShowReadData.size();
            // リストビューに追加する
            for (int i = 0; i < values.length; i++) {
                if(null != values[i] && 0 != values[i].length()){
                    if (Check.equals(true)) {
                        if (mShowReadData.indexOf(values[i]) == -1) {
                            size = mShowReadData.size();
                            // アダプターへデータを追加
                            mShowReadData.add(values[i]);
                        }
                    }
                    else {
                        size = mShowReadData.size();
                        //System.out.println("Tommycheckvalues3: "+values[i]);
                        // アダプターへデータを追加
                        mShowReadData.add(values[i]);
                    }
                }
            }
            // アダプターを更新
        }

        @SuppressLint("WrongThread")
        @Override
        protected Long doInBackground(String... params) {

            ArrayList<String> a = new ArrayList<String>();
            // 重複排除にチェックがはいっていれば
            if (Check.equals(true)) {
                // 新しい読取タグデータ分ループ
                for (int i = 0; i < mReadData.size(); i++) {
                    // 重複していなければ
                    if (-1 == mShowReadData.indexOf(mReadData.get(i))) {
                        System.out.println("Tommycheckvalues1: "+mReadData.get(i));
                        jsonArraytoshiba.put(mReadData.get(i).toUpperCase());
                        System.out.println(jsonArraytoshiba);
                        if (jsonArraytoshiba.length() != 0) {
                            new HttpPostRfid(ScanTransferActivity.this).execute(Config.CODE_LOGIN,Config.HTTP_SERVER_SHOP+Config.API_ODOO_GETMULTIPLEPRODUCT, jsonArraytoshiba.toString());

                        }

                        a.add(mReadData.get(i));
                        // 50個追加された場合
                        if (a.size() >= 50) {
                            // 表示更新
                            publishProgress(a.toArray(new String[a.size()]));
                            a.clear();
                            // ビープ音鳴音
                            soundBeep();
                        }
                    }
                }
            }
            else {
                // 新しい読取タグデータ分ループ
                for (int i = 0; i < mReadData.size(); i++) {
                    a.add(mReadData.get(i));
                    // 50個追加された場合
                    if (a.size() >= 50) {
                        // 表示更新
                        publishProgress(a.toArray(new String[a.size()]));
                        a.clear();
                        // ビープ音鳴音
                        soundBeep();
                    }
                }
            }
            // 50個未満で表示更新が済んでないタグ情報がある場合
            if(!a.isEmpty()){
                // 表示更新
                publishProgress(a.toArray(new String[a.size()]));
                a.clear();
                // ビープ音鳴音
                soundBeep();
            }

            // 読取分のが表示更新が済んだので、クリア
            mReadData.clear();
            return null;
        }
    }
}
