package com.mema.muslimkeyboard.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.mema.muslimkeyboard.BuildConfig;
import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.bean.User;
import com.mema.muslimkeyboard.utility.AppConst;
import com.mema.muslimkeyboard.utility.BaseActivity;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.mema.muslimkeyboard.utility.UsernameExistQueryEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import io.fabric.sdk.android.Fabric;

import static android.view.View.GONE;


/**
 * Created by cano-08 on 30/3/17.
 */

public class SignIn extends BaseActivity implements View.OnClickListener {
    public static final String MyPREFERENCES = "MyPrefs";
    TextView txtLogin, txtForgetPassword, txtSignUp;

    TextView txtHintUsername, txtHintPwd;
    ImageView ivIconEye;

    Boolean bShowPwd = false;

    ImageView ivRemberCheck;

    EditText edtUserName, edtPassword;
    String strUserName, strUserPassword;
    AppConst appConst = new AppConst();
    SharedPreferences sharedpreferences;
    FirebaseAuth mAuth;

    RelativeLayout layoutPopup;
    View maskView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build());

        Crashlytics.setUserIdentifier("CrashId");
        Crashlytics.setUserEmail("cloud@fabric.io");
        Crashlytics.setUserName("Username");

        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        initView();

        boolean bShowPopup = sharedpreferences.getBoolean("isFirstTime", true);
        if (bShowPopup) {
            layoutPopup.setVisibility(View.VISIBLE);
        } else {
            layoutPopup.setVisibility(View.GONE);
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String logout = sharedpreferences.getString("session", "l");
        Boolean bRemember = sharedpreferences.getBoolean("Remember", false);

        if (logout.equals("login") && (bRemember == true)) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }
    }

    @Override
    protected void initUI() {

    }

    private void initView() {
        maskView = (View) findViewById(R.id.popupMask);
        maskView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        layoutPopup = (RelativeLayout) findViewById(R.id.popupOverlay);
        layoutPopup.setVisibility(GONE);

        findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutPopup.setVisibility(GONE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean("isFirstTime", false);
                editor.commit();
                editor.apply();
            }
        });

        txtLogin = (TextView) findViewById(R.id.txtLogin);
        txtSignUp = (TextView) findViewById(R.id.txtSingup);
        txtForgetPassword = (TextView) findViewById(R.id.txtForgetPwd);

        txtHintUsername = (TextView) findViewById(R.id.hintUsername);
        txtHintUsername.setVisibility(View.INVISIBLE);
        txtHintPwd = (TextView) findViewById(R.id.hintPwd);
        txtHintPwd.setVisibility(View.INVISIBLE);

        ivRemberCheck = (ImageView) findViewById(R.id.ivRemeberCheck);

        findViewById(R.id.layoutRemember).setOnClickListener(this);

        ivIconEye = (ImageView) findViewById(R.id.ivView);
        ivIconEye.setImageResource(R.mipmap.icon_view_enable);

        ivIconEye.setOnClickListener(this);

        edtUserName = (EditText) findViewById(R.id.edUsername);
        edtPassword = (EditText) findViewById(R.id.edPasword);
        txtLogin.setOnClickListener(this);
        txtSignUp.setOnClickListener(this);
        txtForgetPassword.setOnClickListener(this);

        SpannableString content = new SpannableString("Forgot Password");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        txtForgetPassword.setText(content);

        content = new SpannableString("SignUp");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        txtSignUp.setText(content);

        findViewById(R.id.txtTerms).setOnClickListener(this);
        findViewById(R.id.txtPrivacy).setOnClickListener(this);

        edtUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(edtUserName.getText())) {
                    txtHintUsername.setVisibility(View.INVISIBLE);
                } else {
                    txtHintUsername.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(edtPassword.getText())) {
                    txtHintPwd.setVisibility(View.INVISIBLE);
                } else {
                    txtHintPwd.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txtLogin: {
                boolean bRemember = false;
                if (ivRemberCheck.getVisibility() == View.VISIBLE) {
                    bRemember = true;
                }
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean("Remember", bRemember);
                editor.apply();
                signInService();

            }
            break;
            case R.id.txtSingup: {
                Intent goToNext = new Intent(SignIn.this, SignUp.class);
                startActivity(goToNext);
            }
            break;
            case R.id.txtForgetPwd: {
                Intent goToNext = new Intent(SignIn.this, ForgotPwdActivity.class);
                startActivity(goToNext);
            }
            break;
            case R.id.layoutRemember:
                boolean bRemember = false;
                if (ivRemberCheck.getVisibility() == View.VISIBLE) {
                    ivRemberCheck.setVisibility(View.INVISIBLE);
                } else {
                    ivRemberCheck.setVisibility(View.VISIBLE);
                    bRemember = true;
                }
                break;
            case R.id.ivView:
                bShowPwd = !bShowPwd;
                if (bShowPwd) {
                    ivIconEye.setImageResource(R.mipmap.icon_view_enable);
                    edtPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    edtPassword.setSelection(edtPassword.getText().length());
                } else {
                    ivIconEye.setImageResource(R.mipmap.icon_view_disable);
                    edtPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    edtPassword.setSelection(edtPassword.getText().length());
                }
                break;

            case R.id.txtTerms: {
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.muslimemoji.com/terms"));
                startActivity(intent);
            }
            break;
            case R.id.txtPrivacy: {
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.muslimemoji.com/privacy"));
                startActivity(intent);
            }
            break;
            default:
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean bShowPopup = sharedpreferences.getBoolean("isFirstTime", true);
        if (bShowPopup) {
            layoutPopup.setVisibility(View.VISIBLE);
        } else {
            layoutPopup.setVisibility(View.GONE);
        }
    }

    private void signInService() {
        strUserName = edtUserName.getText().toString().trim();
        strUserPassword = edtPassword.getText().toString().trim();

        if (strUserName.equals("") && strUserPassword.equals("")) {

            edtUserName.setError("Please enter valid username or Email id");
            edtPassword.setError("Please enter the password");

        } else if (strUserName.equals("")) {
            edtUserName.setError("Please enter valid username or Email id");

        } else if (strUserPassword.equals("")) {
            edtPassword.setError("Please enter the password");

        } else if (strUserPassword.length() < 8 && strUserPassword.length() > 12) {
            edtPassword.setError("Invalid password");

        } else {
//            if (NetworkAvailablity.getInstance().checkNetworkStatus(SignIn.this))
            {
                signIn(strUserName, strUserPassword);
            }
        }
    }

    private void signInWithEmail(final String userName, final String userPass) {
        mAuth.signInWithEmailAndPassword(userName, userPass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignIn.this, task.getException().getLocalizedMessage(),
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

                        Intent goToNext = new Intent(SignIn.this, HomeActivity.class);
                        startActivity(goToNext);
                        finish();
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(SignIn.this, "Login Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signIn(final String userName, final String userPass) {

        if (strUserPassword.contains("@")) {
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(strUserName.toString()).matches()) {
                edtUserName.setError("Please enter a valid email address");
                return;
            }

            progressDialog.show();
            signIn(userName, userPass);
            return;
        }

        progressDialog.show();
        UsernameExistQueryEventListener completionHandler = new UsernameExistQueryEventListener() {
            @Override
            public void onUsernameQueryDone(Boolean bSuccess, String email) {
                if (bSuccess) {
                    signInWithEmail(email, userPass);
                } else {
                    signInWithEmail(userName.toLowerCase(), userPass);
                }
            }
        };
        FirebaseManager.getInstance().getEmailFromUsername(userName, completionHandler);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
