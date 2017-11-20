package com.mema.muslimkeyboard.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.bean.User;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.mema.muslimkeyboard.utility.models.FirebaseValueListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlockListActivity extends AppCompatActivity {

    private static final String TAG = BlockListActivity.class.getSimpleName();
    private ListView listView;
    private GroupEditListAdapter adapter;
    private FirebaseValueListener blockfriendsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_list);
        listView = (ListView) findViewById(R.id.list);
        View empty = findViewById(R.id.emptyView);
        listView.setEmptyView(empty);
        blockfriendsListener = FirebaseManager.getInstance().getBlockUserList(new FirebaseManager.OnUpdateListener() {
            @Override
            public void onUpdate() {
                adapter.notifyDataSetChanged();
            }
        });
        final ArrayList<User> blockFriendList = FirebaseManager.getInstance().blockFriendList;
        adapter = new GroupEditListAdapter(this, blockFriendList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FirebaseManager.getInstance().unBlockUser(blockFriendList.get(position).id);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (blockfriendsListener != null) {
            blockfriendsListener.removeListener();
        }
    }

    private class GroupEditListAdapter extends BaseAdapter {
        Context context1;
        LayoutInflater inflter;
        ArrayList<User> blockFriendList;

        GroupEditListAdapter(Context context, ArrayList<User> blockFriendList) {
            this.context1 = context;
            this.blockFriendList = blockFriendList;
            inflter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return blockFriendList.size();
        }

        @Override
        public Object getItem(int i) {
            return blockFriendList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {

            view = inflter.inflate(R.layout.member_blocklist_item, null);

            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.tv_name);
            viewHolder.circleImageView = (CircleImageView) view.findViewById(R.id.profile_image);
            viewHolder.tvUnblock = (TextView) view.findViewById(R.id.tv_unblock);
            User user = blockFriendList.get(position);
            viewHolder.name.setText(user.username);
            if (user.photo != null && user.photo.length() > 0) {
                Picasso.with(context1)
                        .load(user.photo)
                        .placeholder(R.mipmap.profile)
                        .error(R.mipmap.profile)
                        .into(viewHolder.circleImageView);
            } else {
                viewHolder.circleImageView.setImageResource(R.mipmap.profile);
            }

//            viewHolder.deleteImageView.setVisibility(View.VISIBLE);
//            viewHolder.deleteImageView.setTag(String.valueOf(position));


//            viewHolder.deleteImageView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    String strTag = (String) v.getTag();
//                    int position = Integer.valueOf(strTag);
//                    Util.getInstance().workingGroupMember.remove(position);
//
//                    runOnUiThread(new Runnable() {
//
//                        public void run() {
//                            notifyDataSetChanged();
//                        }
//                    });
//                }
//            });

            //  viewHolder.name.setText(user.firstname + " " + user.lastname);

           /* if (user.photo != null && user.photo.length() > 0) {
                Picasso.with(context1).load(user.photo).into(viewHolder.circleImageView);
            } else {
                viewHolder.circleImageView.setImageResource(R.mipmap.profile);
            }*/

            return view;
        }

        private class ViewHolder {
            public TextView name;
            public CircleImageView circleImageView;
            public TextView tvUnblock;
        }
    }
}
