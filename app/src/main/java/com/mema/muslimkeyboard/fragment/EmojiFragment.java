package com.mema.muslimkeyboard.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mema.muslimkeyboard.R;

/**
 * Created by cano-02 on 5/4/17.
 */

public class EmojiFragment extends Fragment {
    public static final String TAG = EmojiFragment.class.getName();
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_contact, container, false);

        return view;

    }
}
