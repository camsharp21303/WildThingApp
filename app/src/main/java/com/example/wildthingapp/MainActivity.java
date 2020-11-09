package com.example.wildthingapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    Button connectButton, settings;
    Controller controller;
    BluetoothController bluetoothController;
    TextView conStatText, leftSpText, rightSpText;
    Context context;

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

        bluetoothController = new BluetoothController(getString(R.string.MAC), this);

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
                Intent intent = new Intent(context, Settings.class);
                startActivity(intent);
            }
        });

    }

    private void sendData(float[] a){
        float num1 = 0;
        float num2 = 1;

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
            float[] data = controller.processJoystickInput(event, -1);
            sendData(data);

            return true;
        }
        return super.onGenericMotionEvent(event);
    }
}
