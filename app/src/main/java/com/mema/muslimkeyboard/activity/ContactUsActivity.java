package com.mema.muslimkeyboard.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.utility.BaseActivity;

/**
 * Created by Super on 5/17/2017.
 */

public class ContactUsActivity extends BaseActivity implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactus);

        initUI();
    }

    @Override
    protected void initUI() {
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.imvEmail).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.imvEmail:
                sendEmail();
                break;
        }
    }

    private void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"Contact@MuslimEmoji.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Contact Us");
        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent, "Choose an Email client:"));
    }
}
