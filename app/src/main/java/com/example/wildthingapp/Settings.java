package com.example.wildthingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {
    private TextView powerText;
    private String mac;
    private SharedPreferences sharedPref;
    private int powerBy255 = 0;
    private EditText macEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SeekBar powerSlide = findViewById(R.id.powerSlide);
        Button save = findViewById(R.id.saveButton);
        Button reset = findViewById(R.id.resetButton);
        powerText = findViewById(R.id.buttonPower);
        macEdit = findViewById(R.id.macAddEdit);

        sharedPref = getPreferences(MODE_PRIVATE);
        mac = sharedPref.getString(getString(R.string.shared_file), getString(R.string.MAC));
        Log.d("Current MAC", mac);
        macEdit.setText(mac);

        Intent intent = getIntent();
        int buttonPower = intent.getIntExtra("ButtonPower", 0);

        int percent = (int)(((float)buttonPower/255) * 100);
        Log.d("Button Power", Integer.toString(percent));
        powerSlide.setMax(100);
        powerSlide.setProgress(percent);
        powerText.setText("Button Power: " + percent);

        powerSlide.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        powerText.setText("Button Power: " + progress);
                    }
                });
                powerBy255 = (int)(((float)progress/100)*255);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                macEdit.setText(getString(R.string.MAC));
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = macEdit.getText().toString();
                boolean changedMac = !text.equals(mac);
                if(changedMac){
                    Log.d("Changing mac to", text);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.shared_file), text);
                    editor.apply();
                }
                Intent returnIn = new Intent();

                returnIn.putExtra("power", powerBy255);
                returnIn.putExtra("changedMAC", changedMac);
                setResult(RESULT_OK, returnIn);
                finish();
            }
        });

    }
}