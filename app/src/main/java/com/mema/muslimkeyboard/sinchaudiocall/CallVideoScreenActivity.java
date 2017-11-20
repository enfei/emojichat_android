package com.mema.muslimkeyboard.sinchaudiocall;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mema.muslimkeyboard.R;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallState;
import com.sinch.android.rtc.video.VideoCallListener;
import com.sinch.android.rtc.video.VideoController;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CallVideoScreenActivity extends BaseActivity implements View.OnTouchListener {

    static final String TAG = CallVideoScreenActivity.class.getSimpleName();
    static final String ADDED_LISTENER = "addedListener";

    @BindView(R.id.tv_speaker)
    TextView tvSpeaker;
    @BindView(R.id.tv_mic)
    TextView tvMic;
    @BindView(R.id.tv_videoCall)
    TextView tvVideoCall;

    private boolean mAddedListener = false;
    private boolean mLocalVideoViewAdded = false;
    private boolean mRemoteVideoViewAdded = false;

    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;

    private String mCallId;

    private TextView mCallDuration;
    private TextView mCallState;
    private TextView mCallerName;
    private ImageView mUserprofile;
    private String mcaller;
    private String mPhotoUrl;
    private AudioManager audioManager;
    private int callType;
    private LinearLayout layoutMenuButtons;
    private VideoController vc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.callvideoscreen);
        ButterKnife.bind(this);
        mAudioPlayer = new AudioPlayer(this);
        mCallDuration = (TextView) findViewById(R.id.callDuration);
        mCallerName = (TextView) findViewById(R.id.remoteUser);
        mCallState = (TextView) findViewById(R.id.callState);
        mUserprofile = (ImageView) findViewById(R.id.img_userprofile);
        layoutMenuButtons = (LinearLayout) findViewById(R.id.layout_menu_buttons);
        ImageView endCallButton = (ImageView) findViewById(R.id.hangupButton);

        endCallButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                endCall();
            }
        });
        mCallId = getIntent().getStringExtra(SinchService.CALL_ID);
        mcaller = getIntent().getStringExtra(SinchService.CALLER_NAME);
        mPhotoUrl = getIntent().getStringExtra(SinchService.CALLER_PHOTO);
        if (!TextUtils.isEmpty(mPhotoUrl)) {
            Picasso.with(this).load(mPhotoUrl)
                    .error(R.mipmap.profile)
                    .placeholder(R.mipmap.profile)
                    .into(mUserprofile);
        }
        mCallerName.setText(mcaller);
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);

    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(ADDED_LISTENER, mAddedListener);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mAddedListener = savedInstanceState.getBoolean(ADDED_LISTENER);
    }

    @Override
    public void onServiceConnected() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.addCallListener(new SinchCallListener());
        } else {
            Log.e(TAG, "Started with invalid callId, aborting.");
            finish();
        }
        updateUI();
    }

    private void updateUI() {
        if (getSinchServiceInterface() == null) {
            return; // early
        }

        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            if (call.getState().toString().equals("INITIATING"))
                mCallState.setText("Calling ...");
            if (call.getDetails().isVideoOffered()) {
                addLocalView();
                if (call.getState() == CallState.ESTABLISHED) {
                    addRemoteView();
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mDurationTask.cancel();
        mTimer.cancel();
        removeVideoViews();
    }

    @Override
    public void onStart() {
        super.onStart();
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 500);
        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        mDurationTask.cancel();
        mTimer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 500);
    }

    @Override
    public void onBackPressed() {
        // User should exit activity by ending call, not by going back.
    }

    private void endCall() {
        mAudioPlayer.stopProgressTone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    private String formatTimespan(int totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updateCallDuration() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            mCallDuration.setText(formatTimespan(call.getDetails().getDuration()));
        }
    }

    private void addLocalView() {
        if (mLocalVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            RelativeLayout localView = (RelativeLayout) findViewById(R.id.localVideo);
            localView.addView(vc.getLocalView());
            localView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            mLocalVideoViewAdded = true;
        }
    }

    private void addRemoteView() {
        if (mRemoteVideoViewAdded || getSinchServiceInterface() == null) {
            return; //early
        }
        final VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            RelativeLayout view = (RelativeLayout) findViewById(R.id.remoteVideo);
            view.setOnTouchListener(this);
            view.addView(vc.getRemoteView());
            mRemoteVideoViewAdded = true;
        }
    }


    private void removeVideoViews() {
        if (getSinchServiceInterface() == null) {
            return; // early
        }

        VideoController vc = getSinchServiceInterface().getVideoController();
        if (vc != null) {
            RelativeLayout view = (RelativeLayout) findViewById(R.id.remoteVideo);
            view.removeView(vc.getRemoteView());

            RelativeLayout localView = (RelativeLayout) findViewById(R.id.localVideo);
            localView.removeView(vc.getLocalView());
            mLocalVideoViewAdded = false;
            mRemoteVideoViewAdded = false;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.remoteVideo) {
            if (!layoutMenuButtons.isShown()) {
                layoutMenuButtons.setVisibility(View.VISIBLE);
            }
            hindLayoutMenuButtons();
        }
        return false;
    }

    private class SinchCallListener implements VideoCallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString());
            mAudioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            String endMsg = "Call ended: " + call.getDetails().toString();
            Log.d(TAG, "onCallEnded() called with: endMsg = [" + endMsg + "]");
//            Toast.makeText(CallVideoScreenActivity.this, endMsg, Toast.LENGTH_LONG).show();

            endCall();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
            mAudioPlayer.stopProgressTone();
            if (call.getState().toString().equals("ESTABLISHED"))
                mCallState.setText("Video Call from ...");
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            AudioController audioController = getSinchServiceInterface().getAudioController();
            audioController.enableSpeaker();
            Log.d(TAG, "Call offered video: " + call.getDetails().isVideoOffered());
            hindLayoutMenuButtons();
        }

        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
            mAudioPlayer.playProgressTone();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // Send a push through your push provider here, e.g. GCM
        }

        @Override
        public void onVideoTrackAdded(Call call) {
            Log.d(TAG, "Video track added");
            addRemoteView();
        }

        @Override
        public void onVideoTrackPaused(Call call) {

        }

        @Override
        public void onVideoTrackResumed(Call call) {

        }
    }

    private void hindLayoutMenuButtons() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (layoutMenuButtons.isShown()) {
                            layoutMenuButtons.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        };
        thread.start(); //start the thread
    }

    public void onClickSpeaker(View view) {
        if (audioManager != null) {
            if (audioManager.isSpeakerphoneOn()) {
                audioManager.setSpeakerphoneOn(false);
                tvSpeaker.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_speaker_loud_off, 0, 0);
            } else {
                audioManager.setSpeakerphoneOn(true);
                tvSpeaker.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_speaker_loud, 0, 0);
            }

        }
    }

    public void onClickMiceOff(View view) {
        if (audioManager != null) {
            if (audioManager.isMicrophoneMute()) {
                audioManager.setMicrophoneMute(false);
                tvMic.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_mic_off, 0, 0);
            } else {
                audioManager.setMicrophoneMute(true);
                tvMic.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_mic_on, 0, 0);
            }
        }
    }

    public void onClickVideoCall(View view) {
        vc.toggleCaptureDevicePosition();
    }

    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            CallVideoScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }
}
