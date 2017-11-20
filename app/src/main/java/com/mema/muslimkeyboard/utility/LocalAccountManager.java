package com.mema.muslimkeyboard.utility;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

/**
 * Created by king on 11/10/2017.
 */

public class LocalAccountManager {
    public static final String ACCOUNT_TYPE = "EmojiChat";

    public static Account createAccount(Context context, String accountName) {
        final Account account = new Account(accountName, ACCOUNT_TYPE);
        final AccountManager am = AccountManager.get(context);

        Account acct = null;

        if (am.addAccountExplicitly(account, null, null)) {
            acct = getExactAccount(am);
        }

        return acct;
    }

    public static Account getExisintAccount(Context context) {
        return getExactAccount(AccountManager.get(context));
    }

    private static Account getExactAccount(AccountManager am) {
        Account[] accounts = am.getAccountsByType(ACCOUNT_TYPE);
        if (accounts != null && accounts.length > 0) {
            return accounts[0];
        } else {
            return null;
        }
    }

}
