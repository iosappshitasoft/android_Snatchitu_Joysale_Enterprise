package com.ni.addons.call;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ni.SnatchItUp.R;
import com.ni.utils.Constants;


public class CallControlsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = CallControlsFragment.class.getSimpleName();
    private RelativeLayout controlsLay, declineLay;
    private ImageView btnMute, btnDecline, btnSpeaker;
    private Context context;
    private OnCallEvents callEvents;
    private boolean isMicEnabled = true, enableSpeaker = false;
    private Animation downAnimation, upAnimation, fadeInAnimation;
    Bundle bundle;
    private String type;

    @Override
    public void onStart() {
        super.onStart();
        
        
        
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View controlView = inflater.inflate(R.layout.fragment_call_controls, container, false);
        bundle = getArguments();
        declineLay = controlView.findViewById(R.id.declineLay);
        controlsLay = controlView.findViewById(R.id.controlsLay);
        btnMute = controlView.findViewById(R.id.btnMute);
        btnDecline = controlView.findViewById(R.id.btnDecline);
        btnSpeaker = controlView.findViewById(R.id.btnSpeaker);

        if (bundle != null) {
            if (bundle.containsKey(Constants.TAG_FROM) && bundle.getString(Constants.TAG_FROM).equals(Constants.TAG_RECEIVE)) {
                controlsLay.setVisibility(View.GONE);
            }

            if (bundle.containsKey(Constants.TAG_AUDIO)) {
                enableSpeaker = bundle.getBoolean(Constants.TAG_AUDIO);
                setEnableSpeaker(enableSpeaker);
            }

            if (bundle.containsKey(Constants.TAG_CHAT_TYPE)) {
                type = bundle.getString(Constants.TAG_CHAT_TYPE);
                if (type.equals(Constants.TAG_VIDEO)) {
                    btnSpeaker.setImageResource(R.drawable.change_camera);
                    btnSpeaker.setBackground(getResources().getDrawable(R.drawable.white_round_opacity));
                }
            }
        }

        btnMute.setOnClickListener(this);
        btnDecline.setOnClickListener(this);
        btnSpeaker.setOnClickListener(this);

        upAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_up);
        downAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_down);
        fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in);
        return controlView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }


    public void setControlEvent(CallActivity activity) {
        callEvents = (OnCallEvents) activity;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Call control interface for container activity.
     */
    public interface OnCallEvents {

        void onCallHangUp();

        void onCameraSwitch();

        boolean onToggleMic(boolean isMute);

        boolean onToggleSpeaker();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnMute:
                if (isMicEnabled) {
                    isMicEnabled = false;
                    btnMute.setBackground(getResources().getDrawable(R.drawable.white_round_solid));
                    btnMute.setImageResource(R.drawable.mute_gray);
                } else {
                    isMicEnabled = true;
                    btnMute.setBackground(getResources().getDrawable(R.drawable.white_round_opacity));
                    btnMute.setImageResource(R.drawable.mute);
                }
                callEvents.onToggleMic(isMicEnabled);
                break;
            case R.id.btnSpeaker:
                if (type.equals(Constants.TAG_AUDIO)) {
                    enableSpeaker = callEvents.onToggleSpeaker();
                    setEnableSpeaker(enableSpeaker);
                } else {
                    callEvents.onCameraSwitch();
                }
                break;
            case R.id.btnDecline:
                callEvents.onCallHangUp();
                break;
        }
    }

    private void setEnableSpeaker(boolean enableSpeaker) {
        if (enableSpeaker) {
            btnSpeaker.setBackground(getResources().getDrawable(R.drawable.white_round_solid));
            btnSpeaker.setImageResource(R.drawable.speaker_gray);
        } else {
            btnSpeaker.setImageResource(R.drawable.speaker);
            btnSpeaker.setBackground(getResources().getDrawable(R.drawable.white_round_opacity));
        }
    }

    public void setCallUI() {
        controlsLay.setVisibility(View.VISIBLE);
    }
}
