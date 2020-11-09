package com.example.wildthingapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.UUID;

public class BluetoothController {
    private BluetoothSocket btSocket;
    final private BluetoothAdapter btAdapter;
    final private String mac;
    private int power1 = 0, power2 = 1;
    Activity con;

    public BluetoothController(String mac, Activity con){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mac = mac;
        this.con = con;
    }

    public boolean isConnected(){
        return btSocket.isConnected();
    }

    public void setPower(int pow1, int pow2){
        power1 = pow1;
        power2 = pow2;
    }

    public void connect(){
        con.runOnUiThread(new updateText((TextView)con.findViewById(R.id.connectStatsText), "Connected"));
        con.runOnUiThread(new updateText((TextView)con.findViewById(R.id.connectButton), con.getString(R.string.Disconnect)));
        new Thread(new ConnectSocket()).start();
    }

    public void disconnect(){
        try {
            btSocket.close();
            con.runOnUiThread(new updateText((TextView)con.findViewById(R.id.connectStatsText), "Disconnected"));
            con.runOnUiThread(new updateText((TextView)con.findViewById(R.id.connectButton), con.getString(R.string.Connect)));
            Log.d("Connection", "successfully disconnected btsocket");
        }catch (IOException e){
            Log.d("ERROR", "error disconnecting btSocket");
        }
    }

    private static class updateText implements Runnable{
        String text;
        TextView view;

        updateText(TextView v,String text){
            this.text = text;
            view = v;
        }
        @Override
        public void run() {
            view.setText(text);
        }
    }

    private class ConnectSocket implements Runnable{
        boolean success = true;
        @Override
        public void run() {
            try {
                btSocket = null;
                BluetoothDevice device = btAdapter.getRemoteDevice(mac);
                btSocket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(device.getUuids()[0].toString()));
                btAdapter.cancelDiscovery();
                btSocket.connect();
            }
            catch (Exception e){
                success = false;
            }
            if(success){
                Log.d("Connection", "Success");
                new Thread(new powerStream()).start();
            }
            else{
                Log.d("Connection", "failure");
            }
        }
    }

    private class powerStream implements Runnable {
        int errors = 0;

        @Override
        public void run() {
            while (isConnected()) {
                try {
                    btSocket.getOutputStream().write(power1);
                    btSocket.getOutputStream().write(power2);
                    Thread.sleep(10);
                    errors = 0;
                } catch (Exception e) {
                    errors++;
                    Log.d("ERROR", "ERROR sending data " + errors);
                    if (errors > 20) {
                        if (btSocket != null) {
                            disconnect();
                        }
                    }
                }
            }
            errors = 0;
            Log.d("ERROR", "Bluetooth not connected");
        }
    }
}
