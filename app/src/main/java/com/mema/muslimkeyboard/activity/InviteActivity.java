package com.mema.muslimkeyboard.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.adapter.InviteListAdapter;
import com.mema.muslimkeyboard.contacts.ContactManager;
import com.mema.muslimkeyboard.contacts.SyncableContact;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.mema.muslimkeyboard.utility.Util;

import java.util.Locale;

/**
 * Created by king on 13/10/2017.
 */

public class InviteActivity extends AppCompatActivity {

    private EditText editSearch;
    private ImageView ivBack;
    private ImageView ivCancelSearch;
    private TextView tvSelectedCount;
    private ListView mListView;
    private Button btnInvite;
    private InviteListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        Util.getInstance().workingContacts.clear();

        initUI();
        loadData();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        hideSoftKeyboard();
        return true;
    }

    private void hideSoftKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                    INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void initUI() {
        editSearch = (EditText) findViewById(R.id.edt_search);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        ivCancelSearch = (ImageView) findViewById(R.id.iv_cancel_search);
        ivCancelSearch.setVisibility(View.INVISIBLE);
        tvSelectedCount = (TextView) findViewById(R.id.tvSelectedFriendsCount);
        mListView = (ListView) findViewById(R.id.listview);
        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard();
                return false;
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ivCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editSearch.setText("");
            }
        });

        btnInvite = (Button) findViewById(R.id.btn_invite);
        btnInvite.setEnabled(false);
        btnInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPerformDialog();
            }
        });

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = editSearch.getText().toString().toLowerCase(Locale.getDefault());
                mAdapter.filter(text);

                if (TextUtils.isEmpty(text))
                    ivCancelSearch.setVisibility(View.INVISIBLE);
                else
                    ivCancelSearch.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadData() {

        ContactManager.makeContactInfosWithoutFriends(this, FirebaseManager.getInstance().friendList);
        mAdapter = new InviteListAdapter(this, ContactManager.getContactBookInfoWithoutFriends());
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hideSoftKeyboard();
                SyncableContact contact = (SyncableContact) mAdapter.getItem(position);
                if (Util.getInstance().isSelectedContact(contact)) {
                    Util.getInstance().removeContact(contact);
                } else {
                    Util.getInstance().addContact(contact);
                }
                refreshListView();
            }
        });
    }

    private void refreshListView() {
        mAdapter.notifyDataSetChanged();
        if (Util.getInstance().workingContacts.size() > 0) {
            tvSelectedCount.setText(String.format("%d Friends Selected", Util.getInstance().workingContacts.size()));
            btnInvite.setEnabled(true);
        } else {
            tvSelectedCount.setText("");
            btnInvite.setEnabled(false);
        }
    }

    private void showPerformDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Invite Friends");
        builder.setMessage("You may be charged by your carrer for this text message. The recipient must be able to receive a message in order for the invitation to be delivered.");
        builder.setCancelable(true);
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                inviteFriends();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void inviteFriends() {

        StringBuilder uri = new StringBuilder("smsto:");
        for (SyncableContact contact: Util.getInstance().workingContacts) {
            uri.append(contact.phones.get(0).replace("+", ""));
            uri.append(";");
        }

        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(uri.toString()));
        smsIntent.putExtra("sms_body", "I am using Muslim Emoji Chat app. To chat with me for free please install the Muslim Emoji Chat on your phone at http://www.muslimemoji.com.");
        startActivity(smsIntent);

    }

}
