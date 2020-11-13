package com.example.wildthingapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {
    private SeekBar powerSlide;
    private TextView powerText;
    private Button save;
    private Context me;
    private int powerBy255 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        powerSlide = findViewById(R.id.powerSlide);
        powerText = findViewById(R.id.buttonPower);
        save = findViewById(R.id.saveButton);
        me = this;

        Intent intent = getIntent();
        int buttonPower = intent.getIntExtra("ButtonPower", 0);

        int percent = (int)(((float)buttonPower/255) * 100);
        Log.d("Button Power", Integer.toString(percent));
        powerSlide.setMax(100);
        powerSlide.setProgress(percent);
        powerText.setText(Integer.toString(percent));

        powerSlide.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        powerText.setText(Integer.toString(progress));
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

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIn = new Intent();

                returnIn.putExtra("power", powerBy255);
                setResult(RESULT_OK, returnIn);
                finish();
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
}