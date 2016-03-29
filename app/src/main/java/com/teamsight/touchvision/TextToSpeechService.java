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

    private Context mContext;
    private TextToSpeech mT2S;

    public TextToSpeechService(Context applicationContext, TextToSpeech.OnInitListener initListener){
        mContext = applicationContext;
        if(mT2S == null){
            mT2S = new TextToSpeech(mContext, initListener);
        }
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

    public void interruptSpeech() {
        mT2S.stop();
    }

    public synchronized void speakText(final String text,
                                       final boolean busyAction){
        if(busyAction == FLUSH_IF_BUSY || !mT2S.isSpeaking()) {
            mT2S.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

}
