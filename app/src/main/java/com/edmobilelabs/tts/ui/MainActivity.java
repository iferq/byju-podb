package com.edmobilelabs.tts.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.edmobilelabs.tts.R;
import com.edmobilelabs.tts.controller.AmazonController;
import com.edmobilelabs.tts.controller.GoogleController;
import com.edmobilelabs.tts.controller.TTSController;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, TTSController.ResultListener {

    private static final String TAG = "MainActivity";

    private ToggleButton btnGoogle;
    private ToggleButton btnPolly;
    private AppCompatEditText etTextToRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        init();
    }

    private void initView() {
        btnGoogle = findViewById(R.id.btnGoogle);
        btnPolly = findViewById(R.id.btnPolly);
        etTextToRead = findViewById(R.id.etTextToRead);

        btnGoogle.setOnCheckedChangeListener(this);
        btnPolly.setOnCheckedChangeListener(this);

    }

    private void init() {
        GoogleController.getInstance().init(this);
        AmazonController.getInstance().init(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (TextUtils.isEmpty(etTextToRead.getText())) {
            Toast.makeText(this, "Text cannot be empty", Toast.LENGTH_SHORT).show();
            buttonView.setChecked(false);
            return;
        }

        String textToSpeak = etTextToRead.getText().toString();

        if (isChecked) {

            switch (buttonView.getId()) {

                case R.id.btnGoogle:
                    GoogleController.getInstance().speakOut(textToSpeak);
                    AmazonController.getInstance().stop();
                    btnPolly.setChecked(false);
                    break;
                case R.id.btnPolly:
                    AmazonController.getInstance().speakOut(textToSpeak);
                    GoogleController.getInstance().stop();
                    btnGoogle.setChecked(false);
                    break;
            }
        } else {

            switch (buttonView.getId()) {
                case R.id.btnGoogle:
                    GoogleController.getInstance().stop();
                    break;
                case R.id.btnPolly:
                    AmazonController.getInstance().stop();
                    break;
            }
        }


    }


    @Override
    public void onSpeechStart() {
        Log.d(TAG, "onSpeechStart");
    }

    @Override
    public void onSpeechDone() {
        Log.d(TAG, "onSpeechDone");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnGoogle.setChecked(false);
                btnPolly.setChecked(false);
            }
        });
    }

    @Override
    public void onSpeechError(String error) {
        Log.e(TAG, "onSpeechError " + error);
        Toast.makeText(this, "onSpeechError " + error, Toast.LENGTH_SHORT).show();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnGoogle.setChecked(false);
                btnPolly.setChecked(false);

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GoogleController.getInstance().release();
    }

}

