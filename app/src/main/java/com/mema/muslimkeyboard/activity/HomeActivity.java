package com.mema.muslimkeyboard.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.bean.Dialog;
import com.mema.muslimkeyboard.fragment.ChatListFragment;
import com.mema.muslimkeyboard.fragment.ContactFragment;
import com.mema.muslimkeyboard.fragment.EmojiFragment;
import com.mema.muslimkeyboard.fragment.SettingFragment;
import com.mema.muslimkeyboard.sinchaudiocall.BaseActivity;
import com.mema.muslimkeyboard.sinchaudiocall.SinchService;
import com.mema.muslimkeyboard.utility.AdmobInterstitial;
import com.mema.muslimkeyboard.utility.AppConst;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.mema.muslimkeyboard.utility.Util;
import com.mema.muslimkeyboard.utility.models.FirebaseValueListener;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.appinvite.FirebaseAppInvite;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.sinch.android.rtc.SinchError;

import java.util.TimerTask;


public class HomeActivity extends BaseActivity implements SinchService.StartFailedListener {

    public static final int REQUEST_INVITE = 201;
    public static final String MyPREFERENCES = "MyPrefs";
    private static final String TAG = HomeActivity.class.getSimpleName();
    private static HomeActivity activityInstance = null;
    FirebaseAuth mAuth;
    Boolean bFirstTime = true;
    FirebaseValueListener friendRequestsListener = null, sentListener = null;
    FirebaseValueListener valueListener;
    TextView tvBadgeContact, tvBadgeChat;
    TimerTask hourlyTask;
    private TextView contacts_txtview, chats_txtview, emoji_txtview, setting_txtview;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private Context mContext;
    private SharedPreferences sharedpreferences;
    private AppConst appConst = new AppConst();
    private int viewType;
    private ContactFragment contactFragment;

    private String selectedTab = "";

    public static HomeActivity getInstance() {
        return activityInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        findViews();
        mContext = this;
        if (bFirstTime == true) {
            Util.getInstance().initEmoticanMap(this);
            init();
            AdmobInterstitial.loadInterstitial(this);
        }

        contactFragment = new ContactFragment();

        bFirstTime = false;
        activityInstance = this;

        Intent intent = getIntent();
        if (intent.hasExtra("VIEW")) {
            viewType = intent.getExtras().getInt("VIEW");
            if (viewType == 2) {
                loadContactFragment();
            }
        }
        // Check for App Invite invitations and launch deep-link activity if possible.
        // Requires that an Activity is registered in AndroidManifest.xml to handle
        // deep-link URLs.
        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData data) {
                        if (data == null) {
                            Log.d(TAG, "getInvitation: no data");
                            return;
                        }

                        // Get the deep link
                        Uri deepLink = data.getLink();
                        String deepLinkString = deepLink.toString();
                        String token = deepLinkString.substring(deepLinkString.indexOf("token=") + 6, deepLinkString.length());
                        Log.d(TAG, "onSuccess() called with: token = [" + token + "]");
                        // Extract invite
                        FirebaseAppInvite invite = FirebaseAppInvite.getInvitation(data);
                        if (invite != null) {
                            String invitationId = invite.getInvitationId();
                            Log.d(TAG, "onSuccess() invitationId " + invitationId);
                        }
                        if (!token.equals(FirebaseManager.getInstance().getCurrentUserID())) {
                            try {
                                FirebaseManager.getInstance().acceptFriendRequest(FirebaseManager.getInstance().getCurrentUserID(), token);
                            } catch (DatabaseException e) {
                                e.printStackTrace();
                            }
                        }
                        // Handle the deep link
                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hideSoftKeyboard();
        return true;
    }

