package com.mema.muslimkeyboard.utility;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


/**
 * Created by cano-08 on 4/4/17.
 */

public abstract class BaseActivity extends AppCompatActivity {

    protected ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = DialogUtils.getProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    protected abstract void initUI();
}
