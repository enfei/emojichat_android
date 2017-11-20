package com.mema.muslimkeyboard.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.mema.muslimkeyboard.R;
import com.squareup.picasso.Picasso;

public class PhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        String photoURL = getIntent().getStringExtra("PhotoURL");

        ImageView ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
        Picasso.with(this).load(photoURL).into(ivPhoto);
    }
}
