package com.edmobilelabs.tts.controller;

import android.content.Context;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Rounak Khandeparkar on 2019-09-17.
 */
public class RTWController implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private final String TAG = "RTWController";

    private static final String apiKey = "rounae5152c130f";

    // 0 - normal speed. The valid range is from -25 to 70.
    private static final int speed = 0;

    /* Valid voice names
        Michael - Male US English
        Lauren - Female US English
        Robbert - Male US English
        Tom - Male US English
        Frank - Male US English
        Julie - Female US English
        Emily - Female US English
        Allison - Female US English
        Charles - Male UK English
        Elizabeth - Female UK English
        Nina - Female Indian English
        Carlos - Male Spanish
        Maria- Female Spanish
        Phillipe- Male French
        Juliette - Female French
    * */

    private static final String voice = "Nina";

    // http://www.readthewords.com/api/get/apikey/voice/speed.aspx
    private static final String RTW_URL_FORMAT = "http://www.readthewords.com/api/get/%s/%s/%d.aspx";

    private static RTWController INSTANCE = new RTWController();
    private Context context;

    private TTSController.ResultListener listener;

    private OkHttpClient client;
    private MediaPlayer mediaPlayer;
    private String PARAMETER_TEXT = "text";

    public static RTWController getInstance() {
        return INSTANCE;
    }

    private RTWController() {
    }


    public void init(Context context) {
        this.context = context;
        listener = (TTSController.ResultListener) context;
    }


    public void speakOut(final String textToSpeak) {
        client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(PARAMETER_TEXT, textToSpeak)
                .build();

        final Request ttsRequest = new Request.Builder()
                .url(String.format(RTW_URL_FORMAT, apiKey, voice, speed))
                .post(requestBody)
                .build();

        try {

            Call call = client.newCall(ttsRequest);

            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    listener.onSpeechError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) {

                    try {
                        if (response.code() == 200) {
                            String respBody = response.body().string();
                            Log.d(TAG, "RTW Url " + respBody);

                            if (TextUtils.isEmpty(respBody)) {
                                listener.onSpeechError("speakOut failed url empty");
                                return;
                            } else if (respBody.contains("ERROR")) {
                                listener.onSpeechError("speakOut failed: " + respBody);
                                return;
                            }

                            try {
                                playMp3(respBody);

                            } catch (Exception e) {
                                listener.onSpeechError("speakOut failed " + e.getMessage());
                            }
                        } else {
                            listener.onSpeechError("speakOut failed with code :" + response.code());
                        }

                    } catch (Exception e) {
                        listener.onSpeechError("speakOut failed " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            listener.onSpeechError(e.getMessage());
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }

            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }

    }

    private void playMp3(String source) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(source);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepare();
        } catch (Exception ex) {
            listener.onSpeechError(ex.getMessage());
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        listener.onSpeechDone();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            listener.onSpeechStart();
        }
    }


}
