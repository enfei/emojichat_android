package com.mema.muslimkeyboard.utility;

import android.app.Activity;
import android.util.Log;

import com.mema.muslimkeyboard.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.mema.muslimkeyboard.constants.Constants.ADS_DELAY_MIN;

/**
 * Created by cloudstream on 5/28/17.
 */

public class AdmobInterstitial {
    public static final String TAG = AdmobInterstitial.class.getSimpleName();
    private static ScheduledFuture loaderHandler;

    public static void loadInterstitial(final Activity activity) {
        final Runnable loader = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Loading Admob interstitial...");
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final InterstitialAd interstitial = new InterstitialAd(activity);
                        interstitial.setAdUnitId(activity.getString(R.string.ads_unit_id));
                        AdRequest adRequest = new AdRequest.Builder().addTestDevice("E9958775B0D82756FBE4013CF0B54B0A").build();
                        interstitial.loadAd(adRequest);
                        interstitial.setAdListener(new AdListener() {
                            public void onAdLoaded() {
                                displayInterstitial(interstitial);
                            }
                        });
                    }
                });
            }
        };

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        loaderHandler = scheduler.scheduleWithFixedDelay(loader, ADS_DELAY_MIN * 60, ADS_DELAY_MIN * 60, TimeUnit.SECONDS);
//        loaderHandler = scheduler.scheduleWithFixedDelay(loader, 60, 60, TimeUnit.SECONDS);
    }

    private static void displayInterstitial(final InterstitialAd interstitialAd) {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }
}
