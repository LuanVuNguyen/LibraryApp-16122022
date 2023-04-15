package com.example.libraryapp.Thread;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import com.example.libraryapp.common.Config;
import com.example.libraryapp.common.interfaces.Callable;


import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ConnectThread extends Thread {
    Callable mCallable;
    UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static BluetoothSocket bTSocket;
    private HashSet<String> listTag = new HashSet<>();
    private static boolean isConnection = false;
    public static Thread thread;
    private static final int LENGTH_RFID = 8;
    private static final String SIGNAL_START_CHARACTER = "~As";
    Set<String> setCustom = new HashSet<>();
    Set<String> setCustommember = new HashSet<>();
    public boolean connect(BluetoothDevice bTDevice, Context mContext,Callable callable) {
        this.mCallable = callable;
        BluetoothSocket temp = null;
        try {
            temp = bTDevice.createRfcommSocketToServiceRecord(mUUID);
            bTSocket = temp;
        } catch (IOException e) {
            Log.d("CONNECTTHREAD", "Could not create RFCOMM socket:" + e.toString());
            return false;
        }
        try {
            bTSocket.connect();
            isConnection = true;
            if(isConnection == true){
                callable.call(true);
            }
            thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    while (isConnection) {
                        try {

                            // length buffer
                            byte[] buffer = new byte[2048];

                            // stop the process for 1 second to catch the signal from the user
                            try {
                                Thread.sleep(500);
                            } catch (Exception e) {
                            }

                            // receive input signal from bluetooth rfid scanner
                            InputStream inputStream = bTSocket.getInputStream();
                            //NEW PROCESS
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                            while(reader.ready()){
                                String line  = reader.readLine();
                                System.out.println("Luannnn "+line);
                                if(line.contains("~eT")) {
                                    if(line.contains("E000")){
                                        setCustommember.add(line.substring(7));
                                    }else{
                                        setCustom.add(line.substring(7));
                                    }

                                }
                            }
                            JSONArray jsonArray = null;
                            JSONArray jsonArray1=null;

                            if(!setCustommember.isEmpty()) {
                                try {
                                    jsonArray1=new JSONArray();

                                    for (String i:setCustommember){
                                        if (jsonArray1.length()==0){
                                            jsonArray1.put(i);
                                        }
                                    }
                                    if (jsonArray1.length() != 0) {
                                        new HttpPostRfid(mContext).execute(Config.CODE_LOGIN,Config.HTTP_SERVER_SHOP+Config.API_ODOO_GETMEMBER, jsonArray1.toString());

                                    }
                                    if(!setCustom.isEmpty()){
                                        jsonArray = new JSONArray();
                                        for (String i : setCustom) {
                                            jsonArray.put(i);
                                        }
                                        postBooks(mContext,jsonArray);
                                        setCustom.clear();
                                        setCustommember.clear();
                                    }
                                    setCustom.clear();
                                    setCustommember.clear();
                                    // bug1

                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }else{
                                setCustom.clear();
                            }


                        } catch (IOException e) {
                            isConnection = false;
                            System.out.println("Error read data: " + e.toString());
                        }
                    }
                }
            });
            thread.start();
        } catch (IOException e) {
            Log.d("CONNECTTHREAD", "Could not connect: " + e.toString());
            try {
                bTSocket.close();
            } catch (IOException close) {
                Log.d("Try close socket", "Could not close connection:" + e.toString());
                return false;
            }
        }
        return true;
    }

    public boolean cancel() {
        try {
            if(isConnection==true){
                isConnection = false;
                bTSocket.close();
            }
        } catch (IOException e) {
            Log.d("CONNECTTHREAD", "Could not close connection:" + e.toString());
            return false;
        }
        return true;
    }

    private int getIndexStart(String buffer){
       int index = buffer.indexOf("~As");
       return index;
    }
    private int getIndexEnd(String buffer){
        int index = buffer.indexOf("~Af");
        return index;
    }
    private void postBooks(Context mContext,JSONArray jsonArray){
        if (jsonArray.length() != 0) {
            Log.d("data_arr luannnnnn: ", jsonArray.toString());
            new HttpPostRfid(mContext).execute(Config.CODE_LOGIN,Config.HTTP_SERVER_SHOP+Config.API_ODOO_GETBOOK, jsonArray.toString());
        }
    }

}