package com.edmobilelabs.tts.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.edmobilelabs.tts.R;
import com.edmobilelabs.tts.Utils.ViewUtils;
import com.edmobilelabs.tts.controller.AmazonController;
import com.edmobilelabs.tts.controller.GoogleController;
import com.edmobilelabs.tts.controller.MicrosoftCSController;
import com.edmobilelabs.tts.controller.TTSController;
import com.edmobilelabs.tts.controller.WatsonController;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, TTSController.ResultListener {

    private static final String TAG = "MainActivity";

    private ToggleButton btnGoogle, btnWatson;
    private ToggleButton btnPolly, btnMicrosoft;
    private AppCompatEditText etTextToRead;
    private GridLayout glServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        init();
    }

    private void initView() {
        glServices = findViewById(R.id.glServices);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnPolly = findViewById(R.id.btnPolly);
        btnWatson = findViewById(R.id.btnWatson);
        btnMicrosoft = findViewById(R.id.btnMicrosoft);
        etTextToRead = findViewById(R.id.etTextToRead);

        btnGoogle.setOnCheckedChangeListener(this);
        btnPolly.setOnCheckedChangeListener(this);
        btnWatson.setOnCheckedChangeListener(this);
        btnMicrosoft.setOnCheckedChangeListener(this);

    }

    private void init() {
        GoogleController.getInstance().init(this);
        AmazonController.getInstance().init(this);
        WatsonController.getInstance().init(this);
        MicrosoftCSController.getInstance().init(this);
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
            ViewUtils.toggleButtonCheckedStatusExcept(glServices, buttonView.getId(), false);
            switch (buttonView.getId()) {
                case R.id.btnGoogle:
                    GoogleController.getInstance().speakOut(textToSpeak);
                    AmazonController.getInstance().stop();
                    WatsonController.getInstance().stop();
                    MicrosoftCSController.getInstance().stop();
                    break;
                case R.id.btnPolly:
                    AmazonController.getInstance().speakOut(textToSpeak);
                    GoogleController.getInstance().stop();
                    WatsonController.getInstance().stop();
                    MicrosoftCSController.getInstance().stop();
                    break;
                case R.id.btnWatson:
                    WatsonController.getInstance().speakOut(textToSpeak);
                    GoogleController.getInstance().stop();
                    AmazonController.getInstance().stop();
                    MicrosoftCSController.getInstance().stop();
                    break;
                case R.id.btnMicrosoft:
                    MicrosoftCSController.getInstance().speakOut(textToSpeak);
                    WatsonController.getInstance().stop();
                    GoogleController.getInstance().stop();
                    AmazonController.getInstance().stop();
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
                case R.id.btnWatson:
                    WatsonController.getInstance().stop();
                    break;
                case R.id.btnMicrosoft:
                    MicrosoftCSController.getInstance().stop();
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
                ViewUtils.toggleButtonCheckedStatus(glServices, false);
            }
        });
    }

    @Override
    public void onSpeechError(final String error) {
        Log.e(TAG, "onSpeechError " + error);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "onSpeechError " + error, Toast.LENGTH_SHORT).show();
                ViewUtils.toggleButtonCheckedStatus(glServices, false);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GoogleController.getInstance().release();
    }

}