    private void hideSoftKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                    INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to change color of home button when click Home Tab
     * return nothing
     */
    private void ShowCustomBottomContactColor() {

        contacts_txtview.setTextColor(getResources().getColor(R.color.green));
        chats_txtview.setTextColor(getResources().getColor(R.color.dark_gray));
        emoji_txtview.setTextColor(getResources().getColor(R.color.dark_gray));
        setting_txtview.setTextColor(getResources().getColor(R.color.dark_gray));

        contacts_txtview.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.unselected_contacts_tab, 0, 0);
        chats_txtview.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.chat_tab, 0, 0);
        emoji_txtview.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.emoji_tab, 0, 0);
        setting_txtview.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.setting_tab, 0, 0);

    }

    /**
     * Find the Views in the layout<br />
     * <br />
     */
    private void findViews() {
        contacts_txtview = (TextView) findViewById(R.id.txt_contacts);
        emoji_txtview = (TextView) findViewById(R.id.txt_emoji);
        setting_txtview = (TextView) findViewById(R.id.txt_setting);
        chats_txtview = (TextView) findViewById(R.id.txt_chats);
    }


    /**
     * This method is called when click Home Tab
     *
     * @param view
     */
    public void contactOnClick(View view) {
        loadContactFragment();
    }

    public void loadContactFragment() {
        hideSoftKeyboard();
        if (!selectedTab.equals(ContactFragment.TAG)) {
            selectedTab = ContactFragment.TAG;
            ShowCustomBottomContactColor();
            loadFragments(contactFragment, ContactFragment.TAG);
        }
    }

    /**
     * This method is called when Politica Tab clicked
     *
     * @param view
     */
    public void chatOnClick(View view) {
        hideSoftKeyboard();
        if (!selectedTab.equals(ChatListFragment.TAG)) {
            selectedTab = ChatListFragment.TAG;
            ShowCustomBottomChatColor();
            ChatListFragment fragment = new ChatListFragment();
            loadFragments(fragment, ChatListFragment.TAG);
        }
    }

    /**
     * This method is Used to change color of politica tab when clicked
     */
    private void ShowCustomBottomChatColor() {
        contacts_txtview.setTextColor(getResources().getColor(R.color.dark_gray));
        chats_txtview.setTextColor(getResources().getColor(R.color.green));
        emoji_txtview.setTextColor(getResources().getColor(R.color.dark_gray));
        setting_txtview.setTextColor(getResources().getColor(R.color.dark_gray));

        contacts_txtview.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.contact_tab, 0, 0);
        chats_txtview.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.unselected_chats_tab, 0, 0);
        emoji_txtview.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.emoji_tab, 0, 0);
        setting_txtview.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.setting_tab, 0, 0);

    }

    /**
     * This method is used to Load Fragments
     */
    private void loadFragments(Fragment fragment, String tag) {
        fragmentManager = this.getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.lytContent, fragment, tag);
        fragmentTransaction.commit();
    }

    public void removeFragment(Fragment fragment) {
        fragmentManager = this.getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(fragment);
        fragmentTransaction.commit();
    }

    /**
     * This method is called when click CDC Tab
     *
     * @param view
     */
    public void emojiOnClick(View view) {
        hideSoftKeyboard();
        if (!selectedTab.equals(EmojiFragment.TAG)) {
            selectedTab = EmojiFragment.TAG;
            ShowCustomEmojiChatColor();
            EmojiFragment fragment = new EmojiFragment();
            loadFragments(fragment, EmojiFragment.TAG);
        }
    }

    /**
     * This method is used to change color of Emoji Tab.
     */
    private void ShowCustomEmojiChatColor() {

        contacts_txtview.setTextColor(getResources().getColor(R.color.dark_gray));
        chats_txtview.setTextColor(getResources().getColor(R.color.dark_gray));
        emoji_txtview.setTextColor(getResources().getColor(R.color.green));
        setting_txtview.setTextColor(getResources().getColor(R.color.dark_gray));

        contacts_txtview.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.contact_tab, 0, 0);
        chats_txtview.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.chat_tab, 0, 0);
        emoji_txtview.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.unselected_emoji_tab, 0, 0);
        setting_txtview.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.setting_tab, 0, 0);
    }

    /**
     * This method is called when you clicked Briefing Tab.
     *
     * @param view
     */
    public void settingOnClick(View view) {
        hideSoftKeyboard();
        if (!selectedTab.equals(SettingFragment.TAG)) {
            selectedTab = SettingFragment.TAG;
            ShowCustomSettingChatColor();
            SettingFragment fragment = new SettingFragment();
            loadFragments(fragment, SettingFragment.TAG);
        }
    }

    /**
     * This method is called for change color of Setting when clicked Setting Tab.
     */
    private void ShowCustomSettingChatColor() {
        contacts_txtview.setTextColor(getResources().getColor(R.color.dark_gray));
        chats_txtview.setTextColor(getResources().getColor(R.color.dark_gray));
        emoji_txtview.setTextColor(getResources().getColor(R.color.dark_gray));
        setting_txtview.setTextColor(getResources().getColor(R.color.green));

        contacts_txtview.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.contact_tab, 0, 0);
        chats_txtview.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.chat_tab, 0, 0);
        emoji_txtview.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.emoji_tab, 0, 0);
        setting_txtview.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.unselected_setting_tab, 0, 0);
    }

    void init() {

        tvBadgeChat = (TextView) findViewById(R.id.tvChatBadge);
        tvBadgeChat.setVisibility(View.INVISIBLE);

        tvBadgeContact = (TextView) findViewById(R.id.tvContactBadge);
        tvBadgeContact.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.remove("session");
            editor.remove("user_email");
            editor.remove("user_pass");
            editor.apply();

            finish();
        }
        if (viewType != 2)
            chatOnClick(null);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        friendRequestsListener = FirebaseManager.getInstance().addRequestListener(new FirebaseManager.OnUpdateListener() {
            @Override
            public void onUpdate() {
                if (FirebaseManager.getInstance().requestList.size() == 0) {
                    tvBadgeContact.setVisibility(View.INVISIBLE);
                } else {
                    tvBadgeContact.setVisibility(View.VISIBLE);
                    tvBadgeContact.setText("" + FirebaseManager.getInstance().requestList.size());
                }
            }
        });

        valueListener = FirebaseManager.getInstance().addDialogListener(new FirebaseManager.OnUpdateListener() {
            @Override
            public void onUpdate() {
                Log.e("DialogList:", String.valueOf(FirebaseManager.getInstance().dialogList.size()));
                updateChatBadge();
            }
        });

        sentListener = FirebaseManager.getInstance().addSentListener(new FirebaseManager.OnUpdateListener() {
            @Override
            public void onUpdate() {
            }
        });

    }

    public void updateChatBadge() {
        int totalBadge = 0;
        for (int i = 0; i < FirebaseManager.getInstance().dialogList.size(); i++) {
            Dialog dia = FirebaseManager.getInstance().dialogList.get(i);
            if (FirebaseManager.getInstance().dialogBadges.containsKey(dia.dialogID)) {
                totalBadge += Integer.valueOf(FirebaseManager.getInstance().dialogBadges.get(dia.dialogID));
            }
        }

        Log.e("UpdateChatBadge Called:", String.valueOf(totalBadge));

        if (totalBadge == 0) {
            tvBadgeChat.setText("");
            tvBadgeChat.setVisibility(View.INVISIBLE);
        } else {
            tvBadgeChat.setText(String.valueOf(totalBadge));
            tvBadgeChat.setVisibility(View.VISIBLE);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (friendRequestsListener != null) {
            friendRequestsListener.removeListener();
        }

        if (sentListener != null) {
            sentListener.removeListener();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("AddUserActivity", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d("AddUserActivity", "onActivityResult: sent invitation " + id);
                }
            } else {
                Toast.makeText(activityInstance, "Invite canceled", Toast.LENGTH_SHORT).show();
                // Sending failed or it was canceled, show failure message to the user
                // ...
            }
        }
    }

    @Override
    protected void onServiceConnected() {
        Log.d(TAG, "onServiceConnected() called");
        getSinchServiceInterface().setStartListener(this);
        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(FirebaseManager.getInstance().getCurrentUserID());
        }
    }

    @Override
    public void onStartFailed(SinchError error) {
        Log.d(TAG, "onStartFailed() called with: error = [" + error + "]");
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStarted() {
        Log.d(TAG, "onStarted() called");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ContactFragment.REQUEST_PERMISSION_READ_CONTACT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (contactFragment != null && contactFragment.isAdded()) {
                        contactFragment.performSyncContacts(this);
                    }
                }
            default:
                break;
        }
    }

}

