package com.mema.muslimkeyboard.contacts;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

/**
 * Created by king on 12/10/2017.
 */

public class Base64Utils {

    public static String decode(String encodedText) {
        try {
            return new String(Base64.decode(encodedText, Base64.NO_WRAP), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String encode(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    public static String encode(String text) {
        return Base64.encodeToString(text.getBytes(), Base64.NO_WRAP);
    }

}
