package com.mema.muslimkeyboard.utility;

import android.app.Application;
import android.graphics.Typeface;

public class GlobalScope extends Application {
    //	public static com.dev.wangri.model.Preferences preferences;
    public static Typeface font;

    public static Integer nEmojiSub00 = 32;

    public static Integer nEmojiSub10 = 51;
    public static Integer nEmojiSub11 = 40;
    public static Integer nEmojiSub12 = 50;
    public static Integer nEmojiSub13 = 50;
    public static Integer nEmojiSub14 = 37;

    public static Integer nEmojiSub20 = 36;

    public static Integer nEmojiSub30 = 45;

    public static Integer nEmojiSub40 = 54;
    public static Integer nEmojiSub41 = 24;
    public static Integer nEmojiSub42 = 53;
    public static Integer nEmojiSub43 = 60;

    public static Integer nEmojiSub50 = 50;
    public static Integer nEmojiSub51 = 18;
    public static Integer nEmojiSub52 = 29;

    public static Integer nEmojiSub60 = 21;
    public static Integer nEmojiSub61 = 16;

    public static Integer nEmojiSub70 = 63;
    public static Integer nEmojiSub80 = 22;

    public static Integer nCatgories = 8;

    public static int CATEGORY_DIVIDOR = 1000;

    public static int SIZEOFIPHONE6 = 736;
    public static int SIZEOFIPHONE6PLUS = 667;
    public static int SIZEOFIPHONE5 = 568;
    public static int SIZEOFIPHONE4 = 480;

    public static int colorTransparent;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
