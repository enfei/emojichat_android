package com.mema.muslimkeyboard.sinchaudiocall;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.mema.muslimkeyboard.R;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CallScreenActivity extends BaseActivity {

    static final String TAG = CallScreenActivity.class.getSimpleName();
    @BindView(R.id.tv_speaker)
    TextView tvSpeaker;
    @BindView(R.id.tv_mic)
    TextView tvMic;
    @BindView(R.id.tv_videoCall)
    TextView tvVideoCall;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.callscreen);
        ButterKnife.bind(this);
        mAudioPlayer = new AudioPlayer(this);
        mCallDuration = (TextView) findViewById(R.id.callDuration);
        mCallerName = (TextView) findViewById(R.id.remoteUser);
        mCallState = (TextView) findViewById(R.id.callState);
        mUserprofile = (ImageView) findViewById(R.id.img_userprofile);
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
    public void onServiceConnected() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.addCallListener(new SinchCallListener());
//            mCallerName.setText(call.getRemoteUserId());
            if (call.getState().toString().equals("INITIATING"))
                mCallState.setText("Calling ...");
        } else {
            Log.e(TAG, "Started with invalid callId, aborting.");
            finish();
        }
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

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Log.d(TAG, "Call ended. Reason: " + cause.toString());
            mAudioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            String endMsg = "Call ended: " + call.getDetails().toString();
            Log.d(TAG, "onCallEnded() called with: endMsg = [" + endMsg + "]");
//            Toast.makeText(CallScreenActivity.this, endMsg, Toast.LENGTH_LONG).show();
            endCall();
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
            mAudioPlayer.stopProgressTone();
            if (call.getState().toString().equals("ESTABLISHED"))
                mCallState.setText("Voice Call from ...");
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
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
        
    }

    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            CallScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }
}
