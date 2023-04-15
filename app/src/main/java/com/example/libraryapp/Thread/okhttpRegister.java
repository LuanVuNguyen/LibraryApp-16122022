package com.example.libraryapp.Thread;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.Gson;


// Dùng được nhưng bị lỗi You must either set a text or a view ? chưa tìm được lỗi
public class okhttpRegister extends AsyncTask<Void, Void, ApiResponse> {
    private static final String API_URL = "http://192.168.1.56:8069/controller/register_product";
    private Context context;
    private String requestBody;

    public  okhttpRegister(Context context, String requestBody) {
        this.context = context;
        this.requestBody = requestBody;
    }

    @Override
    protected ApiResponse doInBackground(Void... voids) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(requestBody);
            outputStream.flush();
            outputStream.close();
            int statusCode = connection.getResponseCode();
            String responseMessage = connection.getResponseMessage();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                inputStream.close();
                String jsonResponse = stringBuilder.toString();
                Gson gson = new Gson();
                ApiResponse apiResponse = gson.fromJson(jsonResponse, ApiResponse.class);
                return apiResponse;
            } else {
                throw new Exception("HTTP error code: " + statusCode + " " + responseMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(ApiResponse response) {
        if (response != null) {
            // Hiển thị thông báo trả về từ API
            Toast.makeText(context, response.Message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Error: Unable to connect to API", Toast.LENGTH_SHORT).show();
        }
    }
}