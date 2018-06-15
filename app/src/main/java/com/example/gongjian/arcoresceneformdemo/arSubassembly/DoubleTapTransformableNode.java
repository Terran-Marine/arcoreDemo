package com.example.gongjian.arcoresceneformdemo.arSubassembly;

import android.view.MotionEvent;

import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;

public class DoubleTapTransformableNode extends TransformableNode {
    public DoubleTapTransformableNode(TransformationSystem transformationSystem) {
        super(transformationSystem);
    }

    private long DOUBLE_TAP_TIMEOUT = 200;

    public long getDOUBLE_TAP_TIMEOUT() {
        return DOUBLE_TAP_TIMEOUT;
    }

    public void setDOUBLE_TAP_TIMEOUT(long DOUBLE_TAP_TIMEOUT) {
        this.DOUBLE_TAP_TIMEOUT = DOUBLE_TAP_TIMEOUT;
    }

    private OnDoubleTapListener onDoubleTapListener;

    public OnDoubleTapListener getOnDoubleTapListener() {
        return onDoubleTapListener;
    }

    public void setOnDoubleTapListener(OnDoubleTapListener onDoubleTapListener) {
        this.onDoubleTapListener = onDoubleTapListener;
    }

    private MotionEvent mCurrentDownEvent;
    private MotionEvent mPreviousUpEvent;

    @Override
    public boolean onTouchEvent(HitTestResult hitTestResult, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            if (mPreviousUpEvent != null
                    && mCurrentDownEvent != null
                    && isConsideredDoubleTap(mCurrentDownEvent,
                    mPreviousUpEvent, motionEvent)) {

                if (onDoubleTapListener != null) {
                    onDoubleTapListener.onDoubleTap();
                }
                return true;
            }
            mCurrentDownEvent = MotionEvent.obtain(motionEvent);
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            mPreviousUpEvent = MotionEvent.obtain(motionEvent);
        }
        return super.onTouchEvent(hitTestResult, motionEvent);
    }

    private boolean isConsideredDoubleTap(MotionEvent firstDown,
                                          MotionEvent firstUp, MotionEvent secondDown) {
        if (secondDown.getEventTime() - firstUp.getEventTime() > DOUBLE_TAP_TIMEOUT) {
            return false;
        }
        int deltaX = (int) firstUp.getX() - (int) secondDown.getX();
        int deltaY = (int) firstUp.getY() - (int) secondDown.getY();
        return deltaX * deltaX + deltaY * deltaY < 10000;
    }

    interface OnDoubleTapListener {
        void onDoubleTap();
    }
}