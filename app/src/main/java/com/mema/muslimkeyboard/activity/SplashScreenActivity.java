package com.mema.muslimkeyboard.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.utility.FontUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashScreenActivity extends AppCompatActivity {

    @BindView(R.id.loginLayout)
    LinearLayout loginLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        ButterKnife.bind(this);
        FontUtils.setFont(loginLayout, FontUtils.AvenirLTStdBook);
    }

    public void OnClickSignIn(View view) {
        startActivity(new Intent(SplashScreenActivity.this, SignInActivity.class));

    }

    public void OnClickSignUp(View view) {
        startActivity(new Intent(SplashScreenActivity.this, SignUpActivity.class));
    }
}
