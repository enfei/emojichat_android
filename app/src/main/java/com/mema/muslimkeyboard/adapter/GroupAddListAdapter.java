package com.mema.muslimkeyboard.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.bean.User;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.mema.muslimkeyboard.utility.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by cano-02 on 4/4/17.
 */

public class GroupAddListAdapter extends BaseAdapter {
    Context context1;
    LayoutInflater inflter;
    private ArrayList<User> arraylist = new ArrayList<>();// array create for filter

    public GroupAddListAdapter(Context context, ArrayList<String> curIds) {
        this.context1 = context;
        inflter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arraylist.size();
    }

    @Override
    public Object getItem(int i) {
        return arraylist.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = inflter.inflate(R.layout.activity_group_friend_list_item, null);
        }

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.name = (TextView) view.findViewById(R.id.tv_name);
        viewHolder.circleImageView = (CircleImageView) view.findViewById(R.id.profile_image);
        viewHolder.checkImageView = (ImageView) view.findViewById(R.id.check_image);

        User user = arraylist.get(position);
        viewHolder.name.setText(user.username);

        if (user.photo != null && user.photo.length() > 0) {
            Picasso.with(context1).load(user.photo).into(viewHolder.circleImageView);
        } else {
            viewHolder.circleImageView.setImageResource(R.mipmap.profile);
        }

        if (Util.getInstance().isSelectedFriend(user)) {
            viewHolder.checkImageView.setImageResource(R.mipmap.checked);
        } else {
            viewHolder.checkImageView.setImageResource(R.mipmap.unchecked);
        }

        return view;
    }

    // fiter alphabate search name
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        arraylist.clear();

        for (User wp : FirebaseManager.getInstance().groupFriendList) {
            boolean bAddFlag = false;

            if (charText.length() == 0) {
                bAddFlag = true;
            } else {
                if (wp.username != null && !wp.username.equals(""))
                    if (wp.username.toLowerCase().contains(charText)) {
                        bAddFlag = true;
                    }
            }


            boolean bExist = false;
            for (int i = 0; i < Util.getInstance().workingGroupMember.size(); i++) {
                User tmp = Util.getInstance().workingGroupMember.get(i);
                if (tmp.id.equals(wp.id)) {
                    bExist = true;
                }
            }

            if (((bAddFlag == true)) && (!bExist) && (!wp.id.equals(FirebaseManager.getInstance().getCurrentUserID()))) {
                arraylist.add(wp);
            }
        }

        notifyDataSetChanged();
    }


    private class ViewHolder {
        public TextView name;
        public CircleImageView circleImageView;
        public ImageView checkImageView;
    }

}
