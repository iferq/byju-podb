package com.edmobilelabs.tts.controller;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;

/**
 * Created by Rounak Khandeparkar on 2019-09-15.
 */
public class GoogleController implements TextToSpeech.OnInitListener {

    private static final String TAG = "GoogleController";

    public static final float SPEECH_RATE = 0.8f;
    public static final float SPEECH_PITCH = 1.0f;

    private TTSController.ResultListener mResultListener;

    private static final GoogleController INSTANCE = new GoogleController();

    public static final GoogleController getInstance() {
        return INSTANCE;
    }

    private GoogleController() {
    }

    private Context mContext;
    private TextToSpeech tts;

    public void init(Context context) {
        this.mContext = context;
        this.mResultListener = (TTSController.ResultListener) context;
        tts = new TextToSpeech(mContext, this);
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(new Locale("en", "IN"));
            tts.setSpeechRate(SPEECH_RATE);
            tts.setPitch(SPEECH_PITCH);
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    mResultListener.onSpeechStart();
                }

                @Override
                public void onDone(String utteranceId) {
                    mResultListener.onSpeechDone();
                }

                @Override
                public void onError(String utteranceId) {
                    mResultListener.onSpeechError(utteranceId);
                }
            });
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "This Language is not supported");
            }
        } else {
            Log.e(TAG, "Initialization Failed!");
        }
    }

    public void speakOut(String message) {
        if (tts != null) {
            Bundle params = new Bundle();
            // passing hard coded values so that UtteranceProgressListener works.
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, params, "UniqueID");
        }
    }


    public void release() {
        if (this.tts != null) {
            tts.shutdown();
        }
    }


    public void stop() {
        if (this.tts != null) {
            this.tts.stop();
        }
    }

}
