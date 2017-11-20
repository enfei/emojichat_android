package com.mema.muslimkeyboard.contacts;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import com.mema.muslimkeyboard.bean.User;
import com.mema.muslimkeyboard.utility.LocalAccountManager;
import com.mema.muslimkeyboard.utility.Util;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by king on 11/10/2017.
 */

public class ContactManager {
    private static final String TAG = "ContactManager";
    private static final Object mObject = new Object();

    private static String lookupContactDisplayName(ContentResolver resolver, String phone) {

        String displayName = null;
        final Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        final Cursor cursor = resolver.query(lookupUri, DisplayNameQuery.PROJECTION, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                displayName = cursor.getString(DisplayNameQuery.COLUMN_ID);
            }
        } finally {
            cursor.close();
        }
        Log.d(TAG, phone + " -> " + displayName);
        return displayName;
    }

    private static long lookupRawContact(ContentResolver resolver, long userId) {
        long id = 0;
        final Cursor cursor = resolver.query(ContactsContract.RawContacts.CONTENT_URI, UserIdQuery.PROJECTION, UserIdQuery.SELECTION, new String[] {String.valueOf(userId)}, null);

        try {
            if (cursor.moveToFirst()) {
                id = cursor.getLong(UserIdQuery.COLUMN_ID);
            }
        } finally {
            cursor.close();
        }
        return id;
    }

    private interface DisplayNameQuery {
        public static final String[] PROJECTION = new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME};
        public static final int COLUMN_ID = 0;
    }

    private interface UserIdQuery {
        public static final String[] PROJECTION = new String[] {ContactsContract.RawContacts._ID};
        public static final int COLUMN_ID = 0;
        public static final String SELECTION = ContactsContract.RawContacts.ACCOUNT_TYPE + "='" + LocalAccountManager.ACCOUNT_TYPE + "' AND " + ContactsContract.RawContacts.SOURCE_ID + "=?";
    }

    private interface ContactIdQuery {
        public static final String[] PROJECTION = new String[] {ContactsContract.PhoneLookup._ID};
        public static final int COLUMN_ID = 0;
    }

    private static HashMap<Integer, SyncableContact> gotContactBookInfo = null;
    private static ArrayList<SyncableContact> contactBookInfoWithoutFriends = null;
    private static HashMap<Integer, String> versionInfo = new HashMap<Integer, String>();
    private static List<Integer> deletedContact = new ArrayList<Integer>();
    private static List<Integer> updatedContact = new ArrayList<Integer>();

    public static void clearCacheInfo() {
        gotContactBookInfo = null;
        versionInfo.clear();
    }

    public static boolean isContactBookChanged(Context context) {

        synchronized (mObject){
            Log.i(TAG, "started isContactBookChanged");
            ContentResolver resolver = context.getContentResolver();

            if (gotContactBookInfo == null) {
                loadContactBookInfoFromFile(context);
            }

            Log.i(TAG, "versionInfo has " + versionInfo.size() + "version infos");

            HashMap<Integer, String> info = new HashMap<Integer, String>();
            deletedContact.clear();
            updatedContact.clear();


            Cursor c = resolver.query(ContactsContract.RawContacts.CONTENT_URI,
                    new String[]{ContactsContract.RawContacts._ID, ContactsContract.RawContacts.VERSION}, null, null, ContactsContract.RawContacts._ID);

            if (c != null) {

                c.moveToFirst();
                while (c.isAfterLast() == false) {
                    info.put(c.getInt(c.getColumnIndex(ContactsContract.RawContacts._ID)), c.getString(c.getColumnIndex(ContactsContract.RawContacts.VERSION)));
                    c.moveToNext();
                }

                c.close();
            }


            for (Integer key : info.keySet()) {
                if (!versionInfo.containsKey(key)) {
                    updatedContact.add(key);//added
                    continue;
                }

                if (!info.get(key).equals(versionInfo.get(key))) {
                    updatedContact.add(key);
                    continue;
                }
            }

            for (Integer key : versionInfo.keySet()) {
                if (!info.containsKey(key)) {
                    deletedContact.add(key);
                    continue;
                }
            }

            versionInfo = info;

            if (forceUpdatedContactId >= 0) {
                boolean contained = false;
                for (Integer key : updatedContact) {
                    if (key.equals(forceUpdatedContactId)) {
                        contained = true;
                        break;
                    }
                }

                if (!contained)
                    updatedContact.add(forceUpdatedContactId);
            }

            forceUpdatedContactId = -1;

            Log.i(TAG, String.format("ended isContactBookChanged (updated = %d, deleted = %d)", updatedContact.size(), deletedContact.size()));
            return !deletedContact.isEmpty() || !updatedContact.isEmpty();

        }

    }

    public static void setForceChangedMark(int contactId) {
        if (versionInfo.get(contactId) == null)
            return;
        versionInfo.put(contactId, "0");
    }

    private static void getFreshContactInfo(Context context, ContentResolver resolver) {

        if (gotContactBookInfo == null)
            gotContactBookInfo = new HashMap<Integer, SyncableContact>();
        else
            gotContactBookInfo.clear();

        Log.d(TAG, "start getcontactbook");

        Cursor cursor = resolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor != null) {

            if (cursor.getCount() > 0) {

                cursor.moveToFirst();

                do {

                    int contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    ArrayList phones = new ArrayList();
                    SyncableContact sc = new SyncableContact(contactId, displayName, phones);
                    gotContactBookInfo.put(contactId, sc);

                } while (cursor.moveToNext());

            }

            cursor.close();
        }

        Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        if (phoneCursor != null) {

            if (phoneCursor.getCount() > 0) {

                phoneCursor.moveToFirst();

                do {

                    int contactId = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    String phone = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phone = Util.normalizePhone(phone.replace(" ", ""));
                    phone = phone.replace("+", "");

                    if (gotContactBookInfo.containsKey(contactId)) {
                        SyncableContact sc = gotContactBookInfo.get(contactId);
                        sc.phones.add(phone);
                        gotContactBookInfo.put(contactId, sc);
                    }

                } while (phoneCursor.moveToNext());

            }

            phoneCursor.close();
        }

        Log.d(TAG, "end getcontactbook111");

    }

    public static Collection<SyncableContact> syncContactbook(Context context) {
        synchronized (mObject) {

            Log.i("contact", "synching contact");

            ContentResolver resolver = context.getContentResolver();

            if (gotContactBookInfo == null) {

                loadContactBookInfoFromFile(context);

                if (gotContactBookInfo == null) {
                    getFreshContactInfo(context, resolver);

                    deletedContact.clear();
                    updatedContact.clear();

                    isContactBookChanged(context);
                    saveContactBookInfoToFile(context);

                    return gotContactBookInfo.values();
                } else {
                    isContactBookChanged(context);
                }
            }

            for (Integer contactId : deletedContact) {
                gotContactBookInfo.remove(contactId);
            }

            for (Integer contactId : updatedContact) {
                gotContactBookInfo.remove(contactId);
            }

            ArrayList<SyncableContact>  ret = new ArrayList<SyncableContact>();
            for (Integer rawContactId : updatedContact) {

                String displayName = "";
                List<String> phones = new ArrayList<String>();
                List<String> emails = new ArrayList<String>();

                Cursor cur = resolver.query(ContactsContract.Contacts.CONTENT_URI, null,
                        ContactsContract.Contacts._ID + " = ?",
                        new String[]{String.valueOf(rawContactId)},
                        null);

                if (cur != null) {
                    if (cur.getCount() > 0) {
                        cur.moveToFirst();
                        displayName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    }
                    cur.close();
                }

                Cursor pCur = resolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{
                                ContactsContract.CommonDataKinds.Phone.NUMBER,
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                        },
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{String.valueOf(rawContactId)}, null);

                if (pCur != null) {

                    if (pCur.getCount() > 0) {
                        pCur.moveToFirst();
                        do {
                            String phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            phoneNumber = Util.normalizePhone(phoneNumber);
                            if (phoneNumber != null)
                                phones.add(phoneNumber);
                        } while (pCur.moveToNext());

                    }

                    pCur.close();
                }

                Cursor eCur = resolver.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        new String[]{
                                ContactsContract.CommonDataKinds.Email.ADDRESS,
                                ContactsContract.CommonDataKinds.Email.DISPLAY_NAME
                        },
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{String.valueOf(rawContactId)}, null);

                if (eCur != null) {
                    if (eCur.getCount() > 0) {
                        eCur.moveToFirst();
                        do {
                            String email = eCur.getString(eCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                            emails.add(email);
                        } while (eCur.moveToNext());
                    }

                    eCur.close();
                }

                SyncableContact newContact = new SyncableContact(rawContactId, displayName, phones);
                gotContactBookInfo.put(rawContactId, newContact);
                ret.add(newContact);

            }

            if (deletedContact.size() > 0 || updatedContact.size() > 0) {
                if (gotContactBookInfo != null && gotContactBookInfo.size() > 0) {
                    saveContactBookInfoToFile(context);
                }
            }

            deletedContact.clear();
            updatedContact.clear();

            return ret;
        }

    }

    public static ArrayList<SyncableContact> getContactBookInfoWithoutFriends() {
        if (contactBookInfoWithoutFriends == null)
            contactBookInfoWithoutFriends = new ArrayList<>();
        return contactBookInfoWithoutFriends;
    }

    public static void makeContactInfosWithoutFriends(Context context, ArrayList<User> friends) {

        ArrayList<SyncableContact> tempList = new ArrayList<>();

        Log.d(TAG, "start making invitelist");

        if (gotContactBookInfo == null) {
            loadContactBookInfoFromFile(context);
        } else {

            if (friends.size() > 0) {
                for (SyncableContact sc : gotContactBookInfo.values()) {

                    if (sc.phones.size() > 0) {
                        boolean isExisting = false;
                        for (User f: friends) {
                            if (sc.phones.contains(f.mobile.replace("+", ""))) {
                                isExisting = true;
                                break;
                            }
                        }
                        if (!isExisting) {
                            tempList.add(sc);
                        }
                    }

                }
                contactBookInfoWithoutFriends = tempList;
            } else {
                contactBookInfoWithoutFriends = new ArrayList<>(gotContactBookInfo.values());
            }
            Log.d(TAG, "start sort invitelist");


            Collections.sort(contactBookInfoWithoutFriends, SyncableContact.Comparators.NAME);
            Log.d(TAG, "end sort invitelist");
        }

    }

    private static List<String> getXmppPhones(List<String> phones, User[] friends) {

        List<String> ret = new ArrayList<String>();
        for (String phone : phones) {
            boolean exist = false;
            phone = phone.replace("+", "");
            for (User f : friends) {
                if (f.mobile.equals(phone)) {
                    exist = true;
                    break;
                }
            }
            if (exist) {
                ret.add(phone);
            }
        }
        return ret;
    }

    static int forceUpdatedContactId = -1;

    public static void addNumber(ContentResolver resolver, int contactId, String phoneNumber) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValue(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber) // Number of the person
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build()); // Type of mobile number
        try
        {
            ContentProviderResult[] res = resolver.applyBatch(ContactsContract.AUTHORITY, ops);
            forceUpdatedContactId = contactId;
        }
        catch (RemoteException e)
        {
            // error
        }
        catch (OperationApplicationException e)
        {
            // error
        }
    }

    public static void addNumber(ContentResolver resolver, int contactId, ArrayList<String> phones) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        for (String pn : phones) {
            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, pn) // Number of the person
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build()); // Type of mobile number
        }

        try
        {
            ContentProviderResult[] res = resolver.applyBatch(ContactsContract.AUTHORITY, ops);
            forceUpdatedContactId = contactId;
        }
        catch (RemoteException e)
        {
            // error
        }
        catch (OperationApplicationException e)
        {
            // error
        }
    }

    private static void saveContactBookInfoToFile(Context context) {
        Log.i(TAG, "started saveContactBook");
        final String save_path1 = context.getFilesDir() + "/"  + "contactbook1.cache";
        final String save_path2 = context.getFilesDir() + "/"  + "contactbook2.cache";
        try {
            Kryo kryo = new Kryo();

            if (gotContactBookInfo != null && gotContactBookInfo.size() > 0) {

                Output output1 = new Output(new FileOutputStream(save_path1));
                for (Integer key : gotContactBookInfo.keySet()) {
                    SyncableContact sc = gotContactBookInfo.get(key);
                    if (sc == null)
                        continue;

                    try {

                        kryo.writeObject(output1, key);
                        kryo.writeObject(output1, sc);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                output1.close();
            }

            Output output2 = new Output(new FileOutputStream(save_path2));
            for (Integer key : versionInfo.keySet()) {
                String version = versionInfo.get(key);
                if (version == null)
                    continue;
                kryo.writeObject(output2, key);
                kryo.writeObject(output2, version);
            }
            output2.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, String.format("ended saveContactBook count = %d", gotContactBookInfo.size()));
    }

    public static void loadContactBookInfoFromFile(Context context) {

        Log.i(TAG, "started loadContactBook");
        final String save_path1 = context.getFilesDir() + "/"  + "contactbook1.cache";
        final String save_path2 = context.getFilesDir() + "/"  + "contactbook2.cache";

        Kryo kryo = new Kryo();

        try {
            Input input1 = new Input(new FileInputStream(save_path1));

            if (gotContactBookInfo == null)
                gotContactBookInfo = new HashMap<Integer, SyncableContact>();
            else
                gotContactBookInfo.clear();

            while (true) {
                Integer key;
                try {
                    key = kryo.readObject(input1, Integer.class);
                }
                catch(Exception e) {
                    key = null;
                }

                if (key == null)
                    break;

                SyncableContact sc;
                try {
                    sc = kryo.readObject(input1, SyncableContact.class);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    sc = null;
                }

                if (sc == null)
                    break;

                gotContactBookInfo.put(key, sc);
            }
            input1.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            gotContactBookInfo = null;
        }

        try {
            Input input2 = new Input(new FileInputStream(save_path2));
            versionInfo.clear();

            while(true) {
                Integer key;
                try {
                    key = kryo.readObject(input2, Integer.class);
                }
                catch (Exception e) {
                    key = null;
                }

                if (key == null)
                    break;

                String value;
                try {
                    value = kryo.readObject(input2, String.class);
                }
                catch (Exception e) {
                    value = null;
                }

                if (value == null)
                    break;

                versionInfo.put(key, value);
            }
            input2.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            versionInfo.clear();
        }

        if (gotContactBookInfo != null)
            Log.i(TAG, String.format("ended loadContactBook count = %d", gotContactBookInfo.size()));
        else
            Log.i(TAG, "ended loadContactBook count");
    }

    public static void deleteContactBookCacheInfo(Context context, boolean keepData) {

        final String save_path1 = context.getFilesDir() + "/"  + "contactbook1.cache";
        final String save_path2 = context.getFilesDir() + "/"  + "contactbook2.cache";

        if (!keepData) {
            new File(save_path1).delete();
            new File(save_path2).delete();
        }

        gotContactBookInfo = null;
        versionInfo.clear();
    }

    public static boolean hasContactWithPhone(ContentResolver cr, String phone) {

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        boolean result = !(cursor == null || cursor.getCount() == 0);
        cursor.close();

        return result;
    }

}
