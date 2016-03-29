package com.capstone.knockknock;

import android.hardware.SensorManager;


interface Callback{
    void knockEvent();
}


abstract public class KnockDetector implements Callback {

    /**
     * Manages starting, pausing and resuming the Spike Detector and Pattern Recognizer
     */

    protected String mOutputOneKnock;
    protected String mOutputTwoKnock;
    protected String mOutputThreeKnock;

    private boolean mIsActive;

    private SensorManager mParentSensorManager;

    private AccelSpikeDetector mAccelSpikeDetector;
    private PatternRecognizer mPatt = new PatternRecognizer(this);

    abstract protected void knockDetected(int knockCount);

    public KnockDetector(SensorManager parentSensorManager){
        mParentSensorManager = parentSensorManager;
        mIsActive = false;
    }

    public void init() {
        mAccelSpikeDetector = new AccelSpikeDetector(mParentSensorManager);
        assert(false == mIsActive);
        registerStrings(null, null, null);
    }

    public void pause() {
        if(mIsActive){
            mAccelSpikeDetector.stopAccSensing();
            mAccelSpikeDetector.unregisterCallback();
            mPatt.turnOff();

            mOutputOneKnock   = null;
            mOutputTwoKnock   = null;
            mOutputThreeKnock = null;

            mIsActive = false;
        }
    }

    public void resume() {
        if(!mIsActive){
            mIsActive = true;

            mPatt.turnOn();
            mAccelSpikeDetector.startAccSensing();
            mAccelSpikeDetector.registerCallback(this);
        }
    }

    public void registerStrings(final String oneKnock, final String twoKnock, final String threeKnock) {
        if(!mIsActive){
            mOutputOneKnock   = oneKnock;
            mOutputTwoKnock   = twoKnock;
            mOutputThreeKnock = threeKnock;
        }
    }

    public void knockEvent() {
        mPatt.knockEvent();
    }
}
