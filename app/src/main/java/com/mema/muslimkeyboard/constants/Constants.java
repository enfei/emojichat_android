package com.mema.muslimkeyboard.constants;

import android.text.TextUtils;

/**
 * Created by cano-13 on 12/4/17.
 */

public class Constants {

    public static int ADS_DELAY_MIN = 15;
    public static Integer nEmojis00 = 32;
    public static Integer nEmojis10 = 22;
    public static Integer nEmojis20 = 22;
    public static Integer nEmojis30 = 22;
    public static Integer nEmojis40 = 22;
    public static Integer nEmojis50 = 22;
    public static Integer nEmojis60 = 22;
    public static Integer nEmojis70 = 22;
    public static Integer nEmojis80 = 22;
    public static Integer nRows = 4;
    public static Integer nCols = 2;

    /*
   * This method is used to verify email
   * */
    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}
