package com.teamsight.touchvision;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by aldrichW on 15-11-22.
 */
public class TextToSpeechService {
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

    public Boolean setVoice(Locale locale){
        int result = mT2S.setLanguage(locale);
        if(TextToSpeech.LANG_MISSING_DATA == result || TextToSpeech.LANG_NOT_SUPPORTED == result){
            return false;
        }

        return true;
    }

    public synchronized void speakText(final String text,
                                       final boolean queueIfBusy){
        if(queueIfBusy || !mT2S.isSpeaking()) {
            mT2S.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }

    public synchronized void speakText(final String text,
                                       final boolean queueIfBusy,
                                       final String utteranceID){
        if(queueIfBusy || !mT2S.isSpeaking()) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceID);

            mT2S.speak(text, TextToSpeech.QUEUE_ADD, params);
        }
    }


}
