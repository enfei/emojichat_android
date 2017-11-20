package com.mema.muslimkeyboard.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mema.muslimkeyboard.activity.ChatActivity;
import com.mema.muslimkeyboard.constants.Constants;
import com.mema.muslimkeyboard.utility.Util;

import java.util.ArrayList;

/**
 * Created by cloudstream on 6/3/17.
 */

public class EmojiAdapter extends BaseAdapter {
    private int categoryIndex, pageIndex;
    private ArrayList itemResAry = new ArrayList<>();
    private Context mContext;

    public EmojiAdapter(Context context, int cateIndex, int pgIndex) {
        this.categoryIndex = cateIndex;
        this.pageIndex = pgIndex;
        this.mContext = context;
        itemResAry = Util.arrEmojis.get(this.categoryIndex);
    }

    @Override
    public int getCount() {
        int totalCnt = Util.emojiCountForCategory(categoryIndex);

        Integer cntAScreen = Constants.nRows * Constants.nCols;

        if (categoryIndex == 1)
            cntAScreen = 3 * 7;

        int temp = totalCnt - pageIndex * cntAScreen;
        if (temp >= (cntAScreen))
            return cntAScreen;
        else
            return temp;
    }

    @Override
    public Object getItem(int position) {
        Integer cntAScreen = Constants.nRows * Constants.nCols;

        if (categoryIndex == 1)
            cntAScreen = 3 * 7;
        return itemResAry.get(cntAScreen * this.pageIndex + position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout layout = new LinearLayout(mContext);

        int rows = Constants.nRows;
        int cols = Constants.nCols;

        if (categoryIndex == 1) {
            rows = 7;
            cols = 3;
        }

        int width = (int) ((Util.getInstance().getWidthOfScreen(mContext) - Util.getInstance().pxFromDp(mContext, 5) * (rows + 1)) / ((float) rows));
        int height = (parent.getHeight()) / cols;

        layout.setLayoutParams(new GridView.LayoutParams(
                width,
                height));

        layout.setOrientation(LinearLayout.HORIZONTAL);
        Log.e("Cloud", String.valueOf(getItem(position)));

        String emojiStr = String.valueOf(getItem(position));

        int res = mContext.getResources().getIdentifier(emojiStr.replace("emoji_", "keyboard_"), "drawable", mContext.getPackageName());
        layout.setTag(emojiStr);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ChatActivity.getInstance().emojiButtonClicked(v.getTag().toString());
            }
        });
        ImageView ivEmoji = new ImageView(mContext);
        ivEmoji.setImageResource(res);
        ivEmoji.setLayoutParams(new LinearLayout.LayoutParams(
                width,
                height));

        layout.addView(ivEmoji);

        return layout;
    }
}
