package com.mema.muslimkeyboard.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.activity.AddUserActivity;
import com.mema.muslimkeyboard.activity.ChatActivity;
import com.mema.muslimkeyboard.activity.FriendActivity;
import com.mema.muslimkeyboard.activity.HomeActivity;
import com.mema.muslimkeyboard.activity.group.GroupChatActivity;
import com.mema.muslimkeyboard.adapter.ChatListAdapter;
import com.mema.muslimkeyboard.bean.Dialog;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.mema.muslimkeyboard.utility.models.FirebaseValueListener;

import java.util.Locale;

/**
 * Created by cano-02 on 5/4/17.
 */

public class ChatListFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {
    public static final String TAG = ChatListFragment.class.getName();

    View view;
    LinearLayout actionMenuLayout;
    View maskView;
    SwipeMenuListView listView;
    ChatListAdapter chatListAdapter;
    FirebaseValueListener valueListener;
    EditText edtSearch;
    ImageView ivCancelSearch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.chat_list_fargment, container, false);

        initView();
        getDialogHistory();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (valueListener != null) {
            valueListener.removeListener();
        }
    }

    private void initView() {

        actionMenuLayout = (LinearLayout) view.findViewById(R.id.actionMenuLayout);
        maskView = view.findViewById(R.id.maskView);

        edtSearch = (EditText) view.findViewById(R.id.edt_search);

        listView = (SwipeMenuListView) view.findViewById(R.id.chat_list);

        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        View empty = view.findViewById(R.id.emptyView);
        listView.setEmptyView(empty);
        chatListAdapter = new ChatListAdapter(getActivity().getApplicationContext());
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard();
                return false;
            }
        });


        ivCancelSearch = (ImageView) view.findViewById(R.id.img_cancel_search);
        ivCancelSearch.setVisibility(View.INVISIBLE);

        ivCancelSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtSearch.setText("");
            }
        });

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // Create different menus depending on the view type
//                SwipeMenuItem readItem = new SwipeMenuItem(
//                        getActivity().getApplicationContext());
//                // set item background
//                readItem.setBackground(new ColorDrawable(Color.rgb(0xaa, 0xaa,
//                        0xaa)));
//                // set item width
//                readItem.setWidth(dp2px(90));
//                // set a icon
//                readItem.setIcon(R.mipmap.icon_item_read);
//                // add to menu
//                menu.addMenuItem(readItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getActivity().getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xED,
                        0x46, 0x37)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(R.mipmap.icon_item_remove);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        listView.setMenuCreator(creator);

        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                Dialog dialog = FirebaseManager.getInstance().dialogList.get(position);
                switch (index) {
                    case 0:
                        FirebaseManager.getInstance().removeDialog(dialog);
                        FirebaseManager.getInstance().dialogList.remove(position);
                        chatListAdapter.notifyDataSetChanged();
                        updateTabBadgeNumber();
                        break;
                }
                return true;
            }
        });

        listView.setAdapter(chatListAdapter);
        listView.setOnItemClickListener(this);

        //mListView

        listView.setOnMenuStateChangeListener(new SwipeMenuListView.OnMenuStateChangeListener() {
            @Override
            public void onMenuOpen(int position) {

            }

            @Override
            public void onMenuClose(int position) {

            }
        });

        listView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int position) {

            }

            @Override
            public void onSwipeEnd(int position) {

            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
                String text = edtSearch.getText().toString().toLowerCase(Locale.getDefault());
                chatListAdapter.filter(text);
                if (TextUtils.isEmpty(text))
                    ivCancelSearch.setVisibility(View.INVISIBLE);
                else
                    ivCancelSearch.setVisibility(View.VISIBLE);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
            }
        });

        maskView.setOnClickListener(this);
        view.findViewById(R.id.img_add).setOnClickListener(this);
        view.findViewById(R.id.new_chat).setOnClickListener(this);
        view.findViewById(R.id.add_friends).setOnClickListener(this);
        view.findViewById(R.id.group_chat).setOnClickListener(this);
    }

    private void reloadListItem() {
        chatListAdapter.notifyDataSetChanged();
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
    }

    private void getDialogHistory() {
        valueListener = FirebaseManager.getInstance().addDialogListener(new FirebaseManager.OnUpdateListener() {
            @Override
            public void onUpdate() {
                Log.e("DialogList:", String.valueOf(FirebaseManager.getInstance().dialogList.size()));
                String text = edtSearch.getText().toString().toLowerCase(Locale.getDefault());
                chatListAdapter.filter(text);
                updateTabBadgeNumber();
            }
        });
    }

    private void updateTabBadgeNumber() {
        HomeActivity homeActivity = (HomeActivity) getActivity();
        if (homeActivity != null) {
            homeActivity.updateChatBadge();
        }
    }

    private void showActionMenu() {
        actionMenuLayout.setVisibility(View.VISIBLE);
        maskView.setVisibility(View.VISIBLE);
    }

    private void hideActionMenu() {
        actionMenuLayout.setVisibility(View.GONE);
        maskView.setVisibility(View.GONE);
    }

    /**
     * This method is used for Handling all the click events
     *
     * @param v
     */

    @Override
    public void onClick(View v) {
        hideSoftKeyboard();
        switch (v.getId()) {
            case R.id.img_add:
                showActionMenu();
                break;
            case R.id.maskView:
                hideActionMenu();
                break;
            case R.id.new_chat:
                hideActionMenu();

                Intent intent1 = new Intent(getActivity(), FriendActivity.class);
                startActivity(intent1);
                break;
            case R.id.add_friends:
                hideActionMenu();

                Intent intent2 = new Intent(getActivity(), AddUserActivity.class);
                startActivity(intent2);

                break;
            case R.id.group_chat:
                hideActionMenu();

                Intent intent3 = new Intent(getActivity(), GroupChatActivity.class);
                startActivity(intent3);

                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        hideSoftKeyboard();
        Dialog dialog = (Dialog) chatListAdapter.getItem(position);
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("dialog", dialog);
        startActivity(intent);
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}