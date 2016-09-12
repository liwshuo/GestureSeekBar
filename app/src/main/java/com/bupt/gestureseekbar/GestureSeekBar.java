package com.bupt.gestureseekbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * 可以响应手势操作的滚动条之老头滚动条
 * Created by lishuo on 16/6/16.
 */
public class GestureSeekBar extends SeekBar {

    public static final String TAG = GestureSeekBar.class.getSimpleName();
    public final static int SLOP = 20;

    /**
     * Factor
     */
    private float factor = 1f;

    /**
     * Constructors
     */
    public GestureSeekBar(Context context) {
        super(context);
        init();
    }

    public GestureSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GestureSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        init();
    }

    private void init() {
    }

    /**
     * Touch gesture
     */
    private OnSeekBarChangeListener onSlideSeekBarChangeListener;

    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener onSeekBarChangeListener) {
        this.onSlideSeekBarChangeListener = onSeekBarChangeListener;

        super.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (!b)
                    onSlideSeekBarChangeListener.onProgressChanged(seekBar, i, b);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private float mStartX;
    private float mStartY;
    private float mLastX;
    private float mLastY;
    private boolean mIsMotionSet = false;

    public enum ORIENTATION {VERTICAL, HORIZONTAL, NONE}

    private ORIENTATION mExpectedOrientation; //外部设定响应的手势方向
    private ORIENTATION mOrientation;  //内部判断实际的手势方向

    public enum DIRECTION {UP, DOWN, LEFT, RIGHT, NONE}

    public void setDirection(ORIENTATION direction) {
        mExpectedOrientation = direction;
    }

    public ORIENTATION getOrientation() {
        return mOrientation;
    }

    public float getFactor() {
        return factor;
    }

    public void setFactor(float factor) {
        this.factor = factor;
    }

    /**
     * @param motionEvent
     * @return 增加为true, 减少为false.如果当前的滑动方向不是设置的滑动方向, 则始终为true
     */
    public DIRECTION scrollWith(MotionEvent motionEvent) {
        DIRECTION direction = DIRECTION.NONE;
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = motionEvent.getX();
                mStartY = motionEvent.getY();
//                mLastX = mStartX;
//                mLastY = mStartY;
                mOrientation = ORIENTATION.NONE;
//                onSlideSeekBarChangeListener.onStartTrackingTouch(GestureSeekBar.this);
                break;
            case MotionEvent.ACTION_UP: {
                if (mExpectedOrientation == mOrientation) {
                    onSlideSeekBarChangeListener.onStopTrackingTouch(GestureSeekBar.this);
                }
                mIsMotionSet = false;
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                if (mExpectedOrientation == mOrientation) {
                    onSlideSeekBarChangeListener.onStopTrackingTouch(GestureSeekBar.this);
                }
                mIsMotionSet = false;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float curX = motionEvent.getX();
                float curY = motionEvent.getY();
                if (!mIsMotionSet) {
                    float distanceX = Math.abs(curX - mStartX);
                    float distanceY = Math.abs(curY - mStartY);
                    getOrientation(distanceX, distanceY);
                } else {
                    if (mExpectedOrientation != mOrientation) {
                        return DIRECTION.NONE;
                    }
                    int progress = getProgress(curX, curY);
                    if (progress == GestureSeekBar.this.getProgress())
                        return DIRECTION.NONE;
                    if (progress < 0) {
                        progress = 0;
                    }
                    if (progress > getMax()) {
                        progress = getMax();
                    }
                    setProgress(progress);
                    onSlideSeekBarChangeListener.onProgressChanged(GestureSeekBar.this, progress, true);
                }
                if (mExpectedOrientation != mOrientation) {
                    direction = DIRECTION.NONE;
                } else {
                    if (mOrientation == ORIENTATION.HORIZONTAL) {
                        if (curX > mLastX) {
                            direction = DIRECTION.RIGHT;
                        } else {
                            direction = DIRECTION.LEFT;
                        }
                    } else {
                        if (curY > mLastY) {
                            direction = DIRECTION.UP;
                        } else {
                            direction = DIRECTION.DOWN;
                        }
                    }
                }
                mLastX = curX;
                mLastY = curY;
                break;
            }
        }
        return direction;
    }

    private void getOrientation(float distanceX, float distanceY) {
        if (distanceX > SLOP) {
            mOrientation = ORIENTATION.HORIZONTAL;
            mIsMotionSet = true;
        } else if (distanceY > SLOP) {
            mOrientation = ORIENTATION.VERTICAL;
            mIsMotionSet = true;
        }
        if (mExpectedOrientation == mOrientation) {
            onSlideSeekBarChangeListener.onStartTrackingTouch(GestureSeekBar.this);
        }
    }

    private int getProgress(float curX, float curY) {
        float distance = 0;
        switch (mOrientation) {
            case HORIZONTAL:
                distance = curX - mLastX;
                break;
            case VERTICAL:
                distance = -(curY - mLastY);
                break;
        }
        float percentageMoved = distance / (float) (GestureSeekBar.this.getRight() - GestureSeekBar.this.getLeft());
        int progress = GestureSeekBar.this.getProgress() + (int) (percentageMoved * GestureSeekBar.this.getMax() * factor);
        return progress;
    }

}