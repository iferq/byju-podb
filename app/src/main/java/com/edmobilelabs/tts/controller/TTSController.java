package com.edmobilelabs.tts.controller;

/**
 * Created by Rounak Khandeparkar on 2019-09-15.
 */
public class TTSController {

    public interface ResultListener {

        void onSpeechStart();

        void onSpeechDone();

        void onSpeechError(String error);

    }

}

