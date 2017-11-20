package com.mema.muslimkeyboard.utility.models;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.Query;

/**
 * Created by Super on 5/10/2017.
 */

public class FirebaseChildListener {
    private Query query;
    private ChildEventListener listener;

    public FirebaseChildListener() {

    }

    public FirebaseChildListener(Query query, ChildEventListener listener) {
        this.query = query;
        this.listener = listener;
    }

    public void removeListener() {
        query.removeEventListener(listener);
    }

}
