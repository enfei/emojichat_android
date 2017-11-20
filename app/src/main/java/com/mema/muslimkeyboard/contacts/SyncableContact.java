package com.mema.muslimkeyboard.contacts;

import android.support.annotation.NonNull;

import java.util.Comparator;
import java.util.List;

/**
 * Created by king on 11/10/2017.
 */

public class SyncableContact implements Comparable<SyncableContact>{
    public int contactId;
    public String name;
    public List<String> phones;

    public SyncableContact() {

    }

    public SyncableContact(int contactId, String displayName, List<String> phones) {
        this.contactId = contactId;
        this.name = displayName;
        this.phones = phones;
    }

    public void addPhoneNumber(String phoneNumber) {
        if (phones != null) {
            for (String phone: phones) {
                if (phone.equals(phoneNumber)) {
                    return;
                }
            }
            phones.add(phoneNumber);
        }
    }

    @Override
    public int compareTo(@NonNull SyncableContact sc) {
        return Comparators.NAME.compare(this, sc);
    }

    public static class Comparators {
        public static Comparator<SyncableContact> NAME = new Comparator<SyncableContact>() {
            @Override
            public int compare(SyncableContact lhs, SyncableContact rhs) {
                String lhsName = lhs.name != null ? lhs.name : "";
                String rhsName = rhs.name != null ? rhs.name : "";

                return lhsName.toLowerCase().compareTo(rhsName.toLowerCase());
            }
        };
    }
}
