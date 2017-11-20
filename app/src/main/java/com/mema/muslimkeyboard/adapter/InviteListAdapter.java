package com.mema.muslimkeyboard.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.contacts.ContactManager;
import com.mema.muslimkeyboard.contacts.SyncableContact;
import com.mema.muslimkeyboard.utility.Util;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by king on 13/10/2017.
 */

public class InviteListAdapter extends BaseAdapter {
    Context context1;
    LayoutInflater inflter;
    private ArrayList<SyncableContact> arraylist;

    public InviteListAdapter(Context context, ArrayList<SyncableContact> list) {
        this.context1 = context;
        inflter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        arraylist = new ArrayList<>(list);
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

        ViewHolder viewHolder;
        if (view == null) {
            view = inflter.inflate(R.layout.activity_group_friend_list_item, null);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        SyncableContact contact = arraylist.get(position);

        viewHolder.setData(contact);

        return view;
    }

    // fiter alphabate search name
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        arraylist.clear();
        if (charText.length() == 0) {
            arraylist.addAll(ContactManager.getContactBookInfoWithoutFriends());
        } else {
            for (SyncableContact wp : ContactManager.getContactBookInfoWithoutFriends()) {
                if (wp.name != null && !wp.name.equals(""))
                    if (wp.name.toLowerCase().contains(charText)) {
                        arraylist.add(wp);
                    }
            }
        }
        notifyDataSetChanged();
    }

    private class ViewHolder {
        public TextView tvName;
        public CircleImageView circleImageView;
        public ImageView checkImageView;
        public TextView tvPhoneNumber;

        public ViewHolder(View view) {
            tvName = (TextView) view.findViewById(R.id.tv_name);
            tvPhoneNumber = (TextView) view.findViewById(R.id.tv_phone);
            circleImageView = (CircleImageView) view.findViewById(R.id.profile_image);
            checkImageView = (ImageView) view.findViewById(R.id.check_image);
        }

        public void setData(SyncableContact contact) {
            tvName.setText(contact.name);
            if (contact.phones.size() > 0) {
                tvPhoneNumber.setVisibility(View.VISIBLE);
                String phoneNumber = contact.phones.get(0);
                if (!phoneNumber.startsWith("+"))
                    phoneNumber = "+" + phoneNumber;
                tvPhoneNumber.setText(phoneNumber);
            }
            circleImageView.setImageResource(R.mipmap.profile);
            if (Util.getInstance().isSelectedContact(contact)) {
                checkImageView.setImageResource(R.mipmap.checked);
            } else {
                checkImageView.setImageResource(R.mipmap.unchecked);
            }
        }
    }
}
