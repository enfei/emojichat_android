package com.mema.muslimkeyboard.contacts;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.os.OperationCanceledException;
import android.util.Log;

import com.mema.muslimkeyboard.bean.User;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.mema.muslimkeyboard.utility.ResultListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by king on 11/10/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class SyncContactService extends IntentService {

    private static final String TAG = "SyncContactService";
    private static final String LAST_SYNC = "last_sync";
    private static final String AUTHORITY = "com.dev.wangri.muslimkeyboard.provider";

    private static final AtomicBoolean inProgress = new AtomicBoolean(false);
    private static PostService postService;
    public static void requestSyncContacts(Context context) {
        Intent intent = new Intent(context, SyncContactService.class);
        context.startService(intent);
    }

    private SharedPreferences prefs;
    public static final long DEFAULT_SYNC_CONTACTS_PERIOD = 180;

    public SyncContactService() {
        super("SyncContactService");
    }

    public static boolean isTimeToSync(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return isTimeToSync(prefs);
    }
    private static boolean isTimeToSync(SharedPreferences prefs) {
        long prevSync = System.currentTimeMillis() - prefs.getLong(LAST_SYNC, 0);
        return TimeUnit.MILLISECONDS.toSeconds(prevSync) > SyncContactService.DEFAULT_SYNC_CONTACTS_PERIOD;
    }

    public static boolean syncIsInProgress() {
        return inProgress.get();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());

        if (syncIsInProgress()) {
            Log.i(TAG, "Reject to perform sync coz sync in progress");
            return;
        }

        prefs.edit().putLong(LAST_SYNC, System.currentTimeMillis()).apply();

        inProgress.set(true);

        Log.i(TAG, "Performing sync");

        try {
            syncContacts();
        } catch (Exception e) {
            throw new RuntimeException("Failed to sync", e);
        } finally {
            inProgress.set(false);
            PostService.closeDialog();
        }
    }

    public void syncContacts() throws OperationCanceledException, RemoteException, OperationApplicationException {

        Log.d(TAG, "started syncContacts");

        Collection<SyncableContact> contacts = ContactManager.syncContactbook(this);

        Log.d(TAG, "syncContactbook result = " + contacts.size());

        syncAppContacts(getContactPhones(contacts));

    }

    private List<String> getContactPhones(Collection<SyncableContact> contacts) {
        List<String> phoneList = new ArrayList<>();
        for (SyncableContact contact: contacts) {
            phoneList.addAll(contact.phones);
        }
        return phoneList;
    }

    public void syncAppContacts(final List<String> contactPhones) {
        FirebaseManager.getInstance().getAllUsers(new ResultListener() {
            @Override
            public void onResult(boolean isSuccess, String error, List data) {
                if (isSuccess) {
                    final List<User> allUsers = new ArrayList<User>(data);
                    FirebaseManager.getInstance().getFriendUserIds(new ResultListener() {
                        @Override
                        public void onResult(boolean isSuccess, String error, List data) {
                            List<String> friendIds;
                            ArrayList<User> friendList = new ArrayList<User>();
                            if (isSuccess) {
                                friendIds = data;
                            } else {
                                friendIds = new ArrayList<String>();
                            }

                            for (User user: allUsers) {
                                if (friendIds.contains(user.id)) {
                                    friendList.add(user);
                                } else {
                                    if (user.mobile != null && !user.mobile.isEmpty()) {
                                        if (contactPhones.contains(user.mobile.replace("+", ""))) {
                                            friendList.add(user);
                                            FirebaseManager.getInstance().acceptFriendRequest(FirebaseManager.getInstance().getCurrentUserID(), user.id);
                                        }
                                    }
                                }
                            }

                            // Send broadcast message for updating friends
                            Intent intent = new Intent();
                            intent.setAction("upadte_friends");
                            sendBroadcast(intent);

                        }
                    });
                }
            }
        });
    }

    private String makeAddressBook(Collection<SyncableContact> contacts)
            throws RemoteException {
        AddressBookBuilder abookBuilder = new AddressBookBuilder();

        for (SyncableContact sc : contacts) {

            if (sc.name == null || sc.name.isEmpty())
                continue;

            if (sc.phones == null || sc.phones.size() == 0)
                continue;

            abookBuilder.addEntryPhone(sc.name, sc.phones);

        }

        return abookBuilder.build();
    }

    public static void clearCacheInfo(Context context, boolean keepData) {
        ContactManager.deleteContactBookCacheInfo(context, keepData);
    }

    public static void saveCacheInfo(Context context) {
    }

    public static void loadCacheInfo(Context context) {
    }
}
