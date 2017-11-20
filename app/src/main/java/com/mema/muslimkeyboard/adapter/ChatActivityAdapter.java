package com.mema.muslimkeyboard.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.activity.ChatActivity;
import com.mema.muslimkeyboard.bean.Message;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.mema.muslimkeyboard.utility.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import pl.droidsonroids.gif.MultiCallback;

/**
 * Created by cano-02 on 6/4/17.
 */

public class ChatActivityAdapter extends BaseAdapter {
    static MultiCallback multiCallback = new MultiCallback(true);
    Context context;
    LayoutInflater inflter;
    private ArrayList<Message> arraylist = new ArrayList<>();

    public ChatActivityAdapter(Context applicationContext, ArrayList<Message> arraylist1) {
        this.context = applicationContext;
        this.arraylist = arraylist1;
        inflter = (LayoutInflater.from(applicationContext));
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflter.inflate(R.layout.activity_chat_item, null);
        }

        view.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.e("ListView", "ActionDown");
                        ChatActivity.getInstance().hideKeyboards();
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.e("ListView", "ActionUp");
//                        v.setBackground(view.getResources().getDrawable(R.drawable.border));
                        v.performClick();
                        break;
                    case MotionEvent.ACTION_CANCEL:
//                        v.setBackground(view.getResources().getDrawable(R.drawable.border));
                        Log.e("ListView", "ActionCancel");
                        break;
                }
                return false;
            }
        });

        RelativeLayout textLayoutRight = (RelativeLayout) view.findViewById(R.id.sender_text_layout);
        RelativeLayout textLayoutLeft = (RelativeLayout) view.findViewById(R.id.receiver_text_layout);
        RelativeLayout imgLayoutLeft = (RelativeLayout) view.findViewById(R.id.receiver_image_layout);
        RelativeLayout imgLayoutRight = (RelativeLayout) view.findViewById(R.id.sender_image_layout);

        final Holder holder = new Holder();
        holder.textViewSender = (TextView) view.findViewById(R.id.text_view_sender);
        holder.textViewReceiver = (TextView) view.findViewById(R.id.text_view_receiver);
        holder.emojiViewSender = (ImageView) view.findViewById(R.id.sender_imoji_icon);
        holder.emojiViewwReceiver = (ImageView) view.findViewById(R.id.receiver_imoji_icon);
        holder.imageViewSender = (ImageView) view.findViewById(R.id.sender_image);
        holder.imageViewReceiver = (ImageView) view.findViewById(R.id.receiver_image);
        holder.textViewSenderTime = (TextView) view.findViewById(R.id.text_view_sender_time);
        holder.textViewReceiverTime = (TextView) view.findViewById(R.id.text_view_receiver_time);
        holder.textViewEmojiSenderTime = (TextView) view.findViewById(R.id.text_imageView_sender_time);
        holder.textViewEmojiReceiverTime = (TextView) view.findViewById(R.id.text_imageView_receiver_time);
        holder.imageViewReceiverProfile = (ImageView) view.findViewById(R.id.profile_image_receiver);
        holder.imageViewEmojiReceiverProfile = (ImageView) view.findViewById(R.id.profile_imageView_receiver);
        Message message = arraylist.get(i);
