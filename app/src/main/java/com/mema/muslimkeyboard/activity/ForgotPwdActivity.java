package com.mema.muslimkeyboard.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.utility.FontUtils;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ForgotPwdActivity extends AppCompatActivity {


    @BindView(R.id.progressIndicator)
    ProgressBar progressIndicator;
    @BindView(R.id.edt_password)
    EditText edtPassword;
    @BindView(R.id.edt_confpassword)
    EditText edtConfpassword;
    @BindView(R.id.btnCreatePassword)
    Button btnCreatePassword;
    @BindView(R.id.signInLayout)
    LinearLayout forgotPwdInLayout;
    FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pwd);
        ButterKnife.bind(this);
        FontUtils.setFont(forgotPwdInLayout, FontUtils.AvenirLTStdBook);
        mAuth = FirebaseAuth.getInstance();
    }

   /* public void resetPwd(String strEmail) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.sendPasswordResetEmail(strEmail)
                .addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        progressDialog.hide();
                        Log.e("ForgotPwd", "Success");
                        layoutDone.setVisibility(View.VISIBLE);
                        etEmail.setEnabled(false);

                    }
                }).addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.hide();
                Log.e("ForgotPwd", "Failed");
                tvInvalidEmail.setVisibility(View.VISIBLE);

            }
        });
    }*/


    public void OnClickCreateNewPassword(View view) {
        if (edtPassword.getText().toString().equals(edtConfpassword.getText().toString())) {
            setNewPassword();
        } else {
            Toast.makeText(ForgotPwdActivity.this, "Password Not matching", Toast.LENGTH_SHORT).show();

        }
    }

    private void setNewPassword() {

    }
}
