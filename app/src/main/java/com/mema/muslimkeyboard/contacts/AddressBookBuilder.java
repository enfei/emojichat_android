package com.mema.muslimkeyboard.contacts;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by king on 12/10/2017.
 */

public class AddressBookBuilder {

    private StringBuilder builder;

    public AddressBookBuilder() {
        builder = new StringBuilder();
    }

    public AddressBookBuilder addEntryPhone(String contactName, Collection<String> phones) {

        if (contactName == null || phones == null)
            return this;

        phones.removeAll(Collections.singleton(null));

        if (phones.size() > 0) {
            builder.append(Base64Utils.encode(contactName));
            builder.append('\n');

            for (String phone: phones) {
                if (phone == null || phone.isEmpty())
                    continue;
                builder.append(phone);
            }

            builder.append('\n');
        }

        return this;
    }

    public String build() {
        return builder.toString();
    }

}