//        multiCallback.addView(view);
        if (message.type == Message.MessageType.Text) {

            String msg = message.message;
            if (msg.contains("keyboard_")) {
                msg = msg.replace("keyboard_", "emoji_");
            }
            if (message.userID.equals(FirebaseManager.getInstance().getCurrentUserID())) {
                textLayoutLeft.setVisibility(View.GONE);
                textLayoutRight.setVisibility(View.VISIBLE);
                imgLayoutLeft.setVisibility(View.GONE);
                imgLayoutRight.setVisibility(View.GONE);
                multiCallback.addView(holder.textViewSender);
                holder.textViewSender.setText(Util.getEmoticanText(context, msg, multiCallback, false));
                if (Util.bIsOnlyEmoji(msg)) {
                    holder.textViewSender.setBackground(null);
                }
                holder.textViewSenderTime.setText(DateUtils.formatDateTime(context, message.dateSent * 1000, DateUtils.FORMAT_SHOW_TIME));
            } else {
                textLayoutRight.setVisibility(View.GONE);
                textLayoutLeft.setVisibility(View.VISIBLE);
                imgLayoutLeft.setVisibility(View.GONE);
                imgLayoutRight.setVisibility(View.GONE);
                multiCallback.addView(holder.textViewReceiver);
                holder.textViewReceiver.setText(Util.getEmoticanText(context, msg, multiCallback, false));
                if (Util.bIsOnlyEmoji(msg)) {
                    holder.textViewReceiver.setBackground(null);
                }
                holder.textViewReceiverTime.setText(DateUtils.formatDateTime(context, message.dateSent * 1000, DateUtils.FORMAT_SHOW_TIME));
            }
        } else if (message.type == Message.MessageType.Emoji) {
            int resID = context.getResources().getIdentifier(message.message, "drawable", context.getPackageName());

            if (message.userID.equals(FirebaseManager.getInstance().getCurrentUserID())) {
                textLayoutLeft.setVisibility(View.GONE);
                textLayoutRight.setVisibility(View.GONE);
                imgLayoutLeft.setVisibility(View.GONE);
                imgLayoutRight.setVisibility(View.VISIBLE);
                holder.imageViewSender.setVisibility(View.GONE);
                holder.emojiViewSender.setVisibility(View.VISIBLE);
                holder.emojiViewSender.setImageResource(resID);
                holder.textViewEmojiSenderTime.setText(DateUtils.formatDateTime(context, message.dateSent * 1000, DateUtils.FORMAT_SHOW_TIME));
            } else {
                textLayoutLeft.setVisibility(View.GONE);
                textLayoutRight.setVisibility(View.GONE);
                imgLayoutLeft.setVisibility(View.VISIBLE);
                imgLayoutRight.setVisibility(View.GONE);
                holder.imageViewReceiver.setVisibility(View.GONE);
                holder.emojiViewwReceiver.setVisibility(View.VISIBLE);
                holder.emojiViewwReceiver.setImageResource(resID);
                holder.textViewEmojiReceiverTime.setText(DateUtils.formatDateTime(context, message.dateSent * 1000, DateUtils.FORMAT_SHOW_TIME));
            }

        } else if (message.type == Message.MessageType.Photo) {
            if (message.userID.equals(FirebaseManager.getInstance().getCurrentUserID())) {
                textLayoutLeft.setVisibility(View.GONE);
                textLayoutRight.setVisibility(View.GONE);
                imgLayoutLeft.setVisibility(View.GONE);
                imgLayoutRight.setVisibility(View.VISIBLE);
                holder.emojiViewSender.setVisibility(View.GONE);
                holder.imageViewSender.setVisibility(View.VISIBLE);
                Picasso.with(context)
                        .load(message.message)
                        .into(holder.imageViewSender);
                holder.textViewEmojiSenderTime.setText(DateUtils.formatDateTime(context, message.dateSent * 1000, DateUtils.FORMAT_SHOW_TIME));
            } else {
                textLayoutLeft.setVisibility(View.GONE);
                textLayoutRight.setVisibility(View.GONE);
                imgLayoutLeft.setVisibility(View.VISIBLE);
                imgLayoutRight.setVisibility(View.GONE);
                holder.emojiViewwReceiver.setVisibility(View.GONE);
                holder.imageViewReceiver.setVisibility(View.VISIBLE);
                Picasso.with(context)
                        .load(message.message)
                        .into(holder.imageViewReceiver);
                holder.textViewEmojiReceiverTime.setText(DateUtils.formatDateTime(context, message.dateSent * 1000, DateUtils.FORMAT_SHOW_TIME));
            }
        }

        return view;
    }

    public static class Holder {
        TextView textViewSender, textViewReceiver;
        ImageView imageViewReceiverProfile, imageViewEmojiReceiverProfile;
        TextView textViewSenderTime, textViewReceiverTime, textViewEmojiSenderTime, textViewEmojiReceiverTime;
        ImageView imageViewReceiver, imageViewSender, emojiViewwReceiver, emojiViewSender;
    }
}



