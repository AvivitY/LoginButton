package com.example.loginbutton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 22;
    private MaterialButton main_BTN;
    private TextInputEditText editText;
    private Dialog chargeDialog, micDialog;
    private TextView popup_text;
    private ImageView mic;
    private TextView mic_answer,mic_error,popup_error,login_error;
    ;
    private SensorManager mySensorManager;
    private Sensor lightSensor;
    private boolean isDark = false;
    private IntentFilter ifilter;
    private Intent batteryStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor != null) {
            mySensorManager.registerListener(
                    lightSensorListener,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        findViews();
    }

    private void findViews() {
        main_BTN = findViewById(R.id.main_BTN);
        editText = findViewById(R.id.editText);
        main_BTN.setOnClickListener(view -> login());
        login_error = findViewById(R.id.login_error);
    }

    private void login() {
        batteryStatus = MainActivity.this.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level * 100 / (float)scale;
        Log.d("ccc", "battery: " + (int)batteryPct);
        int minutes = Calendar.getInstance().get(Calendar.MINUTE);
        Log.d("ccc", "minutes: " + minutes);

        if(editText.getText().toString().equals(String.valueOf((int)batteryPct+minutes))) {
            login_error.setVisibility(View.GONE);
            chargeDialog = new Dialog(this);
            chargeDialog.setContentView(R.layout.popup_window);
            chargeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            MaterialButton check = chargeDialog.findViewById(R.id.popup_check);
            popup_error = chargeDialog.findViewById(R.id.popup_error);
            check.setOnClickListener(view -> checkCharging());
            chargeDialog.show();
        }else{
            login_error.setVisibility(View.VISIBLE);
        }
    }

    private void checkCharging() {
        batteryStatus = MainActivity.this.registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        Log.d("ccc","bat status "+ status);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
        if (isCharging) {
            popup_error.setVisibility(View.GONE);
            chargeDialog.dismiss();
            initMicPopUp();
        }else{
            popup_error.setVisibility(View.VISIBLE);
        }
    }

    private void initMicPopUp(){
        micDialog = new Dialog(this);
        micDialog.setContentView(R.layout.mic_window);
        micDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        MaterialButton check = micDialog.findViewById(R.id.mic_check);
        mic = micDialog.findViewById(R.id.mic);
        mic_answer = micDialog.findViewById(R.id.mic_answer);
        mic_error = micDialog.findViewById(R.id.mic_error);
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent
                        = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                        Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, " " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mic_answer.getText().equals("nine") || mic_answer.getText().equals("Nine")
                        || mic_answer.getText().equals("תשע")|| mic_answer.getText().equals("9")) {
                    mic_error.setVisibility(View.GONE);
                    micDialog.dismiss();
                    MaterialButton check = chargeDialog.findViewById(R.id.popup_check);
                    popup_text = chargeDialog.findViewById(R.id.popup_text);
                    popup_text.setText("Please turn off the lights");
                    check.setOnClickListener(view1 -> checkLighting());
                    chargeDialog.show();
                }else{
                    mic_error.setVisibility(View.VISIBLE);
                }
            }
        });
        micDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                mic_answer.setText(
                        Objects.requireNonNull(result).get(0));
            }
        }
    }

    private final SensorEventListener lightSensorListener
            = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
                if (sensorEvent.values[0] == 0) {
                    isDark = true;
                } else {
                    isDark = false;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    private void checkLighting() {
        Log.d("ccc", "is dark: " + isDark);
        if (isDark){
            popup_error.setVisibility(View.GONE);
            Intent login = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(login);
        }else{
            popup_error.setVisibility(View.VISIBLE);
        }
    }


}