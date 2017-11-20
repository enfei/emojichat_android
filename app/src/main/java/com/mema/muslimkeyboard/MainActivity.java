package com.mema.muslimkeyboard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.mema.muslimkeyboard.activity.HomeActivity;
import com.mema.muslimkeyboard.activity.Welcome;


public class MainActivity extends AppCompatActivity {
    public static final String MyPREFERENCES = "MyPrefs";
    public static String backPressed = "";
    TextView txtNext;
    private int first = 0;
    private SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        txtNext = (TextView) findViewById(R.id.next);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String logout = sharedpreferences.getString("session", "l");
        if (logout.equals("login")) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }


        txtNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(MainActivity.this, Welcome.class);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (first != 0) {
            Intent i = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(i);
            first = 1;
        }
    }
}
