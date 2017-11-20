package com.mema.muslimkeyboard.activity.profile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.bean.Dialog;
import com.mema.muslimkeyboard.bean.User;
import com.mema.muslimkeyboard.utility.BaseActivity;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.squareup.picasso.Picasso;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Super on 5/15/2017.
 */

public class ProfileActivity extends BaseActivity implements View.OnClickListener {
    CircleImageView imvAvatar;
    TextView tvFullname;
    TextView tvMobile;
    TextView tvLastSeen;
    String userID;
    private TextView tvRemoveFriend;
    private Dialog userDialog;
    private int position;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra("userID")) {
            finish();
            return;
        }
        userDialog = (Dialog) getIntent().getSerializableExtra("dialog");
        userID = intent.getStringExtra("userID");
        position = intent.getIntExtra("position", 0);
        initUI();
        getUserInfo();
    }

    @Override
    protected void initUI() {
        imvAvatar = (CircleImageView) findViewById(R.id.profile_image);
        tvFullname = (TextView) findViewById(R.id.tv_fullname);
        tvMobile = (TextView) findViewById(R.id.tv_mobile);
        tvLastSeen = (TextView) findViewById(R.id.tv_lastSeen);
        tvRemoveFriend = (TextView) findViewById(R.id.tv_removeFriend);
        tvRemoveFriend.setOnClickListener(this);
        findViewById(R.id.img_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tv_removeFriend:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Remove Friend")
                        .setMessage("Are you sure you want to block friend?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                FirebaseManager.getInstance().blockUser(userID);
                                // continue with delete
                               /* FirebaseManager.getInstance().removeDialog(userDialog); // removed chat history
                                FirebaseManager.getInstance().dialogList.remove(position);
                                FirebaseManager.getInstance().removeFriend(userID); // removed linked user from firebase database
                                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                finish()*/
                                ;
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                break;
        }
    }

    private void getUserInfo() {
        progressDialog.show();
        FirebaseManager.getInstance().getUser(userID, new FirebaseManager.OnUserResponseListener() {
            @Override
            public void onUserResponse(User user) {
                progressDialog.dismiss();
                if (user == null) {
                    finish();
                    return;
                }

                if (user.photo != null && user.photo.length() > 0) {
                    Picasso.with(ProfileActivity.this).load(user.photo).into(imvAvatar);
                }

                tvFullname.setText(user.username);
                tvMobile.setText(user.mobile);
                
                Object objAry[] = user.dialogs.values().toArray();
                long latestTime;
                if (objAry.length == 0) {
                    tvLastSeen.setText("");
                } else {
                    Map map = (Map) objAry[0];
                    if (!map.containsKey("lastSeenDate"))
                        return;
                    if (objAry.length > 1) {
                        latestTime = (long) map.get("lastSeenDate");
                        for (int i = 1; i < objAry.length; i++) {
                            Map tmpDia = (Map) objAry[i];
                            if (tmpDia.get("lastSeenDate") == null) {
                                continue;
                            }
                            long tmpTime = (long) tmpDia.get("lastSeenDate");
                            if (latestTime < tmpTime)
                                latestTime = tmpTime;
                        }
                    } else {
                        latestTime = (long) map.get("lastSeenDate");
                    }

                    tvLastSeen.setText(DateUtils.formatDateTime(ProfileActivity.this, latestTime, DateUtils.FORMAT_SHOW_TIME));
                }
            }
        });
    }
}
