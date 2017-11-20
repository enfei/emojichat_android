package com.mema.muslimkeyboard.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.activity.ContactUsActivity;
import com.mema.muslimkeyboard.activity.SignInActivity;
import com.mema.muslimkeyboard.activity.profile.CurrentUserProfileActivity;
import com.mema.muslimkeyboard.bean.User;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by cano-02 on 5/4/17.
 */

public class SettingFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = SettingFragment.class.getName();
    public static final String MyPREFERENCES = "MyPrefs";
    private static final int CURRENT_USER_PROFILE_ACTIVITY_REQUEST_CODE = 100;
    boolean bNotiEnable = true;
    View view;
    TextView tvName;
    CircleImageView imvAvatar;
    ImageView toggleNoti = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.setting_fragment, container, false);
        initView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        displaySettings();
    }

    private void initView() {
        view.findViewById(R.id.profileLayout).setOnClickListener(this);
        view.findViewById(R.id.emojiLayout).setOnClickListener(this);
        view.findViewById(R.id.privacyLayout).setOnClickListener(this);
        view.findViewById(R.id.contactUsLayout).setOnClickListener(this);
        view.findViewById(R.id.aboutUsLayout).setOnClickListener(this);
        view.findViewById(R.id.notificationLayout).setOnClickListener(this);
        view.findViewById(R.id.rateLayout).setOnClickListener(this);
        toggleNoti = (ImageView) view.findViewById(R.id.toggleNoti);
        imvAvatar = (CircleImageView) view.findViewById(R.id.profile_image);
        tvName = (TextView) view.findViewById(R.id.tv_name);
        SharedPreferences sharedpreferences = getActivity().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        tvName.setText(sharedpreferences.getString("personName", "username"));
    }

    private void displaySettings() {
        Uri photoUrl = FirebaseManager.getInstance().getCurrentUserPhotoUrl();
        if (photoUrl != null) {
            Picasso.with(getActivity()).load(photoUrl.toString()).into(imvAvatar);
        } else {
            imvAvatar.setImageResource(R.mipmap.profile);
        }

        getUserInfo();
    }

    private void getUserInfo() {
        FirebaseManager.getInstance().getUser(FirebaseManager.getInstance().getCurrentUserID(), new FirebaseManager.OnUserResponseListener() {
            @Override
            public void onUserResponse(User user) {
                if (user == null) {
                    return;
                }
                bNotiEnable = user.notification;
                if (bNotiEnable) {
                    toggleNoti.setImageResource(R.mipmap.switch_on);
                } else {
                    toggleNoti.setImageResource(R.mipmap.switch_off);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profileLayout:
                showProfile();
                break;
            case R.id.emojiLayout:
                showMuslimEmojis();
                break;
            case R.id.privacyLayout:
                showPrivacy();
                break;
            case R.id.aboutUsLayout:
                showAboutUs();
                break;
            case R.id.contactUsLayout:
                showContactUs();
                break;
            case R.id.rateLayout:
                rateUs();
                break;
            case R.id.notificationLayout:
                bNotiEnable = !bNotiEnable;
                if (bNotiEnable)
                    toggleNoti.setImageResource(R.mipmap.switch_on);
                else
                    toggleNoti.setImageResource(R.mipmap.switch_off);

                FirebaseManager.getInstance().updateNotificationSetting(bNotiEnable);
                break;
        }
    }

    private void rateUs() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CURRENT_USER_PROFILE_ACTIVITY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    getActivity().finish();

                    Intent i = new Intent(getActivity(), SignInActivity.class);
                    startActivity(i);
                }
                break;
        }
    }

    private void showProfile() {
        Intent i = new Intent(getActivity(), CurrentUserProfileActivity.class);
        startActivityForResult(i, CURRENT_USER_PROFILE_ACTIVITY_REQUEST_CODE);
    }

    private void showMuslimEmojis() {
//        Intent intent = new Intent(getActivity(), com.dev.wangri.muslimkeyboard.SplashActivity.class);
//        startActivity(intent);
    }

    private void showPrivacy() {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.muslimemoji.com/privacy"));
        startActivity(intent);
    }

    private void showAboutUs() {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.muslimemoji.com/about"));
        startActivity(intent);
    }

    private void showContactUs() {
        Intent intent = new Intent(getActivity(), ContactUsActivity.class);
        startActivity(intent);
    }
}
