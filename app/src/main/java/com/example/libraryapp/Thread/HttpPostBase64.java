package com.example.libraryapp.Thread;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.libraryapp.common.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpPostBase64 extends AsyncTask<String, String, String> {

    /**
     * Http response.
     */
    private HttpRfidResponse response;

    /**
     * Constructor HttpPost.
     */
    public HttpPostBase64(Context c) {
        this.response = (HttpRfidResponse) c;
    }

    /**
     * Set progress dialog loading.
     */
    protected void onPreExecute() {
    }

    /**
     * Send request and get response to services API.
     */
    @Override
    protected String doInBackground(String... params) {

        try {
            URL url = new URL(params[1]);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            //httpURLConnection.setConnectTimeout(3000);
            httpURLConnection.setRequestMethod(Config.METHOD_POST);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestProperty(Config.PROPERTY_KEY, Config.PROPERTY_VALUE);

            JSONObject ob = setParams(params);
            //System.out.println("Tommm"+ob.toString());

            OutputStream os = httpURLConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os,StandardCharsets.UTF_8));
            writer.write(ob.toString());
            writer.flush();
            writer.close();
            os.close();

            int responseCode = httpURLConnection.getResponseCode();
            //System.out.println("tommy3"+responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(httpURLConnection.getInputStream()));

                String line = in.readLine();
                System.out.println("tommy2"+line);
                return (line != null ? line : "");

            } else {
                return String.valueOf(responseCode);
            }
        } catch (Exception e) {
            return "Exception: " + e.getMessage();
        }

    }

    /**
     * End progress loading.
     */
/*
    @Override
    protected void onPostExecute(String result) {
        try{
            response.progressRfidFinish(result, 0, null);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }
*/

    /**
     * Set request params.
     */
    private JSONObject setParams(String... params) {

        //Log.d("LIST_RFID", params[2]);

        JSONObject jsonObject = new JSONObject();

        try {
            switch (params[0]) {
                case Config.CODE_LOGIN:
                    // #HUYNHQUANGVINH send list rfid
                    //jsonObject.put(Constants.COLUMN_RFID, new JSONArray(params[2]));
                    jsonObject.put("base64",  params[2]);
                    break;
                default:
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
