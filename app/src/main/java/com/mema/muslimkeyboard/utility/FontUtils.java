package com.mema.muslimkeyboard.utility;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;

//Font Style change
public class FontUtils {
    public static final String AvenirLTStdBook = "AvenirLTStdBook.otf";
    public static final String latoregular = "latoregular.ttf";
    private static final HashMap<String, Typeface> fonts = new HashMap<String, Typeface>();

    public static void setFont(TextView view, String name) {
        if (fonts.get(name) == null) {
            fonts.put(name, getFont(view.getContext(), name));
        }
        view.setTypeface(fonts.get(name));
    }

    public static void setFont(ViewGroup group, String font) {
        int count = group.getChildCount();
        View v;
        for (int i = 0; i < count; i++) {
            v = group.getChildAt(i);
            if (v instanceof TextView || v instanceof EditText || v instanceof Button || v instanceof CheckBox) {
                ((TextView) v).setTypeface(getFont(group.getContext(), font));
            } else if (v instanceof ViewGroup)
                setFont((ViewGroup) v, font);
        }
    }

    public static Typeface getFont(Context context, String name) {
        return Typeface.createFromAsset(context.getAssets(), name);
    }
}
