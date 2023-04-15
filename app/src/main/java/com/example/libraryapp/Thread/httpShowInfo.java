package com.example.libraryapp.Thread;

import android.os.AsyncTask;
import android.util.Log;

import com.example.libraryapp.activities.resgister_product;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class httpShowInfo extends AsyncTask<String, Void, String> {

    public static int code;
    ;
    public Callable mCallable;

    public httpShowInfo(resgister_product resgister_product, Callable callable) {
        mCallable = callable;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            URL url = new URL(params[1]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("barcode", params[2]);


            OutputStream os = conn.getOutputStream();
            os.write(jsonParam.toString().getBytes("UTF-8"));
            os.flush();

            InputStream inputStream = new BufferedInputStream(conn.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            inputStream.close();
            return stringBuilder.toString();
        } catch (Exception e) {
            Log.e("MyApp", "Exception: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void onPostExecute(String response) {
        super.onPostExecute(response);
         try {
        if (response != null) {
            List<String> Mylist = new ArrayList<String>();
            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
            System.out.println(jsonObject);
            JsonObject resultObject = jsonObject.get("result").getAsJsonObject();
            if (resultObject.get("code").getAsInt() == 200) {
                String meg = resultObject.get("message").getAsString();
                Mylist.add(meg);
            } else {
                String info = "\t\t\t\t\t\t\t\t INFOMATION PRODUCT\n\n";
                Mylist.add(info);
                String rfid = "RFID: " + resultObject.get("RFID").getAsString() + "\n";
                Mylist.add(rfid);
                String pin = "PIN CODE: " + resultObject.get("Pin Code").getAsString() + "\n";
                Mylist.add(pin);
                String name = "Name Product: " + resultObject.get("Name Product").getAsString() + "\n";
                Mylist.add(name);
                String Categoty = "Product Category: " + resultObject.get("Product Category").getAsString() + "\n";
                Mylist.add(Categoty);
                String Resp = "Responsible: " + resultObject.get("Responsible").getAsString();
                Mylist.add(Resp);

            }
            mCallable.onCallback(Mylist.toString().replace(',', ' ').replace('[', ' ').replace(']', ' '));
        } else {
            mCallable.onCallback("CAN NOT SEARCH PLEASE TRY AGAIN!");
            }
        } catch (Exception e){
            System.out.println("MESS: "+e);
        }
    }
}