package com.teamsight.touchvision;

import android.content.Context;
import android.speech.tts.TextToSpeech;

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

    public Boolean setVoice(Locale locale){
        int result = mT2S.setLanguage(locale);
        if(TextToSpeech.LANG_MISSING_DATA == result || TextToSpeech.LANG_NOT_SUPPORTED == result){
            return false;
        }

        return true;
    }

    public synchronized void speakText(final String text, final boolean queueIfBusy){
        if(queueIfBusy || !mT2S.isSpeaking()) {
            mT2S.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }


}
