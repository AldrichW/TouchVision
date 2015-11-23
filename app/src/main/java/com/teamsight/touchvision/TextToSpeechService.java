package com.teamsight.touchvision;

import android.content.Context;
import android.speech.tts.TextToSpeech;

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
    }

    public TextToSpeech startService(){
        if(mT2S == null){
            mT2S = new TextToSpeech(mContext, mListener);
        }
        return mT2S;
    }

    public Boolean stopService() {
        if(mT2S != null){
            mT2S.shutdown();
            return true;
        }
        return false;
    }


}
