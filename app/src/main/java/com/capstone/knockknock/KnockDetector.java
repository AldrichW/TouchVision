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

    public void init(final String oneKnock, final String twoKnock, final String threeKnock) {
        mAccelSpikeDetector = new AccelSpikeDetector(mParentSensorManager);
        assert(false == mIsActive);
        resume(oneKnock, twoKnock, threeKnock);
    }

    public void pause() {
        mAccelSpikeDetector.stopAccSensing();
        mAccelSpikeDetector.unregisterCallback();

        mOutputOneKnock   = null;
        mOutputTwoKnock   = null;
        mOutputThreeKnock = null;

        mIsActive = false;
    }

    public void resume(final String oneKnock, final String twoKnock, final String threeKnock) {
        if(!mIsActive){
            mAccelSpikeDetector.resumeAccSensing();
            mAccelSpikeDetector.registerCallback(this);

            mOutputOneKnock   = oneKnock;
            mOutputTwoKnock   = twoKnock;
            mOutputThreeKnock = threeKnock;

            mIsActive = true;
        }
    }

    public void knockEvent() {
        mPatt.knockEvent();
    }
}
