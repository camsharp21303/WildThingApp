package com.example.wildthingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    Button connectButton, settings;
    Controller controller;
    BluetoothController bluetoothController = null;
    TextView conStatText, leftSpText, rightSpText;
    Switch buttonControl;
    Context context;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        settings = findViewById(R.id.settings_button);
        connectButton = findViewById(R.id.connectButton);
        conStatText = findViewById(R.id.connectStatsText);
        leftSpText = findViewById(R.id.leftSpText);
        rightSpText = findViewById(R.id.rightSpText);
        buttonControl = findViewById(R.id.buttonControl);

        sharedPref = this.getPreferences(MODE_PRIVATE);
        String mac = sharedPref.getString(getString(R.string.shared_file), getString(R.string.MAC));

        bluetoothController = new BluetoothController(mac, this);

        controller = new Controller();
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(bluetoothController.isConnected()){
                bluetoothController.disconnect();
            }
            else{
                bluetoothController.connect();
            }
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothController.isConnected()) {
                    Log.d("SETTINGS", "IS Connected");
                    bluetoothController.stopSending();
                    bluetoothController.sendData(5);
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int buttonPercent = 0;
                        int trim = 0;
                        while (bluetoothController.isConnected()) {
                            try {
                                if (bluetoothController.getInputStream().available() > 1) {
                                    buttonPercent = bluetoothController.getInputStream().read();
                                    trim = bluetoothController.getInputStream().read();
                                    break;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        Log.d("Button Percent", Integer.toString(buttonPercent));
                        Intent intent = new Intent(context, Settings.class);
                        intent.putExtra("ButtonPower", buttonPercent);
                        intent.putExtra("trimLevel", trim);
                        startActivityForResult(intent, 1);
                    }
                }).start();
            }
        });
        buttonControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(bluetoothController.isConnected()) {
                    if (buttonControl.isChecked()) bluetoothController.stopSending();
                    else bluetoothController.startSending();
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            boolean changed = data.getBooleanExtra("changedMAC", false);
            int power = data.getIntExtra("power", 0);
            int trim = data.getIntExtra("trimLevel", 0);
            Log.d("power", Integer.toString(power));
            if(bluetoothController.isConnected()){
                if (power != 0) {
                    bluetoothController.sendData(4);
                    bluetoothController.sendData(power);
                    bluetoothController.sendData(6);
                    bluetoothController.sendData(trim);
                }
                if(changed) {
                    bluetoothController.disconnect();
                    bluetoothController.setMAC(sharedPref.getString(getString(R.string.shared_file), getString(R.string.MAC)));
                    bluetoothController.connect();
                }
                bluetoothController.startSending();
            }
        }

    }

    private void sendData(double[] a){
        double num1 = 0;
        double num2 = 1;

        String num1String = a[0]*100 + "%";
        String num2String = a[1]*100 + "%";
        leftSpText.setText(num1String);
        rightSpText.setText(num2String);

        a[0] = (a[0])*60f;
        a[1] = (a[1])*60f;

        if(a[0] < 0){
           num1 = Math.abs(a[0])+72;
        }
        else if(a[0] > 0){
            num1 = a[0]+11;
        }

        if(a[1] < 0){
            num2 = Math.abs(a[1])+194;
        }
        else if(a[1] > 0){
            num2 = a[1]+133;
        }

        bluetoothController.setPower((int)num1, (int)num2);

        Log.d("Formatted Numbers", "1:" + (int)num1);
        Log.d("Formatted Numbers", "2:" + (int)num2);
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
            double[] data = controller.processJoystickInput(event, -1);
            sendData(data);

            return true;
        }
        return super.onGenericMotionEvent(event);
    }
}
