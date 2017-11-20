package com.mema.muslimkeyboard.activity.group;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.activity.profile.ProfileActivity;
import com.mema.muslimkeyboard.bean.Dialog;
import com.mema.muslimkeyboard.bean.User;
import com.mema.muslimkeyboard.utility.BaseActivity;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupInfoActivity extends BaseActivity {
    Dialog dialog;
    CircleImageView imvAvatar;
    TextView tvGroupName, tvParticipants, tvEdit;
    ListView listView;
    GroupListAdapter adapter;
    ArrayList<User> userList;
    boolean bNotEnable = true;
    ImageView ivNotification;
    LinearLayout layoutEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        userList = new ArrayList<>();
        dialog = (Dialog) getIntent().getSerializableExtra("dialog");

        imvAvatar = (CircleImageView) findViewById(R.id.iv_group_photo);

        if (dialog.photo != null && dialog.photo.length() > 0) {
            Picasso.with(GroupInfoActivity.this).load(dialog.photo).into(imvAvatar);
        }

        tvGroupName = (TextView) findViewById(R.id.tvGroupName);
        tvParticipants = (TextView) findViewById(R.id.tvParticipants);


        listView = (ListView) findViewById(R.id.list);

        tvGroupName.setText(dialog.title);
        tvParticipants.setText("Participants: " + String.valueOf(dialog.occupantsIds.size()));

        adapter = new GroupListAdapter(this);
        listView.setAdapter(adapter);
        FirebaseManager firebaseManager = FirebaseManager.getInstance();
        firebaseManager.getUser(firebaseManager.getCurrentUserID(), new FirebaseManager.OnUserResponseListener() {
            @Override
            public void onUserResponse(User user) {
                userList.add(user);
                adapter.notifyDataSetChanged();
            }
        });
        for (int i = 0; i < dialog.occupantsIds.size(); i++) {
            String strID = dialog.occupantsIds.get(i);
            firebaseManager.getUser(strID, new FirebaseManager.OnUserResponseListener() {
                @Override
                public void onUserResponse(User user) {
                    userList.add(user);
                    adapter.notifyDataSetChanged();
                }
            });
        }

        findViewById(R.id.ivBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvEdit = (TextView) findViewById(R.id.tvEdit);
        if ((!TextUtils.isEmpty(dialog.adminId)) && dialog.adminId.equals(FirebaseManager.getInstance().getCurrentUserID())) {
            findViewById(R.id.layoutEdit).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layoutEdit).setVisibility(View.GONE);
        }

        findViewById(R.id.layoutEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupEditActivity.class);
                intent.putExtra("dialog", dialog);
                startActivity(intent);
                finish();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = userList.get(position);
                Intent i = new Intent(GroupInfoActivity.this, ProfileActivity.class);
                i.putExtra("userID", user.id);
                startActivity(i);
            }
        });

        ivNotification = (ImageView) findViewById(R.id.noti_toggle);
        findViewById(R.id.notificationLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bNotEnable = !bNotEnable;
                if (bNotEnable)
                    ivNotification.setImageResource(R.mipmap.switch_on);
                else
                    ivNotification.setImageResource(R.mipmap.switch_off);
            }
        });
    }

    @Override
    protected void initUI() {

    }

    public class GroupListAdapter extends BaseAdapter {
        Context context1;
        LayoutInflater inflter;

        public GroupListAdapter(Context context) {
            this.context1 = context;
            inflter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return userList.size();
        }

        @Override
        public Object getItem(int i) {
            return userList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {

            view = inflter.inflate(R.layout.member_list_item, null);

            final GroupInfoActivity.GroupListAdapter.ViewHolder viewHolder = new GroupInfoActivity.GroupListAdapter.ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.tv_name);
            viewHolder.circleImageView = (CircleImageView) view.findViewById(R.id.profile_image);
            viewHolder.deleteImageView = (ImageView) view.findViewById(R.id.delete_img);
            viewHolder.tvAdmin = (TextView) view.findViewById(R.id.tv_admin);

            viewHolder.deleteImageView.setVisibility(View.INVISIBLE);

            User user = userList.get(position);
            if (dialog.adminId.equals(user.id)) {
                viewHolder.tvAdmin.setVisibility(View.VISIBLE);
                viewHolder.deleteImageView.setVisibility(View.GONE);
            } else {
                viewHolder.tvAdmin.setVisibility(View.GONE);
                viewHolder.deleteImageView.setVisibility(View.VISIBLE);
            }

            viewHolder.name.setText(user.username);
            if (user.photo != null && user.photo.length() > 0) {
                Picasso.with(context1).load(user.photo).into(viewHolder.circleImageView);
            } else {
                viewHolder.circleImageView.setImageResource(R.mipmap.profile);
            }

            return view;
        }

        private class ViewHolder {
            public TextView name;
            public TextView tvAdmin;
            public CircleImageView circleImageView;
            public ImageView deleteImageView;
        }

    }
}

