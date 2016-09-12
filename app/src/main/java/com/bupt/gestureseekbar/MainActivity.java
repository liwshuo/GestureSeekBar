package com.bupt.gestureseekbar;

import android.content.Context;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private GestureSeekBar mForwardSeekBar;
    private GestureSeekBar mVolumeSeekBar;
    private RelativeLayout mForwardContainer;
    private RelativeLayout mVolumeContainer;
    private TextView mForwardTime;
    private TextView mForwardDuration;
    private ImageView mForwardImage;
    private AudioManager mAudioManager;

    private long mDuration = 100000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSeekBar();
    }

    private void initSeekBar() {
        mForwardContainer = (RelativeLayout) findViewById(R.id.video_forward_container);
        mForwardContainer.setVisibility(View.GONE);
        mForwardSeekBar = (GestureSeekBar) findViewById(R.id.video_forward_seek_bar);
        mForwardTime = (TextView) findViewById(R.id.video_forward_time);
        mForwardDuration = (TextView) findViewById(R.id.video_forward_duration);
        mForwardImage = (ImageView) findViewById(R.id.video_forward_image);
        mVolumeContainer = (RelativeLayout) findViewById(R.id.video_volume_container);
        mVolumeContainer.setVisibility(View.GONE);
        mVolumeSeekBar = (GestureSeekBar) findViewById(R.id.video_volume_seek_bar);
        mForwardSeekBar.setFactor(0.5f);
        mForwardSeekBar.setMax(1000);
        mForwardSeekBar.setDirection(GestureSeekBar.ORIENTATION.HORIZONTAL);
        mForwardSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mForwardSeekBar.getMax() > 0) {
                    int position = (int) (Math.abs(progress) * mDuration / mForwardSeekBar.getMax());
                    mForwardTime.setText(getFormatTime(position / 1000));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                doFadeInAnimation(mForwardContainer, 300, null);
                mForwardDuration.setText(getFormatTime(mDuration / 1000));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                doFadeOutAnimation(mForwardContainer, 300, null);
            }
        });
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mVolumeSeekBar.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        mVolumeSeekBar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        mVolumeSeekBar.setFactor(0.5f);
        mVolumeSeekBar.setDirection(GestureSeekBar.ORIENTATION.VERTICAL);
        mVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                doFadeInAnimation(mVolumeContainer, 300, null);
                mVolumeSeekBar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                doFadeOutAnimation(mVolumeContainer, 300, null);
            }
        });
    }

    private String getFormatTime(long time) {
        long seconds = time % 60;
        long minutes = time / 60;
        if (minutes < 60) {
            return String.format("%02d:%02d", minutes, seconds);
        } else {
            long hours = minutes / 60;
            minutes = minutes % 60;
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
    }

    private void doFadeInAnimation(View view, int duration, Animation.AnimationListener animationListener) {
        if (view.getVisibility() == View.VISIBLE) {
            return;
        }
        view.setVisibility(View.VISIBLE);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        if (duration >= 0) {
            anim.setDuration(duration);
        }
        if (animationListener != null) {
            anim.setAnimationListener(animationListener);
        }
        view.startAnimation(anim);
    }

    private void doFadeOutAnimation(View view, int duration, Animation.AnimationListener animationListener) {
        if (view.getVisibility() == View.GONE) {
            return;
        }
        view.setVisibility(View.GONE);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        if (duration >= 0) {
            anim.setDuration(duration);
        }
        if (animationListener != null) {
            anim.setAnimationListener(animationListener);
        }
        view.startAnimation(anim);
    }

    GestureSeekBar.DIRECTION mPreForwardDirection = GestureSeekBar.DIRECTION.NONE;
    private float startY = 0;
    private float startX = 0;

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            startX = motionEvent.getX();
            startY = motionEvent.getY();
        }
        //防止在全屏时滑出状态栏时触发调节声音操作,以及滑出导航栏时触发调节快进操作
        if (startX < getScreenHeight() - 50 && startY > 100) {
            GestureSeekBar.DIRECTION forwardDirection = mForwardSeekBar.scrollWith(motionEvent);
            if (forwardDirection != mPreForwardDirection) {
                if (forwardDirection == GestureSeekBar.DIRECTION.RIGHT) {
                    mForwardImage.setImageResource(R.drawable.video_forward_big);
                } else if (forwardDirection == GestureSeekBar.DIRECTION.LEFT) {
                    mForwardImage.setImageResource(R.drawable.video_backward_big);
                }
            }
            mPreForwardDirection = forwardDirection;
            mVolumeSeekBar.scrollWith(motionEvent);
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    private int getScreenHeight() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int screenHeight;
        if (Build.VERSION.SDK_INT >= 13) {
            Point screenSize = new Point();
            display.getSize(screenSize);
            screenHeight = screenSize.y;
        } else {
            screenHeight = display.getWidth();
        }
        return screenHeight;
    }

}
