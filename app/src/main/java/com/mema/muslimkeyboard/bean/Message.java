package com.mema.muslimkeyboard.bean;

import java.io.Serializable;

/**
 * Created by Super on 5/12/2017.
 */

public class Message implements Serializable {
    public String userID;
    public MessageType type;
    public String message;
    public long dateSent;
    public String key;

    public enum MessageType {Text, Emoji, Photo, Video, Document}
}
