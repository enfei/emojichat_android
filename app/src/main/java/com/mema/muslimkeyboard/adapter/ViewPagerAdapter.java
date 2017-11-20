package com.mema.muslimkeyboard.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.mema.muslimkeyboard.constants.Constants;
import com.mema.muslimkeyboard.utility.Util;

import java.util.ArrayList;

/**
 * Created by cloudstream on 6/2/17.
 */

public class ViewPagerAdapter extends PagerAdapter {
    private final Context context;
    private int categoryIndex = 0;
    private ArrayList<EmojiAdapter> adapterAry = new ArrayList<>();
//    private ArrayList<GridView> gridViews = new ArrayList<>();

    public ViewPagerAdapter(Context context) {
        this.context = context;
        initAdapters();
    }

    @Override
    public int getCount() {
        Log.e("Cloud", "PageCount=" + calcPageCount(categoryIndex));
        return calcPageCount(categoryIndex);
    }

    public void initAdapters() {
        adapterAry.clear();
        int pgCount = getCount();
        for (int i = 0; i < pgCount; i++) {
            adapterAry.add(new EmojiAdapter(context, categoryIndex, i));
        }
    }

    public void refreshData(int catIndex) {
        if (categoryIndex == catIndex)
            return;

        categoryIndex = catIndex;
        initAdapters();

        notifyDataSetChanged();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        GridView gridView = null;

        gridView = new GridView(context);
        gridView.setTag(position);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        gridView.setLayoutParams(params);
        if (categoryIndex == 1)
            gridView.setNumColumns(7);
        else
            gridView.setNumColumns(4);

        gridView.setGravity(Gravity.CENTER);
        gridView.setAdapter(adapterAry.get(position));
        gridView.setTag("GridView" + position);

        container.addView(gridView);

        return gridView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        object = null;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals((View) object);
    }

    public int calcPageCount(int categoryIndex) {
        Integer totalCnt = Util.emojiCountForCategory(categoryIndex);
        Integer cntAScreen = Constants.nRows * Constants.nCols;

        if (categoryIndex == 1)
            cntAScreen = 3 * 7;

        int pageCnt = totalCnt / (cntAScreen);
        if (totalCnt % (cntAScreen) != 0)
            pageCnt++;

        Log.e("Cloud", String.valueOf(pageCnt));
        return pageCnt;
    }
}
