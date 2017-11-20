package com.mema.muslimkeyboard.bean;

import com.mema.muslimkeyboard.utility.FirebaseManager;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Super on 5/12/2017.
 */

public class Dialog implements Serializable {
    public String dialogID;
    public Message.MessageType lastMessageType;
    public String lastMessage;
    public long lastMessageDateSent;
    public long readDate;
    public DialogType type;
    public ArrayList<String> occupantsIds = new ArrayList<>();
    public String photo;
    public String title;
    public boolean mute;
    public boolean saveMedia;
    public boolean notification;
    public long lastSeenDate;
    public String adminId;

    public static Dialog createIndividualDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.type = DialogType.Individual;
        dialog.title = user.username;
        dialog.photo = user.photo;
        dialog.adminId = "";
        dialog.dialogID = FirebaseManager.getInstance().getIndividualDialogID(user.username);
        dialog.occupantsIds.add(user.id);
        return dialog;
    }

    public enum DialogType {Individual, Group}
}
