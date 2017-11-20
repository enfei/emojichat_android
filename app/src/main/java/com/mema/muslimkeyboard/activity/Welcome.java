package com.mema.muslimkeyboard.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.crashlytics.android.Crashlytics;
import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.utility.BaseActivity;
import com.mema.muslimkeyboard.utility.FontUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

/**
 * Created by cano-08 on 30/3/17.
 */

public class Welcome extends BaseActivity implements View.OnClickListener {
    LinearLayout linChat;
    private SharedPreferences sharedpreferences;
    private LinearLayout linEmoji;
    @BindView(R.id.privacyLayout)
    LinearLayout privacyLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);
        sharedpreferences = getSharedPreferences(HomeActivity.MyPREFERENCES, Context.MODE_PRIVATE);
        String logout = sharedpreferences.getString("session", "l");
        if (logout.equals("login")) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        initView();
    }

    @Override
    protected void initUI() {

    }

    private void initView() {
        Button btn_continue = (Button) findViewById(R.id.btn_continue);
        btn_continue.setOnClickListener(this);
        FontUtils.setFont(privacyLayout, FontUtils.AvenirLTStdBook);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_continue: {

                /*Intent goToLogin = new Intent(Welcome.this, SignIn.class);
                startActivity(goToLogin);*/

                sharedpreferences = getSharedPreferences(HomeActivity.MyPREFERENCES, Context.MODE_PRIVATE);
                String logout = sharedpreferences.getString("session", "l");
                if (logout.equals("login")) {
                    Intent intent = new Intent(this, HomeActivity.class);
                    startActivity(intent);
                } else {
                    Intent goToLogin = new Intent(Welcome.this, SplashScreenActivity.class);
                    startActivity(goToLogin);
                }
            }
            break;

//            case R.id.linEmoji: {
//
//                Intent intent = new Intent(this, com.dev.wangri.muslimkeyboard.SplashActivity.class);
//                startActivity(intent);
//            }

//            break;

            default:
        }
    }

    public void OnClickTerms(View view) {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.muslimemoji.com/terms"));
        startActivity(intent);
    }

    public void OnClickPrivacy(View view) {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.muslimemoji.com/privacy"));
        startActivity(intent);
    }

}
