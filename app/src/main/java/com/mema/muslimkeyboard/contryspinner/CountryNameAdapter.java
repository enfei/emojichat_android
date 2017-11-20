package com.mema.muslimkeyboard.contryspinner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.utility.FontUtils;

import java.util.List;

class CountryNameAdapter extends BaseAdapter {
    private List<String> countryNames;
    private LayoutInflater inflter;

    CountryNameAdapter(Context applicationContext, List<String> countryNames) {
        this.countryNames = countryNames;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return countryNames.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.custom_spinner_items, null);
        TextView names = (TextView) view.findViewById(R.id.txtContryName);
        FontUtils.setFont(names, FontUtils.AvenirLTStdBook);
        names.setText(countryNames.get(i));
        return view;
    }
}