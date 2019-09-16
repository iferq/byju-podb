package com.edmobilelabs.tts.controller;

import android.content.Context;
import android.os.AsyncTask;

import com.ibm.watson.developer_cloud.android.library.audio.StreamPlayer;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.SynthesizeOptions;

/**
 * Created by Rounak Khandeparkar on 2019-09-16.
 */
public class WatsonController {

    private static final WatsonController INSTANCE = new WatsonController();

    private static final String TTS_API_KEY = "YRI-nkis6z7YEU1vDiYiq0jde6hTfLd6gW0EDUu3Ht_x";
    private static final String TTS_ENDPOINT = "https://gateway-wdc.watsonplatform.net/text-to-speech/api";
    private SynthesisTask synthesisTask;

    public static final WatsonController getInstance() {
        return INSTANCE;
    }

    TextToSpeech textToSpeech;
    Context mContext;
    private StreamPlayer player;
    private TTSController.ResultListener mListener;

    private WatsonController() {
    }

    public void init(Context context) {
        mContext = context;
        mListener = (TTSController.ResultListener) context;
        textToSpeech = initTextToSpeechService();
        player = new StreamPlayer();
    }

    private class SynthesisTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            if (isCancelled()) {
                mListener.onSpeechError("doInBackground cancelled");
                return "Did not synthesize";
            }

            SynthesizeOptions synthesizeOptions = new SynthesizeOptions.Builder()
                    .text(params[0])
                    .voice(SynthesizeOptions.Voice.EN_US_ALLISONVOICE)
                    .accept(SynthesizeOptions.Accept.AUDIO_WAV)
                    .build();
            player.playStream(textToSpeech.synthesize(synthesizeOptions).execute());
            mListener.onSpeechStart();
            return "Did synthesize";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mListener.onSpeechDone();
        }
    }

    private TextToSpeech initTextToSpeechService() {
        IamOptions options = new IamOptions.Builder()
                .apiKey(TTS_API_KEY)
                .build();
        TextToSpeech service = new TextToSpeech(options);
        service.setEndPoint(TTS_ENDPOINT);
        return service;
    }

    public void speakOut(String textToSpeak) {
        if (synthesisTask != null && synthesisTask.getStatus() == AsyncTask.Status.RUNNING) {
            synthesisTask.cancel(true);
        }

        synthesisTask = new SynthesisTask();
        synthesisTask.execute(textToSpeak);
    }

    public void stop() {

        if (player != null) {
            player.interrupt();
        }

        if (synthesisTask != null && synthesisTask.getStatus() == AsyncTask.Status.RUNNING) {
            synthesisTask.cancel(true);
            synthesisTask = null;
        }
    }


}
