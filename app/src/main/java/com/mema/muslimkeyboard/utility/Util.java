package com.mema.muslimkeyboard.utility;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.mema.muslimkeyboard.bean.User;
import com.mema.muslimkeyboard.contacts.SyncableContact;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.MultiCallback;


/**
 * Created by user on 7/9/16.
 */

public class Util {
    public static Util mRefrences = null;
    public static ArrayList<ArrayList> arrEmojis = new ArrayList<ArrayList>();
    private static HashMap<String, Integer> emoticons = new HashMap<String, Integer>();
    public ArrayList<User> workingFriends = new ArrayList<>();
    public ArrayList<User> workingGroupMember = new ArrayList<>();
    public ArrayList<User> selFriends = new ArrayList<>();
    Dialog dialog;

    public static void initEmoticanMap(Context cont) {
        ArrayList<ArrayList> arrEmojiCounts;
        arrEmojiCounts = new ArrayList<ArrayList>();

        ArrayList<Integer> arr0 = new ArrayList<Integer>();
        arr0.add(GlobalScope.nEmojiSub00);

        arrEmojiCounts.add(arr0);

        ArrayList<Integer> arr1 = new ArrayList<Integer>();
        arr1.add(GlobalScope.nEmojiSub10);
        arr1.add(GlobalScope.nEmojiSub11);
        arr1.add(GlobalScope.nEmojiSub12);
        arr1.add(GlobalScope.nEmojiSub13);
        arr1.add(GlobalScope.nEmojiSub14);

        arrEmojiCounts.add(arr1);

        ArrayList<Integer> arr2 = new ArrayList<Integer>();
        arr2.add(GlobalScope.nEmojiSub20);

        arrEmojiCounts.add(arr2);

        ArrayList<Integer> arr3 = new ArrayList<Integer>();
        arr3.add(GlobalScope.nEmojiSub30);

        arrEmojiCounts.add(arr3);

        ArrayList<Integer> arr4 = new ArrayList<Integer>();
        arr4.add(GlobalScope.nEmojiSub40);
        arr4.add(GlobalScope.nEmojiSub41);
        arr4.add(GlobalScope.nEmojiSub42);
        arr4.add(GlobalScope.nEmojiSub43);

        arrEmojiCounts.add(arr4);

        ArrayList<Integer> arr5 = new ArrayList<Integer>();
        arr5.add(GlobalScope.nEmojiSub50);
        arr5.add(GlobalScope.nEmojiSub51);
        arr5.add(GlobalScope.nEmojiSub52);

        arrEmojiCounts.add(arr5);

        ArrayList<Integer> arr6 = new ArrayList<Integer>();
        arr6.add(GlobalScope.nEmojiSub60);
        arr6.add(GlobalScope.nEmojiSub61);

        arrEmojiCounts.add(arr6);

        ArrayList<Integer> arr7 = new ArrayList<Integer>();
        arr7.add(GlobalScope.nEmojiSub70);

        arrEmojiCounts.add(arr7);

        ArrayList<Integer> arr8 = new ArrayList<Integer>();
        arr8.add(GlobalScope.nEmojiSub80);

        arrEmojiCounts.add(arr8);

        for (int i = 0; i < arrEmojiCounts.size(); i++) {
            ArrayList<String> emojiSubAry = new ArrayList<>();
            ArrayList<Integer> subAry = arrEmojiCounts.get(i);
            for (int j = 0; j < subAry.size(); j++) {
                for (int k = 0; k < subAry.get(j); k++) {
                    String emojiStr = String.format("emoji_%d_%d_%d", i, j, k + 1);
                    String emojiKey = String.format("(emoji_%d_%d_%d)", i, j, k + 1);

                    emojiSubAry.add(emojiKey.substring(1, emojiKey.length() - 1));
                    int res = cont.getResources().getIdentifier(emojiStr, "drawable", cont.getPackageName());
                    if ((i != 0) && (i != 1)) {
                        res = cont.getResources().getIdentifier(emojiStr.replace("emoji_", "keyboard_"), "drawable", cont.getPackageName());
                    }
                    emoticons.put(emojiKey, res);
                }
            }
            arrEmojis.add(emojiSubAry);
        }
    }

    public static Util getInstance() {
        if (null == mRefrences)
            mRefrences = new Util();
        return mRefrences;
    }

    public static int emojiCountForCategory(int category) {
        int nCount = 0;
        switch (category) {
            case 0:
                nCount = GlobalScope.nEmojiSub00;
                break;
            case 1:
                nCount = GlobalScope.nEmojiSub10 + GlobalScope.nEmojiSub11 + GlobalScope.nEmojiSub12 + GlobalScope.nEmojiSub13 + GlobalScope.nEmojiSub14;
                break;
            case 2:
                nCount = GlobalScope.nEmojiSub20;
                break;
            case 3:
                nCount = GlobalScope.nEmojiSub30;
                break;
            case 4:
                nCount = GlobalScope.nEmojiSub40 + GlobalScope.nEmojiSub41 + GlobalScope.nEmojiSub42 + GlobalScope.nEmojiSub43;
                break;
            case 5:
                nCount = GlobalScope.nEmojiSub50 + GlobalScope.nEmojiSub51 + GlobalScope.nEmojiSub52;
                break;
            case 6:
                nCount = GlobalScope.nEmojiSub60 + GlobalScope.nEmojiSub61;
                break;
            case 7:
                nCount = GlobalScope.nEmojiSub70;
                break;
            case 8:
                nCount = GlobalScope.nEmojiSub80;
                break;
        }
        return nCount;
    }

