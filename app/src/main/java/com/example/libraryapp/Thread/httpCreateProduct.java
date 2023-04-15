package com.example.libraryapp.Thread;

import android.os.AsyncTask;
import android.util.Log;

import com.example.libraryapp.activities.create_new_product;
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

public class httpCreateProduct extends AsyncTask<String, Void, String> {

    public static int code;
    ;
    public Callable mCallable;

    public httpCreateProduct(create_new_product create_new_product, Callable callable) {
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
            jsonParam.put("responsible", params[2]);
            jsonParam.put("name_product", params[3]);
            jsonParam.put("barcode", params[4]);
            jsonParam.put("rfid", params[5]);

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
            Log.e("VULUAN", "Exception: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void onPostExecute(String response) {
        try {
            if (response!=null) {
                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                String message = "";
                JsonElement errorElement = jsonObject.get("error");
                if (errorElement != null && !errorElement.isJsonNull()) {
                    JsonObject errorObject = errorElement.getAsJsonObject();

                    JsonElement dataElement = errorObject.get("data");
                    JsonObject dataObject = dataElement.getAsJsonObject();

                    String meg = dataObject.get("message").getAsString();

                    message = meg;
                } else {
                    JsonObject resultObject = jsonObject.get("result").getAsJsonObject();
                    String meg = resultObject.get("message").getAsString();
                    message = meg;
                }
                mCallable.onCallback(message);
            }
            else {
                mCallable.onCallback("CREATE PRODUCT ERROR PLEASE TRY AGAIN");
            }
        } catch (Exception e) {
            Log.e("VULUANNN ", "Exception: " + e.getMessage());
        }
    }
}
