package com.mema.muslimkeyboard.activity;

import android.app.Dialog;
import android.content.Intent;
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
import com.sinch.verification.PhoneNumberFormattingTextWatcher;
import com.sinch.verification.PhoneNumberUtils;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignUpActivity extends AppCompatActivity {
    public static final String SMS = "sms";
    @BindView(R.id.spinner)
    CountrySpinner spinner;
    @BindView(R.id.countryCode)
    EditText mCountryCode;
    @BindView(R.id.phoneNumber)
    EditText mPhoneNumber;
    @BindView(R.id.smsVerificationButton)
    Button mSmsButton;
    @BindView(R.id.signUpLayout)
    LinearLayout signUpLayout;
    @BindView(R.id.progressIndicator)
    ProgressBar progressIndicator;
    private String mCountryIso;
    private TextWatcher mNumberTextWatcher;
    private String strPhoneNumber;
    private String strUserPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
        FontUtils.setFont(signUpLayout, FontUtils.AvenirLTStdBook);
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

    public void OnsignUpClick(View view) {

        strPhoneNumber = mPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(strPhoneNumber)) {
            mPhoneNumber.setError("Please enter valid phone");
        } else {
            if (NetworkAvailablity.getInstance().checkNetworkStatus(SignUpActivity.this)) {
                openPhoneConfirmationDialog();
            }
        }


    }

    private void openPhoneConfirmationDialog() {
        final Dialog dialog = new Dialog(SignUpActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_mobile_confirmation);
        final String e164Number = getE164Number();
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
                Intent intent = new Intent(SignUpActivity.this, VerificationCodeActivity.class);
                intent.putExtra(Intent.EXTRA_PHONE_NUMBER, e164Number);
                intent.putExtra(Intent.EXTRA_TITLE, "SignUp");
                startActivity(intent);
                dialog.dismiss();
                finish();
            }
        });
        text.setText(String.format("%s%s", getString(R.string.sendvefication_code_msg), e164Number));
        dialog.show();
    }

    public void OnCancelClick(View view) {
        super.onBackPressed();
    }
}
