package com.mema.muslimkeyboard.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.adapter.ContactListAdapter;
import com.mema.muslimkeyboard.adapter.widget.IndexableListView;
import com.mema.muslimkeyboard.bean.User;
import com.mema.muslimkeyboard.utility.BaseActivity;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.mema.muslimkeyboard.utility.Util;
import com.mema.muslimkeyboard.utility.models.FirebaseValueListener;

import java.util.Locale;

public class FriendActivity extends BaseActivity implements View.OnClickListener {

    EditText edtSearch;
    IndexableListView list;
    TextView tvTopTitle;
    ImageView imgBackL, ivCancelSearch, ivCancelRight, ivDone;

    FirebaseValueListener friendsListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        Util.getInstance().selFriends.clear();
        initView();
    }

    @Override
    protected void initUI() {

    }

    public void initView() {
        list = (IndexableListView) findViewById(R.id.list);

        imgBackL = (ImageView) findViewById(R.id.img_back);
        ivCancelRight = (ImageView) findViewById(R.id.img_backRight);

        ivDone = (ImageView) findViewById(R.id.img_done);

        edtSearch = (EditText) findViewById(R.id.edt_search);
        View empty = findViewById(R.id.emptyView);
        list.setEmptyView(empty);

        ivCancelSearch = (ImageView) findViewById(R.id.img_cancel_search);
        ivCancelSearch.setVisibility(View.INVISIBLE);

        ivCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtSearch.setText("");
            }
        });

        imgBackL.setVisibility(View.INVISIBLE);

        ivCancelRight.setVisibility(View.VISIBLE);
        ivDone.setVisibility(View.INVISIBLE);

        tvTopTitle = (TextView) findViewById(R.id.tvTopTitle);
        tvTopTitle.setText("");

        ivDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Util.getInstance().selFriends.size() == 0)
                    return;
                User user = Util.getInstance().selFriends.get(0);
                Intent intent = new Intent(FriendActivity.this, ChatActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
                finish();
            }
        });

        ivCancelRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        final ContactListAdapter adapter = new ContactListAdapter(FirebaseManager.getInstance().friendList, this);
        list.setAdapter(adapter);
        list.setFastScrollEnabled(true);

        friendsListener = FirebaseManager.getInstance().addFriendListener(new FirebaseManager.OnUpdateListener() {
            @Override
            public void onUpdate() {
                tvTopTitle.setText(String.valueOf(FirebaseManager.getInstance().friendList.size()) + " Contacts");
                String text = edtSearch.getText().toString().toLowerCase(Locale.getDefault());
                adapter.filter(text);
            }
        });

        imgBackL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.getInstance().selFriends.clear();
                adapter.notifyDataSetChanged();
                refreshTopButtons();
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int bIndex = -1;
                User user = adapter.getItem(position);
                for (int i = 0; i < Util.getInstance().selFriends.size(); i++) {
                    User temp = Util.getInstance().selFriends.get(i);
                    if (temp.email.equals(user.email)) {
                        bIndex = i;
                    }
                }

                if (bIndex != -1) {
                    Util.getInstance().selFriends.remove(bIndex);
                } else {
                    Util.getInstance().selFriends.clear();
                    Util.getInstance().selFriends.add(user);
                }

                refreshTopButtons();
                adapter.notifyDataSetChanged();
            }
        });

        String text = edtSearch.getText().toString().toLowerCase(Locale.getDefault());
        adapter.filter(text);

        edtSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
                String text = edtSearch.getText().toString().toLowerCase(Locale.getDefault());
                adapter.filter(text);
                if (TextUtils.isEmpty(text))
                    ivCancelSearch.setVisibility(View.INVISIBLE);
                else
                    ivCancelSearch.setVisibility(View.VISIBLE);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void refreshTopButtons() {
        if (Util.getInstance().selFriends.size() == 0) {
            ivDone.setVisibility(View.INVISIBLE);
            ivCancelRight.setVisibility(View.VISIBLE);
            imgBackL.setVisibility(View.INVISIBLE);
        } else {
            ivDone.setVisibility(View.VISIBLE);
            ivCancelRight.setVisibility(View.INVISIBLE);
            imgBackL.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (friendsListener != null) {
            friendsListener.removeListener();
        }
    }

    @Override
    public void onClick(View v) {

    }
}
