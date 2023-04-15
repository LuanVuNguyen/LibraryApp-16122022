package com.example.libraryapp.activities;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.libraryapp.R;
import com.example.libraryapp.Thread.ConnectThread;
import com.example.libraryapp.Thread.HttpPostIssue;
import com.example.libraryapp.Thread.HttpPostRfid;
import com.example.libraryapp.Thread.HttpRfidResponse;
import com.example.libraryapp.adapter.ListViewScanBookAdapter;
import com.example.libraryapp.common.Config;
import com.example.libraryapp.common.Constants;
import com.example.libraryapp.common.Message;
import com.example.libraryapp.common.entities.InforMemberEntity;
import com.example.libraryapp.common.entities.InforBookEntity;
import com.example.libraryapp.common.function.SupModRfidCommon;
import com.example.libraryapp.common.interfaces.Callable;
import com.example.libraryapp.database.SQLiteDatabaseHandler;
import com.example.libraryapp.fragment.DialogYesNoFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class IssueActivity extends AppCompatActivity implements HttpRfidResponse, View.OnClickListener {
    private int MODE_SCAN = 0;
    private int PAUSE_DEVICE = 0;
    private int IS_SHOW_DIALOG_LIMIT = 0;
    // SDK_ADD
    Set<String> setCustomInput = new HashSet<>();
    Set<String> setCustomOutput = new HashSet<>();
    private LinkedList<InforBookEntity> arrDataInList;
    private LinkedList<InforMemberEntity> arrMemberInList;
    private LinkedList<InforBookEntity> arrDataInListSum;
    private InforBookEntity inforBookEntity;
    private InforMemberEntity inforMemberEntity;
    private boolean isKeepScanMagazine = false;
    //toshoba
    private boolean isReadBackPress = false;
    private boolean mIsStartReadTags = false;
    private JSONArray jsonArraytoshiba=null;
    private JSONArray jsonArraytoshibamember=null;
    private ListView lvProduct;
    private TextView txt_Name,txt_Rfid,txt_MemberID,txt_Gender,txt_CurrentMembership,txt_Contact;
    private ImageView btn_delete,btn_issue,btn_back;
    private ImageView btn_startscanissue=null;
    private int scan_size;
    // #HUYNHQUANGVINH list rfid not found
    Set<String> setRfidNotFound = new HashSet<>();
    SQLiteDatabaseHandler db;
    List<HttpPostRfid> listHttp = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue);
        db = new SQLiteDatabaseHandler(this);

        initView();
        jsonArraytoshiba=new JSONArray();
        jsonArraytoshibamember=new JSONArray();
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
            btn_startscanissue.setImageResource(R.drawable.play);
            btn_startscanissue.setVisibility(View.VISIBLE);
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
                showToast("接続されたスキャナー!!!");
                dismissProgress();

            }
        }
    };
    ConnectThread connectThread = null;
    private void initDeviceScanVN(){
        //bluetoothDeviceConnect();
        connectThread = new ConnectThread();
        new Thread(new Runnable() {
            @Override
            public void run() {
                connectThread.connect(bluetoothDeviceConnected2(), IssueActivity.this, new Callable() {
                    @Override
                    public void call(boolean result) {
                        //Toast.makeText(MainActivity.this,"Starting",Toast.LENGTH_SHORT).show();
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
                    return device;
                }
                i++;
            }
        }
        return deviceTemp;

    }
    private boolean isShowProgress = false;
    /**
     * ライブラリアクセス中プログレス
     */
    private ProgressBar mProgressBar = null;
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
    private void showToast(String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(IssueActivity.this,s+"",Toast.LENGTH_LONG).show();
            }
        });
    }


    private Runnable mDissmissProgressRunnable = null;
    private Handler mDissmissProgressHandler = new Handler(Looper.getMainLooper());
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


    private void initView() {
        // Memberview
        txt_Name=(TextView) findViewById(R.id.txt_mbname);
        txt_Rfid=(TextView) findViewById(R.id.txt_mbrfid);
        txt_MemberID=(TextView) findViewById(R.id.txt_mbid);
        txt_Gender=(TextView) findViewById(R.id.txt_mbgender);
        txt_CurrentMembership=(TextView) findViewById(R.id.txt_mbcrmbship);
        txt_Contact=(TextView) findViewById(R.id.txt_mbcontact);
        //button
        btn_delete=(ImageView) findViewById(R.id.btn_delete);
        btn_issue=(ImageView) findViewById(R.id.btn_issue);
        btn_back=(ImageView) findViewById(R.id.btn_backissue);
        btn_startscanissue=(ImageView) findViewById(R.id.btn_startscanissue) ;
        btn_startscanissue.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        btn_issue.setOnClickListener(this);
        btn_back.setOnClickListener(this);
        inforBookEntity = new InforBookEntity();
        inforMemberEntity=new InforMemberEntity();
        arrDataInList = new LinkedList<>();
        arrMemberInList=new LinkedList<>();
        lvProduct=(ListView) findViewById(R.id.list_scan);

        reloadSQLiteData();

    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_delete:

                eventClickDeleteAll();
                break;
            case R.id.btn_backissue:
                eventClickBack();
                break;
            case R.id.btn_issue:
                eventClickIssue();
                break;
            case R.id.btn_startscanissue:
                startReadtag();
                break;
        }
    }
    //event
    private void eventDisableButton() {

        btn_back.setClickable(false);
        btn_issue.setClickable(false);
        btn_delete.setClickable(false);
    }
    private void eventEnableButton() {

        btn_back.setClickable(true);
        btn_issue.setClickable(true);
        //btnDelete.setClickable(true);
        btn_delete.setClickable(true);

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
    private void eventClickBack() {
        /*if (arrDataInList.size() > 0) {
            isKeepScanMagazine = checkOldBarcode();
        } else {
            isKeepScanMagazine = false;
        }*/
        if (arrDataInList.size() > 0) {
            // Stop scan honeywell

            // Disable event onClick
            eventDisableButton();
            //eventOpenButton(false);
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
                    actionDeleteAll();
                    //eventDisconnectDevice();
                    onBackPressed();
                }else{
                }
            }
        });
        loadFragment(dialogYesNoFragment);
    }
    /**
     * Function click delete all
     */
    private void eventClickDeleteAll() {
        //showDialogMessageConfirmExport();
            DialogYesNoFragment dialogYesNoFragment=new DialogYesNoFragment(this, "全て削除", Message.MESSAGE_CONFIRM_DELETE_ALL, new Callable() {
                @Override
                public void call(boolean result) {
                    if(result==true){
                        actionDeleteAll();
                        showToast("削除に成功しました！！！");
                    }else{
                        dismissProgress();
                    }
                }
            });
            loadFragment(dialogYesNoFragment);
        }
    private void eventClickIssue() {
        DialogYesNoFragment dialogYesNoFragment=new DialogYesNoFragment(this, "発行図書ですか？", Message.MESSAGE_CONFIRM_DELETE_ALL, new Callable() {
            @Override
            public void call(boolean result) {
                if(result==true){
                    issueBook();
                    actionDeleteAll();
                    showToast("発行に成功しました！！！");
                }else{
                    dismissProgress();
                }
            }
        });
        loadFragment(dialogYesNoFragment);

    }
    private void issueBook(){
        JSONArray jsonArrayissue=null;
        jsonArrayissue=new JSONArray();

        JSONArray jsonArrayissue1=null;
        jsonArrayissue1=new JSONArray();

        jsonArrayissue.put(inforMemberEntity.getRfid());
        db.insertAllProducts(arrDataInList);

        for(InforBookEntity p : db.getAllProducts()) {
            jsonArrayissue1.put(p.getRfidCode());
        }

        new HttpPostIssue(this).execute(Config.CODE_LOGIN,Config.HTTP_SERVER_SHOP+Config.API_ODOO_ISSUEBOOK, jsonArrayissue.toString(),jsonArrayissue1.toString());
    }
    private void updateDeleteMemberView() {
        txt_Name.setText("");
        txt_MemberID.setText("");
        txt_Rfid.setText("");
        txt_Gender.setText("");
        txt_CurrentMembership.setText("");
        txt_Contact.setText("");
    }
    private void reloadSQLiteData(){
        setCustomInput.clear();
        setCustomOutput.clear();
        //ADD SQLITE DATA
        for(InforBookEntity i : db.getAllProducts()){
            setCustomInput.add(i.getRfidCode());
            setCustomOutput.add(i.getRfidCode());
        }
    }
    @Override
    public void onDestroy() {
        if(Constants.CONFIG_DEVICE_NAME.equals(Constants.CONFIG_DEVICE_ATS100)) {
            connectThread.cancel();
        }
        else if (TecRfidSuite.OPOS_SUCCESS != mLib.stopReadTags(mStopReadTagsResultCallback)){
        }
        //eventDisconnectDevice();
        //btConnect.cancel();
        super.onDestroy();


    }
    private void actionDeleteAll() {
        if (!arrDataInList.isEmpty()) {
            setCustomInput.clear();
            setCustomOutput.clear();
            mReadData.clear();
            mShowReadData.clear();
            //reloadSQLiteData();
            inforMemberEntity = new InforMemberEntity();
            inforBookEntity = new InforBookEntity();
            jsonArraytoshiba=new JSONArray();
            jsonArraytoshibamember=new JSONArray();
            db.deleteAllProducts();

            updateDeleteMemberView();

            initListViewScreen();

        }else if(!arrMemberInList.isEmpty()){
            inforMemberEntity = new InforMemberEntity();
            updateDeleteMemberView();
        }
    }
    private void initListViewScreen() {

        // check array null
        arrDataInList = (LinkedList<InforBookEntity>) getLastCustomNonConfigurationInstance();
        arrMemberInList=(LinkedList<InforMemberEntity>) getLastCustomNonConfigurationInstance();
        if (arrDataInList == null) {
            arrDataInList = new LinkedList<>();
        }
        if (arrMemberInList==null){
            arrMemberInList=new LinkedList<>();
        }


        // Check if array is not null
        restartListView();

    }

    private void setMemberEntity(JSONObject obj){
        String name=null;
        String ID=null;
        String RFID=null;
        String gender=null;
        String contact=null;
        String membership=null;
        try {
            name = obj.getString("Name");
            ID = obj.getString("Member_ID");
            RFID = obj.getString("RFID");
/*            if(!obj.getString(Constants.KEY_TAX).equals("null"))
                bar2 = obj.getString(Constants.KEY_JANCODE_2);*/
            gender = obj.getString("Gender");
            membership = obj.getString("Current_membership");
            contact = obj.getString("Contact");


        } catch (JSONException e) {
            e.printStackTrace();
        }
        inforMemberEntity.setMember_ID(ID);
        inforMemberEntity.setCurrent_membership(membership);
        inforMemberEntity.setGender(gender);
        inforMemberEntity.setName(name);
        inforMemberEntity.setRfid(RFID);
        inforMemberEntity.setContact(contact);
        updateMemberView();


    }



    private void setDataEntity(JSONObject obj) {
        String isbn13= null;
        String categories= null;
        String rfid= null;
        String title = null;
        String author=null;
        int cost = 0;
        int tax = 0;
        try {
            isbn13 = obj.getString("ISBN_13");
            categories = obj.getString("Categories");
            author = obj.getString("Author");
/*            if(!obj.getString(Constants.KEY_TAX).equals("null"))
                bar2 = obj.getString(Constants.KEY_JANCODE_2);*/
            title = obj.getString("Book_title");
            rfid = obj.getString("RFID");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(isbn13);
        inforBookEntity.setIsbn13(isbn13);
        inforBookEntity.setCategories(categories);
        inforBookEntity.setAuthor(author);
        inforBookEntity.setBooktitle(title);
        inforBookEntity.setRfidCode(rfid);
        updateCurrentView();
        //processBarcode(bar1,cost);
/*        inforProductEntity.setBarcodeCD2(bar2);
        processBarcode(bar2);*/

    }
    private void updateCurrentView() {

        arrDataInList.add(0, inforBookEntity);
        inforBookEntity = new InforBookEntity();

        // Insert current record to database
        //checkAndInsert1RecordDatabase();

        restartListView();

    }
    /*    private void updateCurrentmemberView() {

            arrMemberInList.add(0, inforMemberEntity);
            inforMemberEntity = new InforMemberEntity();

            // Insert current record to database
            //checkAndInsert1RecordDatabase();

            restartListView();

        }*/
    private void updateMemberView() {

        txt_Name.setText(inforMemberEntity.getName());
        txt_MemberID.setText(inforMemberEntity.getMember_ID());
        txt_Rfid.setText(inforMemberEntity.getRfid());
        txt_Gender.setText(inforMemberEntity.getGender());
        txt_CurrentMembership.setText(inforMemberEntity.getCurrent_membership());
        txt_Contact.setText(inforMemberEntity.getContact());
        arrMemberInList.add(0, inforMemberEntity);
    }
    private void restartListView() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Update list view
                ListViewScanBookAdapter adapterBook = new ListViewScanBookAdapter(IssueActivity.this,
                        arrDataInList);
                lvProduct.setAdapter(adapterBook);
                // Show total number and price
                //callTotalNumberAndPrice();

                // Get size of data scan in list view
                scan_size = arrDataInList.size();
            }
        });

    }


    @Override
    public void progressRfidFinish(String output, int typeRequestApi, String fileName) {
        // KILL ALL HTTP
        if (output.contains("Exception")) {
            for (HttpPostRfid http : listHttp) {
                http.cancel(true);
            }
        }

        // System.out.println(output);
        try {
            Log.d("OUTPUT", output);
            System.out.println("KKKS: " + output);
            JSONObject jsonObject = new JSONObject(output);
            if (SupModRfidCommon.isStatusHttpOk(output)) {
                //if (jsonObject.getString(Constants.KEY_CODE).equals(Constants.VALUE_CODE_OK)) {
                if (jsonObject.getString(Constants.KEY_CODE).equals(Constants.VALUE_CODE_OK)) {
                    //setDataEntity(jsonObject.getJSONArray(Constants.KEY_DATA));
                    //JSONArray jArray = jsonObject.getJSONArray(Constants.KEY_DATA);
                    JSONObject obj = jsonObject.getJSONObject(Constants.KEY_DATA);
                    switch (obj.getString("Type")){
                        case "Member_Card":
                            //JSONArray jArray = obj.getJSONArray("content");
                            JSONObject obj2 = obj.getJSONObject("content");
                            String stringRfid= obj2.getString(Constants.KEY_RFID);
                            // #MARK_2
                            setMemberEntity(obj2);
                            break;
                        case "Product_Card":
                            //System.out.println("tommy1 co data:"+setCustomOutput);
                            JSONArray jArray = obj.getJSONArray("content");

                            for(int i= 0 ; i < jArray.length();i++) {
                                JSONObject obj3 = jArray.getJSONObject(i);
                                String stringRfid2= obj3.getString(Constants.KEY_RFID);

                                if(setCustomOutput.add(stringRfid2)){
                                    setDataEntity(obj3);
                                }
                            }
                            break;

                    }
/*                    for(int i= 0 ; i < jArray.length();i++){
                        JSONArray jArray1 = jArray.getJSONArray(0);
                        JSONObject obj = jArray.getJSONObject(i);
                        String stringRfid= obj.getString(Constants.KEY_RFID);*/
                    // #MARK_2
                        /*if(setCustomOutput.add(stringRfid)){
                            setDataEntity(obj);
                        }*/
/*                    for (int j=0;j<jArray.length();j++){
                        JSONObject obj2 = jArray.getJSONObject(j);
                        String stringRfid= obj2.getString(Constants.KEY_RFID);
                        String stringRfid1= obj2.getString("Type");
                        System.out.println(stringRfid1);
                        // #MARK_2
                        if (stringRfid1=="Member_Card"){
                            if(setCustomOutput.add(stringRfid)){

                                setMemberEntity(obj2);
                            }
                        }else{
                            if(setCustomOutput.add(stringRfid)){

                                setDataEntity(obj2);
                            }
                        }

                    }*/

                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
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
    private UpdateReadTagDataTask mUpdateReadTagDataTask = null;


    private static final TecRfidSuite mLib = TecRfidSuite.getInstance();
    private ArrayList<String> mReadData = new ArrayList<String>();
    private void startReadtag(){
        if (!mIsStartReadTags){
            btn_startscanissue.setImageResource(R.drawable.play_u);
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
            btn_startscanissue.setImageResource(R.drawable.play);
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
                mUpdateReadTagDataTask = new UpdateReadTagDataTask();
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
                        if (mReadData.get(i).contains("e000")&jsonArraytoshibamember.length()==0){
                            jsonArraytoshibamember.put(mReadData.get(i).toUpperCase());
                           postMember(jsonArraytoshibamember);
                        }
                        jsonArraytoshiba.put(mReadData.get(i).toUpperCase());
                        postBooks(jsonArraytoshiba);


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
    private void postBooks(JSONArray jsonArray){
        if (jsonArray.length() != 0) {
            Log.d("data_arr", jsonArray.toString());
            new HttpPostRfid(IssueActivity.this).execute(Config.CODE_LOGIN,Config.HTTP_SERVER_SHOP+Config.API_ODOO_GETBOOK, jsonArray.toString());
        }
    }
    private void postMember(JSONArray jsonArray){
        if (jsonArray.length()!=0){
            System.out.println("Tommycheckmeber"+jsonArraytoshibamember);
            new HttpPostRfid(IssueActivity.this).execute(Config.CODE_LOGIN,Config.HTTP_SERVER_SHOP+Config.API_ODOO_GETMEMBER, jsonArray.toString());
        }
    }
}