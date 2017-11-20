package com.mema.muslimkeyboard.bean;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Super on 5/10/2017.
 */

public class User implements Serializable {

    private static final User instance = new User();
    private String password;
    public String id;
    public String username;
    public String mobile;
    public String firstname;
    public String lastname;
    public String email;
    public String photo;
    public long lastSeen;
    public Map<String, Object> dialogs;
    public Map<String, Boolean> blockedUser;
    public String birthday;
    public String pushToken;
    public boolean notification;

    public static User get() {
        synchronized (instance) {
            return instance;
        }
    }

    public User() {

    }
}
