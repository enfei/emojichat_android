package com.mema.muslimkeyboard.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.bean.User;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.mema.muslimkeyboard.utility.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by cano-02 on 4/4/17.
 */


public class ContactListAdapter extends BaseAdapter implements SectionIndexer {

    private String mSections = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private ArrayList<User> mCurrentList;
    private ArrayList<User> mFilterList;
    private Context mContext;

    public ContactListAdapter(ArrayList<User> listData, Context mContext) {
        super();

        this.mContext = mContext;
        this.mCurrentList = new ArrayList<>();
        this.mFilterList = new ArrayList<>();

        if (listData != null) {
            this.mCurrentList.addAll(listData);
            this.mFilterList.addAll(listData);
        }
    }

    @Override
    public int getCount() {
        if (mFilterList != null)
            return mFilterList.size();
        else
            return 0;
    }

    @Override
    public User getItem(int position) {
        if (mFilterList != null)
            return mFilterList.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void onItemSelected(int position) {

    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        mCurrentList.clear();
        mCurrentList.addAll(FirebaseManager.getInstance().friendList);
        Collections.sort(mCurrentList, new ContactListAdapter.UserNameComparator());
        mFilterList.clear();
        if (charText.length() == 0) {
            mFilterList.addAll(mCurrentList);
        } else {
            for (User wp : FirebaseManager.getInstance().friendList) {
                if (wp.username != null && !wp.username.equals(""))
                    if (wp.username.toLowerCase().contains(charText)) {
                        mFilterList.add(wp);
                    }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final int p = position;

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.activity_contact_list_sel_item, null);

        try {
            TextView textRow = (TextView) convertView.findViewById(R.id.tv_name);
            User user = getItem(position);
            textRow.setText(user.username);

            TextView textLetter = (TextView) convertView.findViewById(R.id.tv_letter);
            textLetter.setText("");

            User curUser = getItem(position);
            String curFirstLetter = curUser.username.toLowerCase().substring(0, 1);
            View sectionDivider = (View) convertView.findViewById(R.id.sectionDivider);
            sectionDivider.setVisibility(View.INVISIBLE);

            if (position != 0) {
                User prevUser = getItem(position - 1);
                String prevFirstLetter = prevUser.username.substring(0, 1).toLowerCase();

                if (prevFirstLetter.equals(curFirstLetter)) {
                    textLetter.setText("");
                } else {
                    textLetter.setText(curFirstLetter.toUpperCase());
                    sectionDivider.setVisibility(View.VISIBLE);
                }
            } else {
                textLetter.setText(curFirstLetter.toUpperCase());
            }

            ImageView ivCheck = (ImageView) convertView.findViewById(R.id.check_image);

            boolean bExist = false;
            for (User u : Util.getInstance().selFriends) {
                if (u.email.equals(curUser.email)) {
                    bExist = true;
                    break;
                }
            }

            if (bExist) {
                ivCheck.setImageResource(R.mipmap.checked);
            } else {
                ivCheck.setImageResource(R.mipmap.unchecked);
            }

            TextView tvLastSeen = (TextView) convertView.findViewById(R.id.tv_lastseen);
            if (user.lastSeen == 0)
                tvLastSeen.setText("");
            else
                tvLastSeen.setText(String.valueOf(user.lastSeen));
        } catch (Exception e) {
        }

        return convertView;
    }

    @Override
    public int getPositionForSection(int section) {
        for (int j = 0; j < getCount(); j++) {
            if (section == 0) {
                // For numeric section
                for (int k = 0; k <= 9; k++) {
                    String text = null;
                    try {
                        User user = mFilterList.get(j);
                        text = user.username;
                    } catch (Exception e) {
                    }
                    if (text == null)
                        return 0;
                    else if (String.valueOf(text.charAt(0)).toLowerCase().equals(String.valueOf(String.valueOf(k)).toString().toLowerCase()))
                        return j;
                }
            } else {
                String artist = null;
                try {
                    User user = mFilterList.get(j);
                    artist = user.username;
                } catch (Exception e) {
                }
                if (artist == null)
                    return 0;
                else if (String.valueOf(artist.charAt(0)).toLowerCase().equals(String.valueOf(mSections.charAt(section)).toString().toLowerCase())) {
                    return j;
                }
            }
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public Object[] getSections() {
        String[] sections = new String[mSections.length()];
        for (int i = 0; i < mSections.length(); i++)
            sections[i] = String.valueOf(mSections.charAt(i));
        return sections;
    }

    public class UserNameComparator implements Comparator<User> {
        public int compare(User left, User right) {
            return left.username.compareTo(right.username);
        }
    }
}
