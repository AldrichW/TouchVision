package com.teamsight.touchvision;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by aldrichW on 15-11-22.
 */
public class TextToSpeechService {
    public static final boolean FLUSH_IF_BUSY = false;
    public static final boolean DROP_IF_BUSY  = true;
    public static final String ID_KNOCK_DETECTOR_RESUME = "id_knockDetectorResume";

    private int mNumActivePrompts;

    private Context mContext;
    private TextToSpeech mT2S;

    public TextToSpeechService(Context applicationContext, TextToSpeech.OnInitListener initListener){
        mContext = applicationContext;
        mNumActivePrompts = 0;
        if(mT2S == null){
            mT2S = new TextToSpeech(mContext, initListener);
        }
    }

    public Boolean startService() {
        return true;
    }

    public Boolean stopService() {
        if(mT2S != null){
            mT2S.shutdown();
            return true;
        }
        return false;
    }

    public void setProgressListener(UtteranceProgressListener progressListener){
        mT2S.setOnUtteranceProgressListener(progressListener);
    }

    public boolean setVoice(Locale locale){
        int result = mT2S.setLanguage(locale);
        if(TextToSpeech.LANG_MISSING_DATA == result || TextToSpeech.LANG_NOT_SUPPORTED == result){
            return false;
        }

        return true;
    }

    public boolean isLastDone() {
        mNumActivePrompts--;
        Log.d("TextToSpeechService", "Num Active Prompts: " + mNumActivePrompts);
        return (0 == mNumActivePrompts);
    }

    public synchronized void speakText(final String text,
                                       final boolean busyAction){
        if(busyAction == FLUSH_IF_BUSY || !mT2S.isSpeaking()) {
            mT2S.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
        else{   //add to the Text to Speech queue
            mT2S.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }

    public synchronized void speakText(final String text,
                                       final boolean busyAction,
                                       final String utteranceID){
        if(busyAction == FLUSH_IF_BUSY || !mT2S.isSpeaking()) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceID);

            if(utteranceID.equals(ID_KNOCK_DETECTOR_RESUME)) {
                mNumActivePrompts++;
                Log.d("TextToSpeechService", "Num Active Prompts: " + mNumActivePrompts);
            }

            mT2S.speak(text, TextToSpeech.QUEUE_FLUSH, params);
        }
    }


}
