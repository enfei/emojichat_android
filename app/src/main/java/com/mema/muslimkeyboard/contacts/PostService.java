package com.mema.muslimkeyboard.contacts;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by king on 11/10/2017.
 */

@android.support.annotation.RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class PostService extends IntentService {

    private static final String TAG = "PostService";
    private static final long DEFAULT_INTERVAL = 1000;
    private static final long INTERVAL_INCREMENT = 1000;
    private static final long MAX_INTERVAL = 10000;
    private static final int EMPTY_POST_THRESHOLD = 3;

    private static final AtomicInteger mEmptyPosts = new AtomicInteger();
    private static final AtomicInteger mUsed = new AtomicInteger(0);
    private static final AtomicLong interval = new AtomicLong();

    private static boolean isNeedToSyncContactbook = false;
    private static ProgressDialog dialog = null;
    private static Context currentContext = null;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public PostService() {
        super(TAG);
        mEmptyPosts.set(0);
        interval.set(DEFAULT_INTERVAL);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    public static int register(Context context) {
        int n = mUsed.incrementAndGet();

        Log.i(TAG, "register - " + n + " - " + context.toString());
        if (n == 1) {
            scheduleOnce(context);

            if (isNeedToSyncContactbook) {
                performSyncContacts(context);
            } else {
                dialog = ProgressDialog.show(context, "", "Updating EmojiChat", true);
                timerDelayRemoveDialog(10000, dialog);
                SyncContactService.requestSyncContacts(context);
            }

            if (currentContext != null) {
                currentContext.unregisterReceiver(broadcastReceiver);
            }

            currentContext = context;

            IntentFilter filter = new IntentFilter();
            filter.addAction("ACTION_IMPORT");
            context.registerReceiver(broadcastReceiver, filter);
        }

        isNeedToSyncContactbook = false;

        return n;
    }

    public static void scheduleOnce(Context context) {
        scheduleOnce(context, 0);
    }

    public static void scheduleOnce(Context context, long interval) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Context appContext = context.getApplicationContext();
        final PendingIntent pi = PendingIntent.getService(appContext, 0, new Intent(appContext, PostService.class), PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + interval, pi);
    }

    public static int unregister() {
        int ret = mUsed.decrementAndGet();
        Log.i(TAG, "unregister - " + ret);
        if (currentContext != null) {
            currentContext.unregisterReceiver(broadcastReceiver);
            currentContext = null;
        }
        return ret;
    }

    public static boolean isUsed() {
        return mUsed.get() > 0;
    }

    public static void setNeedSyncContact(boolean need) {
        isNeedToSyncContactbook = need;
    }

    private static BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("ACTION_IMPORT")) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    dialog = null;
                }
            }
        }
    };

    public static void timerDelayRemoveDialog(long time, final ProgressDialog d) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (d != null && d.isShowing())
                    d.dismiss();
            }
        }, time);
    }

    public static void performSyncContacts(final Context context) {
        if (isSyncing())
            return;

        if (ContactManager.isContactBookChanged(context)) {
            dialog = ProgressDialog.show(context, "", "Contacts changed. Updating EmojiChat", true);
            timerDelayRemoveDialog(MAX_INTERVAL, dialog);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    SyncContactService.requestSyncContacts(context);
                }
            }).start();
        }
    }

    public static boolean isSyncing() {
        return dialog != null;
    }

    public static void closeDialog() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        dialog = null;
    }

}
