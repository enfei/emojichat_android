package com.mema.muslimkeyboard.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.activity.profile.UserProfileActivity;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.mema.muslimkeyboard.utility.FontUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sinch.verification.Verification;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VerificationCodeActivity extends AppCompatActivity {
    private static final String TAG = VerificationCodeActivity.class.getSimpleName();
    @BindView(R.id.txt_phoneNumber)
    TextView mtxtPhoneNumber;
    @BindView(R.id.txt_back)
    TextView mtxtBack;
    @BindView(R.id.reVerification)
    TextView reVerification;

    @BindView(R.id.edt_confirmation_code)
    EditText edtConfirmationCode;
    @BindView(R.id.progressIndicator)
    ProgressBar mProgressIndicator;
    @BindView(R.id.verificationLayout)
    LinearLayout verificationLayout;

    private boolean mShouldFallback = true;

    private boolean mIsSmsVerification;
    private static final String APPLICATION_KEY = "13c34212-f919-4b65-9482-01f79a23186a";
    private Verification mVerification;
    private static final String[] SMS_PERMISSIONS = {Manifest.permission.INTERNET,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.ACCESS_NETWORK_STATE};
    private static final String[] FLASHCALL_PERMISSIONS = {Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ACCESS_NETWORK_STATE};
    private String mPhoneNumber;
    private BottomSheetDialog dialog;
    private String extraTitle;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_code);
        ButterKnife.bind(this);
        FontUtils.setFont(verificationLayout, FontUtils.AvenirLTStdBook);
        mAuth = FirebaseAuth.getInstance();
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        mPhoneNumber = getIntent().getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        extraTitle = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        mtxtPhoneNumber.setText(mPhoneNumber);
        mIsSmsVerification = true;
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verificaiton without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                hideProgress();
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                hideProgress();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // ...
            }
        };
        requestPermissions();
    }

    private void requestPermissions() {
        List<String> missingPermissions;
        String methodText;

        if (mIsSmsVerification) {
            missingPermissions = getMissingPermissions(SMS_PERMISSIONS);
            methodText = "SMS";
        } else {
            missingPermissions = getMissingPermissions(FLASHCALL_PERMISSIONS);
            methodText = "calls";
        }

        if (missingPermissions.isEmpty()) {
            createVerification(0);
        } else {
            if (needPermissionsRationale(missingPermissions)) {
                Toast.makeText(this, "This application needs permissions to read your " + methodText + " to automatically verify your "
                        + "phone, you may disable the permissions once you have been verified.", Toast.LENGTH_LONG)
                        .show();
            }
            ActivityCompat.requestPermissions(this,
                    missingPermissions.toArray(new String[missingPermissions.size()]),
                    0);
        }

    }

    /**
     * @param type 0 means sms and 1 means call
     */
    private void createVerification(int type) {
        // It is important to pass ApplicationContext to the Verification config builder as the
        // verification process might outlive the activity.
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mPhoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);

       /* Config config = SinchVerification.config()
                .applicationKey(APPLICATION_KEY)
                .context(getApplicationContext())
                .build();
        VerificationListener listener = new MyVerificationListener();
        switch (type) {
            case 0:
                mVerification = SinchVerification.createSmsVerification(config, mPhoneNumber, listener);
                mVerification.initiate();
                break;
            default:
                mVerification = SinchVerification.createFlashCallVerification(config, mPhoneNumber, listener);
                mVerification.initiate();
                break;
        }*/
        showProgress();
    }

    private boolean needPermissionsRationale(List<String> permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Proceed with verification after requesting permissions.
        // If the verification SDK fails to intercept the code automatically due to missing permissions,
        // the VerificationListener.onVerificationFailed(1) method will be executed with an instance of
        // CodeInterceptionException. In this case it is still possible to proceed with verification
        // by asking the user to enter the code manually.
        createVerification(0);
    }

    private List<String> getMissingPermissions(String[] requiredPermissions) {
        List<String> missingPermissions = new ArrayList<>();
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        return missingPermissions;
    }

    @OnClick(R.id.btn_submit)
    void submit() {
        String code = edtConfirmationCode.getText().toString();
        if (!code.isEmpty()) {
            try {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                signInWithPhoneAuthCredential(credential);
                showProgress();
            } catch (Exception e) {
                e.printStackTrace();
                edtConfirmationCode.setError(e.getLocalizedMessage());
            }
        } else {
            edtConfirmationCode.setError("Please enter code");
        }
    }

    private void hideProgress() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressIndicator);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void showProgress() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressIndicator);
        progressBar.setVisibility(View.VISIBLE);
    }


    private void openPasswordCreateActvity() {
        startActivity(new Intent(VerificationCodeActivity.this, ForgotPwdActivity.class));
    }

    private void openUserProfileActivity() {
        Intent intent = new Intent(VerificationCodeActivity.this, UserProfileActivity.class);
        intent.putExtra(Intent.EXTRA_PHONE_NUMBER, mPhoneNumber);
        startActivity(intent);
        finish();
    }

    private void openHomeActivity() {
        startActivity(new Intent(VerificationCodeActivity.this, HomeActivity.class));
        finish();
    }

    @OnClick(R.id.txt_back)
    void setMtxtBack() {
        finish();
    }

    public void setCancel(View view) {
        if (dialog.isShowing())
            dialog.dismiss();
    }

    public void setVerification_call(View view) {
        createVerification(1);
        if (dialog.isShowing())
            dialog.dismiss();
    }

    public void setVerification_resend(View view) {
        createVerification(0);
        if (dialog.isShowing())
            dialog.dismiss();
    }

    @OnClick(R.id.reVerification)
    void setReVerification() {
        dialog = new BottomSheetDialog(VerificationCodeActivity.this);
        dialog.setContentView(R.layout.dialog_revarification);
        LinearLayout dialogLayout = (LinearLayout) dialog.findViewById(R.id.reVerificationLayout);
        FontUtils.setFont(dialogLayout, FontUtils.AvenirLTStdBook);
        dialog.show();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            if (TextUtils.isEmpty(user.getDisplayName())) {
                                openUserProfileActivity();
                            } else {
                                SharedPreferences.Editor editor = sharedpreferences.edit();
                                editor.putString("session", "login");
                                editor.putString("personName", user.getDisplayName());
                                editor.putString("push_token", FirebaseInstanceId.getInstance().getToken());
                                editor.apply();
                                FirebaseManager.getInstance().updatePushToken(FirebaseInstanceId.getInstance().getToken());
                                openHomeActivity();
                            }
                            Log.d(TAG, "onComplete() called with: user = [" + user.getPhoneNumber() + "]");
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }
}
