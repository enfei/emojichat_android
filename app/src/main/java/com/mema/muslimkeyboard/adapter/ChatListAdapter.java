package com.mema.muslimkeyboard.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.bean.Dialog;
import com.mema.muslimkeyboard.bean.Message;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.mema.muslimkeyboard.utility.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import pl.droidsonroids.gif.MultiCallback;

/**
 * Created by cano-02 on 7/4/17.
 */

public class ChatListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater inflter;

    ArrayList<Dialog> filterDialogs = new ArrayList<>();
    MultiCallback gifCallback = new MultiCallback(true);

    public ChatListAdapter(Context applicationContext) {

        this.context = applicationContext;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return filterDialogs.size();
    }

    @Override
    public Object getItem(int i) {
        return filterDialogs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = inflter.inflate(R.layout.chat_list_item, null);
        }

        Dialog dialog = (Dialog) getItem(i);
        Holder holder = new Holder();
        holder.textViewName = (TextView) view.findViewById(R.id.tv_name);
        holder.textViewTime = (TextView) view.findViewById(R.id.tv_time);
        holder.textViewMessage = (TextView) view.findViewById(R.id.tv_message);
        holder.profileImage = (ImageView) view.findViewById(R.id.profile_image);
        holder.textViewBadge = (TextView) view.findViewById(R.id.tvBadge);
        holder.textViewName.setText(dialog.title);
        if (dialog.photo != null && dialog.photo.length() > 0) {
            Picasso.with(context).load(dialog.photo).placeholder(R.mipmap.profile).into(holder.profileImage);
        } else {
            holder.profileImage.setImageResource(R.mipmap.profile);
        }

        if (dialog.lastMessage != null && dialog.lastMessage.length() > 0) {
            if (dialog.lastMessageType == Message.MessageType.Emoji) {
                holder.textViewMessage.setText("emoji added");
            } else if (dialog.lastMessageType == Message.MessageType.Photo) {
                holder.textViewMessage.setText("photo attached");
            } else if (dialog.lastMessageType == Message.MessageType.Video) {
                holder.textViewMessage.setText("video attached");
            } else if (dialog.lastMessage.startsWith("https://firebasestorage.googleapis.com")) {
                holder.textViewMessage.setText("photo attached");
            } else {
                if (!dialog.lastMessage.equalsIgnoreCase("")) {
                    gifCallback.addView(holder.textViewMessage);
                }
                holder.textViewMessage.setText(Util.getEmoticanText(context, dialog.lastMessage, gifCallback, true));
            }
            holder.textViewTime.setText(DateUtils.formatDateTime(context, dialog.lastMessageDateSent, DateUtils.FORMAT_SHOW_TIME));

            HashMap<String, String> badgeHash = FirebaseManager.getInstance().dialogBadges;
            String strBadge = "0";
            if (badgeHash.containsKey(dialog.dialogID)) {
                strBadge = badgeHash.get(dialog.dialogID);
            }

            holder.textViewBadge.setText(strBadge);

            if (strBadge.equals("0"))
                holder.textViewBadge.setVisibility(View.INVISIBLE);
            else
                holder.textViewBadge.setVisibility(View.VISIBLE);

        } else {
            holder.textViewMessage.setText("");
            holder.textViewTime.setText("");
            holder.textViewBadge.setVisibility(View.INVISIBLE);
        }

        return view;
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        filterDialogs.clear();
        if (charText.length() == 0) {
            filterDialogs.addAll(FirebaseManager.getInstance().dialogList);
        } else {
            for (Dialog wp : FirebaseManager.getInstance().dialogList) {
                if (wp.title != null && !wp.title.equals(""))
                    if (wp.title.toLowerCase().contains(charText)) {
                        filterDialogs.add(wp);
                    }
            }
        }

        Collections.reverse(filterDialogs);

        notifyDataSetChanged();
    }

    public class Holder {
        TextView textViewTime;
        TextView textViewMessage;
        TextView textViewName;
        ImageView profileImage;
        TextView textViewBadge;
    }

}