    public static boolean bIsOnlyEmoji(String s) {
        String temp = s;
        while (temp.length() >= 13) {
            try {
                int startIndex = temp.indexOf("(emoji_");
                if (startIndex == -1) {
                    return false;
                }

                int endIndex = temp.substring(startIndex).indexOf(")");
                if (endIndex == -1) {
                    return false;
                }

                if (!bIsEmojiString(temp.substring(startIndex, endIndex + 1))) {
                    return false;
                }

                temp = temp.substring(endIndex + 1);
            } catch (Exception e) {
                return false;
            }
        }

        if (temp.equals("")) {
            return true;
        }

        return false;
    }

    public static boolean bIsEmojiString(String s) {
        if ((s.charAt(0) == '(') && (s.charAt(s.length() - 1) == ')') && (s.contains("emoji_"))) {
            String split[] = s.split("_");
            if (split.length == 4) {
                return true;
            }
        }

        return false;
    }

    public static SpannableStringBuilder getEmoticanText(Context context, String s, MultiCallback callback, boolean bLastMessage) {
        int index;
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(s);

        boolean bOnlyEmoji = bIsOnlyEmoji(s);
        int emojiSize = 200;

        for (index = 0; index < builder.length(); index++) {
            for (Map.Entry<String, Integer> entry : emoticons.entrySet()) {
                int length = entry.getKey().length();
                if (index + length > builder.length())
                    continue;

                if (builder.subSequence(index, index + length).toString()
                        .equals(entry.getKey())) {

                    if (entry.getKey().substring(0, 8).equalsIgnoreCase("(emoji_0") || entry.getKey().substring(0, 8).equalsIgnoreCase("(emoji_1")) { //If cagtegory is 0 or 1
                        GifDrawable gifFromResource = null;
                        try {
                            gifFromResource = new GifDrawable(context.getResources(), entry.getValue());
                            if (bOnlyEmoji && !bLastMessage)
                                gifFromResource.setBounds(0, 0, 240, 240);
                            else
                                gifFromResource.setBounds(0, 0, 50, 50);
                            ImageSpan imageSpan = new ImageSpan(gifFromResource, ImageSpan.ALIGN_BASELINE);
                            builder.setSpan(imageSpan,
                                    index, index + length,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            gifFromResource.setCallback(callback);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Drawable imgDrawable = context.getResources().getDrawable(entry.getValue());

                        if (bOnlyEmoji && !bLastMessage) {
                            String tmp = entry.getKey().substring(1, entry.getKey().length() - 1);
                            Log.e("Resource Name:", tmp);
                            int emojiRes = context.getResources().getIdentifier(entry.getKey().substring(1, entry.getKey().length() - 1), "drawable", context.getPackageName());
                            imgDrawable = context.getResources().getDrawable(emojiRes);
                            imgDrawable.setBounds(0, 0, emojiSize, emojiSize);
                        } else
                            imgDrawable.setBounds(0, 0, 50, 50);

                        ImageSpan span = new ImageSpan(imgDrawable, ImageSpan.ALIGN_BASELINE);
                        builder.setSpan(span,
                                index, index + length,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                    index += length - 1;
                    break;
                }
            }
        }
        return builder;
    }

    public static void hideSoftInputKeyboard(Activity activity) {
        try {
            if (activity.getCurrentFocus() != null) {
                InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JSONObject pushJsonObjectFrom(String title, String body, ArrayList<String> occTokens) {
        JSONObject notif = new JSONObject();
        JSONObject jsonObjects = new JSONObject();
        try {
            notif.put("title", "");
            notif.put("body", "Test");
            jsonObjects.put("notification", notif);
            JSONArray targetJson = new JSONArray(occTokens);
            jsonObjects.put("registration_ids", targetJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObjects;
    }

    public static JSONObject pushJsonObjectFrom(String title, String body, String partnerToken) {
        JSONObject notif = new JSONObject();
        JSONObject jsonObjects = new JSONObject();
        try {
            notif.put("title", "");
            notif.put("body", body);
            notif.put("badge", 1);
            jsonObjects.put("notification", notif);
            jsonObjects.put("to", partnerToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObjects;
    }

    public static int getWidthOfScreen(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public static int getHeightOfScreen(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public boolean isSelectedFriend(User user) {
        for (User friend : workingFriends) {
            if (user.id.equals(friend.id)) {
                return true;
            }
        }

        return false;
    }

    public void addFriend(User user) {
        workingFriends.add(user);
    }

    public void removeFriend(User user) {
        for (User friend : workingFriends) {
            if (user.id.equals(friend.id)) {
                workingFriends.remove(friend);
                break;
            }
        }
    }

    public ArrayList<SyncableContact> workingContacts = new ArrayList<>();

    public void addContact(SyncableContact contact) {
        workingContacts.add(contact);
    }

    public void removeContact(SyncableContact contact) {
        for (SyncableContact sc: workingContacts) {
            if (sc.contactId == contact.contactId) {
                workingContacts.remove(contact);
                break;
            }
        }
    }

    public boolean isSelectedContact(SyncableContact contact) {
        for (SyncableContact sc: workingContacts) {
            if (sc.contactId == contact.contactId) {
                return true;
            }
        }
        return false;
    }

    /*
   Custom method to get image uri from bitmap
    */
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
            return Uri.parse(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String normalizePhone(String phone) {
        StringBuilder sb = new StringBuilder();
        boolean addLeadingPlus = true;
        for (int i = 0; i < phone.length(); i ++) {
            char c = phone.charAt(i);
            if (Character.isDigit(c)) {
                sb.append(c);
            } else if (addLeadingPlus && c == '+') {
                if (sb.length() == 0) {
                    sb.append(c);
                }
                addLeadingPlus = false;
            }
        }

        String s = sb.toString();

        return s;
    }

}
