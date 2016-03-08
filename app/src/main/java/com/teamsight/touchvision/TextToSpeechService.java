package com.teamsight.touchvision;

import android.content.Context;
import android.media.AudioAttributes;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;

import java.io.Serializable;
import java.util.Locale;

/**
 * Created by aldrichW on 15-11-22.
 */
public class TextToSpeechService {
    private Context mContext;
    private TextToSpeech.OnInitListener mListener;
    private TextToSpeech mT2S;

    public TextToSpeechService(Context applicationContext, TextToSpeech.OnInitListener listener){
        mContext = applicationContext;
        mListener = listener;
        if(mT2S == null){
            mT2S = new TextToSpeech(mContext, mListener);
        }
    }

    public void startService(){

    }

    public Boolean stopService() {
        if(mT2S != null){
            mT2S.shutdown();
            return true;
        }
        return false;
    }

    public Boolean setVoice(Locale locale){
        Voice voice = new Voice("default", locale, Voice.QUALITY_NORMAL, Voice.LATENCY_NORMAL, false, null);
        int result = mT2S.setVoice(voice);
        if(TextToSpeech.LANG_MISSING_DATA == result ||
                TextToSpeech.LANG_NOT_SUPPORTED == result){
            return false;
        }

        return true;
    }

    public synchronized void speakText(final String text){
        while(mT2S.isSpeaking()) {
            ;
        }
        mT2S.playSilentUtterance(500, TextToSpeech.QUEUE_FLUSH, "");
        mT2S.speak(text, TextToSpeech.QUEUE_ADD, null, "");
    }


}
