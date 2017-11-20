package com.mema.muslimkeyboard.activity.group;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.adapter.GroupFriendListAdapter;
import com.mema.muslimkeyboard.bean.User;
import com.mema.muslimkeyboard.utility.BaseActivity;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.mema.muslimkeyboard.utility.Util;
import com.mema.muslimkeyboard.utility.models.FirebaseValueListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Super on 5/13/2017.
 */

public class GroupChatActivity extends BaseActivity implements View.OnClickListener {
    private static int NEW_GROUP_ACTIVITY_REQUEST_CODE = 100;

    ListView listviewFriends;
    RecyclerView listviewMembers;
    TextView textViewMembers;
    EditText edtSearch;
    FirebaseValueListener friendsListener;
    GroupFriendListAdapter adapter;
    GroupMembersSmallAdapter memberAdapter;
    ImageView ivCancelSearch;
    ArrayList<User> filterUserArraylist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        Util.getInstance().workingFriends.clear();
        filterUserArraylist = new ArrayList<>();
        initUI();
        getFriends();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshMembers();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (friendsListener != null) {
            friendsListener.removeListener();
        }
    }

    @Override
    protected void initUI() {
        findViewById(R.id.img_back).setOnClickListener(this);
        findViewById(R.id.img_done).setOnClickListener(this);

        ivCancelSearch = (ImageView) findViewById(R.id.img_cancel_search);
        ivCancelSearch.setVisibility(View.INVISIBLE);

        ivCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtSearch.setText("");
            }
        });

        textViewMembers = (TextView) findViewById(R.id.tvSelectedFriendsCount);
        edtSearch = (EditText) findViewById(R.id.edt_search);
        listviewFriends = (ListView) findViewById(R.id.list);
        listviewMembers = (RecyclerView) findViewById(R.id.list_members);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        listviewMembers.setLayoutManager(layoutManager);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.img_done:
                if (Util.getInstance().workingFriends.size() > 0) {
                    Intent i = new Intent(this, NewGroupActivity.class);
                    startActivityForResult(i, NEW_GROUP_ACTIVITY_REQUEST_CODE);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_GROUP_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            finish();
        }
    }

    public void refreshMembers() {
        adapter.notifyDataSetChanged();
        memberAdapter.notifyDataSetChanged();
        if (Util.getInstance().workingFriends.size() > 0) {
            textViewMembers.setText(String.format("%d Friends Selected", Util.getInstance().workingFriends.size()));
            listviewMembers.setVisibility(View.VISIBLE);

        } else {
            textViewMembers.setText("");
            listviewMembers.setVisibility(View.GONE);
        }
    }

    public void getFriends() {
        getfilterUserList(edtSearch.getText().toString().toLowerCase(Locale.getDefault()));
        adapter = new GroupFriendListAdapter(this,filterUserArraylist);
        listviewFriends.setAdapter(adapter);

        memberAdapter = new GroupMembersSmallAdapter(this);
        listviewMembers.setAdapter(memberAdapter);

        friendsListener = FirebaseManager.getInstance().addGroupFriendListener(new FirebaseManager.OnUpdateListener() {
            @Override
            public void onUpdate() {
                String text = edtSearch.getText().toString().toLowerCase(Locale.getDefault());
                getfilterUserList(text);
                adapter.notifyDataSetChanged();
            }
        });

        listviewFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = (User) adapter.getItem(position);
                if (Util.getInstance().isSelectedFriend(user)) {
                    Util.getInstance().removeFriend(user);
                } else {
                    Util.getInstance().addFriend(user);
                }
                refreshMembers();
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
                String text = edtSearch.getText().toString().toLowerCase(Locale.getDefault());
                getfilterUserList(text);
                adapter.notifyDataSetChanged();

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

    public void getfilterUserList(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        filterUserArraylist.clear();
        if (charText.length() == 0) {
            filterUserArraylist.addAll(FirebaseManager.getInstance().groupFriendList);
        } else {
            for (User wp : FirebaseManager.getInstance().groupFriendList) {
                if (wp.username != null && !wp.username.equals(""))
                    if (wp.username.toLowerCase().contains(charText)) {
                        filterUserArraylist.add(wp);
                    }
            }
        }
    }

    public class GroupMembersSmallAdapter extends RecyclerView.Adapter<GroupMemberViewHolder> {
        Context context1;
        LayoutInflater inflter;

        public GroupMembersSmallAdapter(Context context) {
            this.context1 = context;
            inflter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getItemCount() {
            return Util.getInstance().workingFriends.size();
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public void onBindViewHolder(GroupMemberViewHolder viewHolder, final int i) {
            User user = Util.getInstance().workingFriends.get(i);
            viewHolder.name.setText(user.username);

            if (user.photo != null && user.photo.length() > 0) {
                Picasso.with(context1).load(user.photo).into(viewHolder.circleImageView);
            } else {
                viewHolder.circleImageView.setImageResource(R.mipmap.profile);
            }

            viewHolder.checkImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.getInstance().removeFriend(Util.getInstance().workingFriends.get(i));
                    refreshMembers();
                }
            });
        }

        @Override
        public GroupMemberViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater
                    .from(viewGroup.getContext())
                    .inflate(R.layout.activity_group_member_list_item, viewGroup, false);
            return new GroupMemberViewHolder(itemView);
        }
    }

    private class GroupMemberViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public CircleImageView circleImageView;
        public ImageView checkImageView;

        public GroupMemberViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.tv_name);
            circleImageView = (CircleImageView) view.findViewById(R.id.profile_image);
            checkImageView = (ImageView) view.findViewById(R.id.check_image);
        }
    }
}
