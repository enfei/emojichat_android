package com.mema.muslimkeyboard.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * TO check if Internet connection is available or not.
 *
 * @author Canopus
 */
public class NetworkAvailablity {

    public static NetworkAvailablity mRefrence = null;

    public static NetworkAvailablity getInstance() {
        if (null == mRefrence)
            mRefrence = new NetworkAvailablity();
        return mRefrence;
    }

    /**
     * Check network availability
     */
    public boolean checkNetworkStatus(Context context) {
        try {
            boolean HaveConnectedWifi = false;
            boolean HaveConnectedMobile = false;
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (NetworkInfo ni : netInfo) {
                if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                    if (ni.isConnected())
                        HaveConnectedWifi = true;
                if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (ni.isConnected())
                        HaveConnectedMobile = true;
            }
            if (!HaveConnectedWifi && !HaveConnectedMobile) {
                Toast.makeText(context, "Network lost. Please re-connect to your network and try again.", Toast.LENGTH_SHORT).show();
            }
            return HaveConnectedWifi || HaveConnectedMobile;
        } catch (Exception e) {
            return false;
        }
    }


}

