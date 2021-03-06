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
    private TextView powerText, trimText;
    private String mac;
    private SharedPreferences sharedPref;
    private int powerBy255 = 0, trim;
    private EditText macEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SeekBar powerSlide = findViewById(R.id.powerSlide);
        SeekBar trimSlide = findViewById(R.id.trimSeek);
        Button save = findViewById(R.id.saveButton);
        Button reset = findViewById(R.id.resetButton);
        powerText = findViewById(R.id.buttonPower);
        trimText = findViewById(R.id.trimLevel);
        macEdit = findViewById(R.id.macAddEdit);

        sharedPref = getPreferences(MODE_PRIVATE);
        mac = sharedPref.getString(getString(R.string.shared_file), getString(R.string.MAC));
        Log.d("Current MAC", mac);
        macEdit.setText(mac);

        Intent intent = getIntent();
        int buttonPower = intent.getIntExtra("ButtonPower", 0);
        trim = intent.getIntExtra("trimLevel", 0);

        int percent = (int)(((float)buttonPower/255) * 100);
        Log.d("Button Power", Integer.toString(percent));
        powerSlide.setMax(100);
        powerSlide.setProgress(percent);
        powerText.setText(getString(R.string.buttonSliderText, percent));
        powerSlide.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                powerText.setText(getString(R.string.buttonSliderText, progress));
                powerBy255 = (int)(((float)progress/100)*255);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        trimSlide.setMax(255);
        trimSlide.setProgress(trim);
        trimText.setText(getString(R.string.trimSliderText, trim - 127));
        trimSlide.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                trimText.setText(getString(R.string.trimSliderText, progress-127));
                trim = progress;
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
                returnIn.putExtra("trimLevel", trim);
                returnIn.putExtra("power", powerBy255);
                returnIn.putExtra("changedMAC", changedMac);
                setResult(RESULT_OK, returnIn);
                finish();
            }
        });

    }
}