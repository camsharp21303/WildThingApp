package com.example.wildthingapp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.io.Console;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private BluetoothAdapter btAdapter= null;
    Context context;
    String mac = "00:14:03:06:33:2B";
    Button connectButton, settings;
    Controller controller;
    int power1= 0, power2 = 1;

    private ProgressDialog progress = null;
    BluetoothSocket btSocket = null;
    private boolean isConnected = false;
    //static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    TextView conStatText, leftSpText, rightSpText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = (Button)findViewById(R.id.settings_button);
        connectButton = (Button)findViewById(R.id.connectButton);
        conStatText = (TextView)findViewById(R.id.connectStatsText);
        leftSpText = (TextView)findViewById(R.id.leftSpText);
        rightSpText = (TextView)findViewById(R.id.rightSpText);

        controller = new Controller(this);
        context = this;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new InitiateSocket().execute();
                if(isConnected){
                    try {
                        btSocket.close();
                        isConnected = false;
                        Log.d("Connection", "successfully disconnected btsocket");
                    }catch (IOException e){
                        Log.d("ERROR", "error disconnecting btSocket");
                    }

                    new InitiateSocket().execute();
                }
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Settings.class);
                startActivity(intent);
            }
        });

    }

    public void sendData(float a1, float a2){
        float num1 = 0;
        float num2 = 1;

        String num1String = a1*100 + "%";
        String num2String = a2*100 + "%";
        leftSpText.setText(num1String);
        rightSpText.setText(num2String);

        a1 = (a1)*60f;
        a2 = (a2)*60f;

        if(isConnected){
            if(a1 < 0){
               num1 = Math.abs(a1)+72;
            }
            else if(a1 > 0){
                num1 = a1+11;
            }

            if(a2 < 0){
                num2 = Math.abs(a2)+194;
            }
            else if(a2 > 0){
                num2 = a2+133;
            }
            power1 = (int)num1;
            power2 = (int)num2;

            Log.d("Formatted Numbers", "1:" + power1);
            Log.d("Formatted Numbers", "2:" + power2);
        }
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {

        // Check that the event came from a game controller
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) ==
                InputDevice.SOURCE_JOYSTICK &&
                event.getAction() == MotionEvent.ACTION_MOVE) {

            // Process all historical movement samples in the batch
            final int historySize = event.getHistorySize();

            // Process the movements starting from the
            // earliest historical position in the batch
            for (int i = 0; i < historySize; i++) {
                // Process the event at historical position i
                controller.processJoystickInput(event, i);
            }

            // Process the current movement sample in the batch (position -1)
            controller.processJoystickInput(event, -1);
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    private class InitiateSocket extends AsyncTask<Void, Void, Void> {

        boolean success = true;

        @Override
        protected void onPreExecute(){
            progress = progress.show(context, "Connecting...", "Please Wait");
        }
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                btSocket = null;
                if (!isConnected) {
                    BluetoothDevice device = btAdapter.getRemoteDevice(mac);
                    ParcelUuid[] pUUIDs = device.getUuids();

                    btSocket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(pUUIDs[0].toString()));
                    btAdapter.cancelDiscovery();
                    btSocket.connect();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            conStatText.setText("Connected");
                        }
                    });
                }
            }
            catch (Exception e){
                Log.d("ERROR", e.getMessage());
                success = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result){
            if(success){
                Log.d("Connection", "Success");
                isConnected = true;
            }
            else{
                Log.d("Connection", "failure");
                isConnected = false;
            }
            new dataStream().execute();
            progress.dismiss();
        }
    }

    private class dataStream extends AsyncTask<Void, Void, Void>{
        int errors = 0;
        @Override
        protected Void doInBackground(Void... voids) {
            while(btSocket.isConnected()) {
                try {
                    btSocket.getOutputStream().write(power1);
                    //Thread.sleep(50);
                    btSocket.getOutputStream().write(power2);
                    Thread.sleep(10);
                    errors = 0;
                } catch (Exception e) {
                    errors++;
                    Log.d("ERROR", "ERROR sending data " + errors);
                    if(errors > 20){
                        if(btSocket != null){
                            try {
                                btSocket.close();
                            }
                            catch (IOException a){
                                Log.e("ERROR", a.getMessage());
                            }
                        }
                    }
                }
            }
            isConnected = false;
            errors = 0;
            Log.d("ERROR", "Bluetooth not connected");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    conStatText.setText("disconnected");
                }
            });

            return null;
        }
    }


}
