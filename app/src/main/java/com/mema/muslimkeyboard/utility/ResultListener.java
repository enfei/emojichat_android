package com.mema.muslimkeyboard.utility;

import java.util.List;

/**
 * Created by king on 12/10/2017.
 */

public interface ResultListener {
    public void onResult(boolean isSuccess, String error, List data);
}
