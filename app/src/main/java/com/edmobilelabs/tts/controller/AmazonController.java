package com.edmobilelabs.tts.controller;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPollyPresigningClient;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.LanguageCode;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechPresignRequest;
import com.amazonaws.services.polly.model.Voice;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rounak Khandeparkar on 2019-09-16.
 */
public class AmazonController {

    private static final String TAG = "AmazonController";

    // TODO Pool id and regions value should be set in awsconfiguration.json as well
    private static final String COGNITO_POOL_ID = "us-west-2:a1c5b8cc-7515-4bd4-8431-3f3bef8f47d5";

    // Region of Amazon Polly.
    private static final Regions MY_REGION = Regions.US_WEST_2;

    private static final AmazonController INSTANCE = new AmazonController();

    private List<Voice> voices = new ArrayList<>();
    private MediaPlayer mediaPlayer;

    private TTSController.ResultListener mResultListener;

    public static final AmazonController getInstance() {
        return INSTANCE;
    }

    private AmazonController() {
    }

    private AmazonPollyPresigningClient client;


    public void init(Context context) {

        this.mResultListener = (TTSController.ResultListener) context;

        setupNewMediaPlayer();
        // Cognito pool ID. Pool needs to be unauthenticated pool with
        // Amazon Polly permissions.

        // Initialize the Amazon Cognito credentials provider.
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context.getApplicationContext(),
                COGNITO_POOL_ID,
                MY_REGION
        );

        AWSMobileClient.getInstance().initialize(context, new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                // Create a client that supports generation of presigned URLs.
                client = new AmazonPollyPresigningClient(AWSMobileClient.getInstance());
                Log.d(TAG, "onResult: Created polly pre-signing client");

                if (voices.isEmpty()) {
                    // Create describe voices request.
                    DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();

                    try {
                        // Synchronously ask the Polly Service to describe available TTS voices.
                        DescribeVoicesResult describeVoicesResult = client.describeVoices(describeVoicesRequest);

                        // Get list of voices from the result.
                        voices = describeVoicesResult.getVoices();

                        // Log a message with a list of available TTS voices.
                        Log.i(TAG, "Available Polly voices: " + voices);
                    } catch (RuntimeException e) {
                        Log.e(TAG, "Unable to get available voices.", e);
                        return;
                    }
                }

            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "onError: Initialization error", e);
            }
        });

    }

    public void speakOut(String textToRead) {

        if (voices == null || voices.isEmpty()) {
            mResultListener.onSpeechError("voices list empty");
            return;
        }

        // Create speech synthesis request.
        // TODO Hard coding language code and voice id.
        SynthesizeSpeechPresignRequest synthesizeSpeechPresignRequest =
                new SynthesizeSpeechPresignRequest()
                        // Set text to synthesize.
                        .withText(textToRead).withLanguageCode(LanguageCode.EnIN)
                        // Set voice selected by the user.
                        .withVoiceId(/*voices.get(0).getId()*/"Aditi")
                        // Set format to MP3.
                        .withOutputFormat(OutputFormat.Mp3);

        // Get the presigned URL for synthesized speech audio stream.
        URL presignedSynthesizeSpeechUrl =
                client.getPresignedSynthesizeSpeechUrl(synthesizeSpeechPresignRequest);

        Log.i(TAG, "Playing speech from presigned URL: " + presignedSynthesizeSpeechUrl);

        // Create a media player to play the synthesized audio stream.
        if (mediaPlayer.isPlaying()) {
            setupNewMediaPlayer();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            // Set media player's data source to previously obtained URL.
            mediaPlayer.setDataSource(presignedSynthesizeSpeechUrl.toString());
        } catch (IOException e) {
            Log.e(TAG, "Unable to set data source for the media player! " + e.getMessage());
        }

        // Start the playback asynchronously (since the data source is a network stream).
        mediaPlayer.prepareAsync();
    }

    void setupNewMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                mResultListener.onSpeechDone();
                setupNewMediaPlayer();
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mResultListener.onSpeechStart();
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mResultListener.onSpeechError("setupNewMediaPlayer: " + what);
                return false;
            }
        });
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            setupNewMediaPlayer();
        }
    }

}
