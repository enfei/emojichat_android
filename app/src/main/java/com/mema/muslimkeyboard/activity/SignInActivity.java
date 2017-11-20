package com.mema.muslimkeyboard.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.contryspinner.CountrySpinner;
import com.mema.muslimkeyboard.contryspinner.Iso2Phone;
import com.mema.muslimkeyboard.utility.FontUtils;
import com.mema.muslimkeyboard.utility.NetworkAvailablity;
import com.google.firebase.auth.FirebaseAuth;
import com.sinch.verification.PhoneNumberFormattingTextWatcher;
import com.sinch.verification.PhoneNumberUtils;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignInActivity extends AppCompatActivity {
    @BindView(R.id.signInLayout)
    LinearLayout signInLayout;
    @BindView(R.id.progressIndicator)
    ProgressBar progressDialog;
    private String mCountryIso;
    private TextWatcher mNumberTextWatcher;
    @BindView(R.id.spinner)
    CountrySpinner spinner;
    @BindView(R.id.countryCode)
    EditText mCountryCode;
    @BindView(R.id.phoneNumber)
    EditText mPhoneNumber;
    @BindView(R.id.smsVerificationButton)
    Button mSmsButton;
    FirebaseAuth mAuth;
    private SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs";
    private String e164Number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);
        FontUtils.setFont(signInLayout, FontUtils.AvenirLTStdBook);
        mAuth = FirebaseAuth.getInstance();
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        mCountryIso = PhoneNumberUtils.getDefaultCountryIso(this);
        final String defaultCountryName = new Locale("", mCountryIso).getDisplayName();
        spinner.init(defaultCountryName);
        spinner.addCountryIsoSelectedListener(new CountrySpinner.CountryIsoSelectedListener() {
            @Override
            public void onCountryIsoSelected(String selectedIso) {
                if (selectedIso != null) {
                    mCountryCode.setText(Iso2Phone.getPhone(selectedIso));
                    mCountryIso = selectedIso;
                    resetNumberTextWatcher(mCountryIso);
                    // force update:
                    mNumberTextWatcher.afterTextChanged(mPhoneNumber.getText());
                }
            }
        });
        resetNumberTextWatcher(mCountryIso);
    }

    public void OnsignInClick(View view) {
        signInService();
    }

    /*private void signInWithEmail(final String userName, final String userPass) {
        mAuth.signInWithEmailAndPassword(userName, userPass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.setVisibility(View.INVISIBLE);
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, task.getException().getLocalizedMessage(),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        FirebaseManager.getInstance().updatePushToken(FirebaseInstanceId.getInstance().getToken());

                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("session", "login");
                        editor.putString("user_email", userName);
                        editor.putString("user_pass", userPass);
                        editor.putString("user_id", task.getResult().getUser().getUid());
                        editor.putString("push_token", FirebaseInstanceId.getInstance().getToken());

                        editor.apply();
                        String uID = task.getResult().getUser().getUid();
                        FirebaseManager.getInstance().getUser(uID, new FirebaseManager.OnUserResponseListener() {
                            @Override
                            public void onUserResponse(User user) {
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString("user_name", user.username);
                                editor.apply();
                            }
                        });

                        setResult(RESULT_OK);

//                        openPhoneConfirmationDialog();
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.setVisibility(View.INVISIBLE);
                Toast.makeText(SignInActivity.this, "Error in Login" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void signIn(final String userName, final String userPass) {
        progressDialog.setVisibility(View.VISIBLE);
        UsernameExistQueryEventListener completionHandler = new UsernameExistQueryEventListener() {
            @Override
            public void onUsernameQueryDone(Boolean bSuccess, String email) {
                if (bSuccess) {
                    signInWithEmail(email, userPass);
                } else {
                    signInWithEmail(userName.toLowerCase().trim(), userPass);
                }
            }
        };
        //FirebaseManager.getInstance().getEmailFromUsername(userName, completionHandler);
    }*/

    private void signInService() {
        if (TextUtils.isEmpty(mPhoneNumber.getText().toString())) {
            mPhoneNumber.setError("Please enter valid username or Email id");

        }
//        else if (TextUtils.isEmpty(strUserPassword)) {
//            edtPassword.setError("Please enter the password");
//
//        } else if (strUserPassword.length() < 8 && strUserPassword.length() > 12) {
//            edtPassword.setError("Invalid password");
//
        else {
            if (NetworkAvailablity.getInstance().checkNetworkStatus(SignInActivity.this)) {
                e164Number = getE164Number();
                //signIn(e164Number, strUserPassword);
//                signInWithEmail(String.format("%s@muslimapp.com", e164Number), strUserPassword);
                openPhoneConfirmationDialog();
            }
        }
    }

    void openPhoneConfirmationDialog() {

        final Dialog dialog = new Dialog(SignInActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_mobile_confirmation);
        final String e164Number = getE164Number();
        LinearLayout dialogLayout = (LinearLayout) dialog.findViewById(R.id.dialogLayout);
        FontUtils.setFont(dialogLayout, FontUtils.AvenirLTStdBook);
        TextView text = (TextView) dialog.findViewById(R.id.txt_verification_mobile);
        TextView mCancel = (TextView) dialog.findViewById(R.id.txt_cancle);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.isShowing())
                    dialog.dismiss();
            }
        });
        TextView mOk = (TextView) dialog.findViewById(R.id.txt_ok);
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, VerificationCodeActivity.class);
                intent.putExtra(Intent.EXTRA_PHONE_NUMBER, e164Number);
                intent.putExtra(Intent.EXTRA_TITLE, "SignIn");
                startActivity(intent);
                dialog.dismiss();
                finish();
            }
        });
        text.setText(String.format("%s%s", getString(R.string.sendvefication_code_msg), e164Number));
        dialog.show();

    }

    private void resetNumberTextWatcher(String countryIso) {

        if (mNumberTextWatcher != null) {
            mPhoneNumber.removeTextChangedListener(mNumberTextWatcher);
        }

        mNumberTextWatcher = new PhoneNumberFormattingTextWatcher(countryIso) {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                super.onTextChanged(s, start, before, count);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                super.beforeTextChanged(s, start, count, after);
            }

            @Override
            public synchronized void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                if (isPossiblePhoneNumber()) {
                    setButtonsEnabled(true);
                    mPhoneNumber.setTextColor(Color.BLACK);
                } else {
                    setButtonsEnabled(false);
                    mPhoneNumber.setTextColor(Color.RED);
                }
            }
        };

        mPhoneNumber.addTextChangedListener(mNumberTextWatcher);
    }

    private void setButtonsEnabled(boolean enabled) {
        mSmsButton.setEnabled(enabled);
    }

    private boolean isPossiblePhoneNumber() {
        return PhoneNumberUtils.isPossibleNumber(mCountryCode.getText().toString() + mPhoneNumber.getText().toString(), mCountryIso);
    }

    private String getE164Number() {
        return PhoneNumberUtils.formatNumberToE164(mCountryCode.getText().toString() + mPhoneNumber.getText().toString(), mCountryIso);
    }

    public void OnCancelClick(View view) {
        finish();
    }

    public void onClickForgotPwd(View view) {
        if (!TextUtils.isEmpty(mPhoneNumber.getText().toString())) {
            e164Number = getE164Number();
            Intent intent = new Intent(SignInActivity.this, VerificationCodeActivity.class);
            intent.putExtra(Intent.EXTRA_PHONE_NUMBER, e164Number);
            intent.putExtra(Intent.EXTRA_TITLE, "ForgotPassward");
            startActivity(intent);
            finish();
        } else {
            mPhoneNumber.setError("Please Enter Number");
        }

    }
}
