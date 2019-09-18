package com.edmobilelabs.tts.controller;

import android.content.Context;
import android.media.MediaPlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

/**
 * Created by Rounak Khandeparkar on 2019-09-17.
 */
public class MicrosoftCSController implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private final String TAG = "MicrosoftCSController";

    private static String speechSubscriptionKey = "63b39180c79642e6bc7136b3fed777b5";

    private static final String TTS_API_TOKEN_URL = "https://westus.api.cognitive.microsoft.com/sts/v1.0/issueToken";
    private static final String TTS_SERVICE_URL = "https://westus.tts.speech.microsoft.com/cognitiveservices/v1";

    private static MicrosoftCSController INSTANCE = new MicrosoftCSController();
    private Context context;

    private TTSController.ResultListener listener;

    private String authToken = "";
    private OkHttpClient client;
    private MediaPlayer mediaPlayer;
    private File tempMp3;

    public static MicrosoftCSController getInstance() {
        return INSTANCE;
    }

    private MicrosoftCSController() {
    }

    public void init(Context context) {
        this.context = context;
        listener = (TTSController.ResultListener) context;
    }


    public void speakOut(final String textToSpeak) {
        client = new OkHttpClient();

        final Request tokenRequest = new Request.Builder()
                .method("POST", new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        return null;
                    }

                    @Override
                    public void writeTo(BufferedSink bufferedSink) throws IOException {

                    }
                })
                .addHeader("Ocp-Apim-Subscription-Key", speechSubscriptionKey)
                .url(TTS_API_TOKEN_URL)
                .build();

        try {

            Call call = client.newCall(tokenRequest);

            call.enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    listener.onSpeechError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) {

                    try {
                        if (response.code() == 200) {
                            authToken = response.body().string();
                            handleTTS(textToSpeak);
                        } else {
                            listener.onSpeechError("Fetch token failed with code :" + response.code());
                        }

                    } catch (Exception e) {
                        listener.onSpeechError("Fetch token failed " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            listener.onSpeechError(e.getMessage());
        }
    }

    private void handleTTS(final String textToSpeak) {

        final String ssmlText = "<speak version='1.0' xml:lang='en-IN'>\n" +
                "    <voice xml:lang='en-IN' xml:gender='Female'\n" +
                "    name='en-IN-Heera-Apollo'>\n" +
                textToSpeak +
                "</voice>\n" +
                "</speak>";

        final Request ttsRequest = new Request.Builder()
                .method("POST", new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        return MediaType.get("application/ssml+xml");
                    }

                    @Override
                    public void writeTo(BufferedSink bufferedSink) throws IOException {
                        bufferedSink.writeString(ssmlText, Charset.forName("UTF-8"));
                    }
                })
                .addHeader("Authorization", "Bearer " + authToken)
                .addHeader("Content-Type", "application/ssml+xml")
                .addHeader("X-Microsoft-OutputFormat", "audio-24khz-48kbitrate-mono-mp3")
                .addHeader("User-Agent", "UB_TTS")
                .url(TTS_SERVICE_URL)
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

                            assert response.body() != null;
                            playMp3(response.body().bytes());

                        } else {
                            listener.onSpeechError("handleTTS with code :" + response.code());
                        }

                    } catch (Exception e) {
                        listener.onSpeechError("handleTTS failed " + e.getMessage());
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
            if (tempMp3 != null && tempMp3.exists()) {
                tempMp3.delete();
            }
        }

    }

    private void playMp3(byte[] mp3SoundByteArray) {
        try {
            tempMp3 = File.createTempFile("tts", "mp3", context.getCacheDir());
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            mediaPlayer = new MediaPlayer();

            FileInputStream fis = new FileInputStream(tempMp3);
            mediaPlayer.setDataSource(fis.getFD());
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
