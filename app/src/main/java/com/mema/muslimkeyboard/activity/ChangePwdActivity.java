package com.mema.muslimkeyboard.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.utility.BaseActivity;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePwdActivity extends BaseActivity {

    EditText etCurrentPwd, etNewPwd, etConfPwd;
    RelativeLayout layoutDone;
    View maskView;
    SharedPreferences sharedpreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pwd);

        etCurrentPwd = (EditText) findViewById(R.id.et_currentpwd);
        etNewPwd = (EditText) findViewById(R.id.et_newpwd);
        etConfPwd = (EditText) findViewById(R.id.et_confirmpwd);

        layoutDone = (RelativeLayout) findViewById(R.id.doneOverlayLayout);

        maskView = (View) findViewById(R.id.maskView);
        maskView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutDone.setVisibility(View.GONE);
                enableEditTextViews(true);
            }
        });

        sharedpreferences = getSharedPreferences(SignIn.MyPREFERENCES, Context.MODE_PRIVATE);

        findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strCurPwd = etCurrentPwd.getText().toString();
                final String strNewPwd = etNewPwd.getText().toString();
                String strConfPwd = etConfPwd.getText().toString();

                if (strCurPwd.isEmpty()) {
                    etCurrentPwd.setError("Enter current Password");
                    etCurrentPwd.requestFocus();
                    return;
                } else if (strNewPwd.isEmpty()) {
                    etNewPwd.setError("Enter New Password");
                    etNewPwd.requestFocus();
                    return;
                } else if (strConfPwd.isEmpty()) {
                    etConfPwd.setError("Enter Confirm Password");
                    etConfPwd.requestFocus();
                    return;
                } else if (strCurPwd.length() < 8) {
                    etCurrentPwd.setError("Password length can not be less than 8 character");
                    etCurrentPwd.requestFocus();
                    return;
                } else if (strNewPwd.length() < 8) {
                    etNewPwd.setError("Password length can not be less than 8 character");
                    etNewPwd.requestFocus();
                    return;
                } else if (strConfPwd.length() < 8) {
                    etConfPwd.setError("Password length can not be less than 8 character");
                    etConfPwd.requestFocus();
                    return;
                } else if (!strConfPwd.equals(strNewPwd)) {
                    etConfPwd.setError("Invalid Confirm Password");
                    etConfPwd.requestFocus();
                    return;
                }

                String strEmail = FirebaseManager.getInstance().getCurrentUserName();

                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                AuthCredential credential = EmailAuthProvider
                        .getCredential(strEmail, strCurPwd);
                progressDialog.show();

                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    user.updatePassword(strNewPwd).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressDialog.hide();
                                            if (task.isSuccessful()) {
                                                //Success
                                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                                editor.putString("user_pass", strNewPwd);
                                                editor.apply();

                                                layoutDone.setVisibility(View.VISIBLE);
                                                enableEditTextViews(false);
                                            } else {
                                                //Failed
                                                //shortToast("Failed");
                                            }
                                        }
                                    });
                                } else {
                                    //Auth Failed
                                    progressDialog.hide();
                                    ChangePwdActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            etCurrentPwd.setError("Invalid Password");
                                            etCurrentPwd.requestFocus();
                                        }
                                    });
                                }
                            }
                        });
            }
        });

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void enableEditTextViews(boolean bEnable) {
        etCurrentPwd.setEnabled(bEnable);
        etNewPwd.setEnabled(bEnable);
        etConfPwd.setEnabled(bEnable);
    }

    @Override
    protected void initUI() {

    }

    @Override
    public void onBackPressed() {
        if (layoutDone.getVisibility() == View.VISIBLE) {
            layoutDone.setVisibility(View.GONE);
            enableEditTextViews(true);
            return;
        }
        super.onBackPressed();
    }

}
