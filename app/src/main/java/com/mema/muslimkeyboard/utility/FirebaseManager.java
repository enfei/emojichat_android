package com.mema.muslimkeyboard.utility;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.mema.muslimkeyboard.activity.HomeActivity;
import com.mema.muslimkeyboard.bean.Dialog;
import com.mema.muslimkeyboard.bean.Message;
import com.mema.muslimkeyboard.bean.User;
import com.mema.muslimkeyboard.tasks.SendPushTask;
import com.mema.muslimkeyboard.utility.models.FirebaseChildListener;
import com.mema.muslimkeyboard.utility.models.FirebaseValueListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.mema.muslimkeyboard.activity.SignIn.MyPREFERENCES;

/**
 * Created by Super on 5/10/2017.
 */

public class FirebaseManager {
    private static final String TAG = FirebaseManager.class.getSimpleName();
    public static FirebaseManager mRefrence = null;
    public ArrayList<User> userList = new ArrayList<>();
    public ArrayList<User> searchUserList = new ArrayList<>();
    public ArrayList<User> friendList = new ArrayList<>();
    public ArrayList<User> blockFriendList = new ArrayList<>();
    public ArrayList<User> groupFriendList = new ArrayList<>();
    public ArrayList<User> requestList = new ArrayList<>();
    public ArrayList<User> sentList = new ArrayList<>();
    public ArrayList<Dialog> dialogList = new ArrayList<>();
    public HashMap<String, String> dialogBadges = new HashMap<>();
    UsernameExistQueryEventListener usernameQueryListener = null;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private DatabaseReference mUserRef = FirebaseDatabase.getInstance().getReference().child("users");
    private DatabaseReference mMessageRef = FirebaseDatabase.getInstance().getReference().child("messages");
    private FirebaseChildListener mMessageChildListener;

    public static FirebaseManager getInstance() {
        if (null == mRefrence) {
            mRefrence = new FirebaseManager();
        }
        return mRefrence;
    }

    public String getCurrentUserID() {
        return mAuth.getCurrentUser().getUid();
    }

    public String getCurrentUserName() {
        return mAuth.getCurrentUser().getDisplayName();
    }

    public String getCurrentUserEmail() {
        return mAuth.getCurrentUser().getEmail();
    }

    public String getCurrentUserPhoneNumber() {
        return mAuth.getCurrentUser().getPhoneNumber();
    }

    public Uri getCurrentUserPhotoUrl() {
        return mAuth.getCurrentUser().getPhotoUrl();
    }

    public DatabaseReference getCurrentUserRef() {
        String id = mAuth.getCurrentUser().getUid();
        return mUserRef.child(id);
    }

    public DatabaseReference getCurrentUserFriendsRef() {
        return getCurrentUserRef().child("friends");
    }

    public DatabaseReference getCurrentUserSentRef() {
        return getCurrentUserRef().child("sentRequests");
    }

    public DatabaseReference getCurrentUserRequestsRef() {
        return getCurrentUserRef().child("requests");
    }

    public DatabaseReference getCurrentUsersDialogsRef() {
        return getCurrentUserRef().child("dialogs");
    }

    public void updateNotificationSetting(boolean bNotification) {
        getCurrentUserRef().child("notification").setValue(bNotification);
    }

    public void updateProfilePhoto(String photoPath, final OnBooleanListener onUpdateProfileListener) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Uri file = Uri.fromFile(new File(photoPath));
        StorageReference avatarRef = mStorageRef.child("images/avatars/" + user.getUid());
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        avatarRef.putFile(file, metadata)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        @SuppressWarnings("VisibleForTests") final Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(downloadUrl)
                                .build();
                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            getCurrentUserRef().child("photo").setValue(downloadUrl.toString());

                                            onUpdateProfileListener.onBooleanResponse(true);
                                        } else {
                                            onUpdateProfileListener.onBooleanResponse(false);
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        onUpdateProfileListener.onBooleanResponse(false);
                    }
                });
    }

    public void updatePushToken(String pushToken) {
        getCurrentUserRef().child("pushToken").setValue(pushToken);
    }

    public void createAccount(Activity activity, final String firstname, final String lastname, final String email, String password, final String username, final String birth,
                              final String photoPath, final OnBooleanListener onSignupResponseListener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            if (onSignupResponseListener != null)
                                onSignupResponseListener.onBooleanResponse(false);
                            return;
                        }

                        if (photoPath != null && photoPath.length() > 0) {
                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                            Uri file = Uri.fromFile(new File(photoPath));
                            StorageReference avatarRef = mStorageRef.child("images/avatars/" + user.getUid());
                            StorageMetadata metadata = new StorageMetadata.Builder()
                                    .setContentType("image/jpeg")
                                    .build();

                            avatarRef.putFile(file, metadata)
                                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            // Get a URL to the uploaded content
                                            @SuppressWarnings("VisibleForTests") final Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();

                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(username)
                                                    .setPhotoUri(downloadUrl)
                                                    .build();
                                            user.updateProfile(profileUpdates)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                HashMap<String, Object> userMap = new HashMap<>();
                                                                userMap.put("id", user.getUid());
                                                                userMap.put("email", email);
                                                                userMap.put("username", username);
                                                                userMap.put("firstname", firstname);
                                                                userMap.put("lastname", lastname);
                                                                userMap.put("birthday", birth);
                                                                userMap.put("photo", downloadUrl.toString());
                                                                userMap.put("pushToken", FirebaseInstanceId.getInstance().getToken());
                                                                userMap.put("notification", true);
                                                                getCurrentUserRef().setValue(userMap);

                                                                onSignupResponseListener.onBooleanResponse(true);
                                                            } else {
                                                                onSignupResponseListener.onBooleanResponse(false);
                                                            }
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            onSignupResponseListener.onBooleanResponse(false);
                                        }
                                    });
                        } else {
                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                HashMap<String, Object> userMap = new HashMap<>();
                                                userMap.put("id", user.getUid());
                                                userMap.put("email", email);
                                                userMap.put("username", username);
                                                userMap.put("firstname", firstname);
                                                userMap.put("lastname", lastname);
                                                userMap.put("birthday", birth);
                                                userMap.put("pushToken", FirebaseInstanceId.getInstance().getToken());
                                                userMap.put("notification", true);
                                                getCurrentUserRef().setValue(userMap);

                                                onSignupResponseListener.onBooleanResponse(true);
                                            } else {
                                                onSignupResponseListener.onBooleanResponse(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    public void createAccountWithPhoneNumber(final String personName, final String mPhoneNumber, final String photoPath, final OnBooleanListener onSignupResponseListener) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (photoPath != null) {
            Uri file = Uri.fromFile(new File(photoPath));
            StorageReference avatarRef = FirebaseStorage.getInstance().getReference().child("images/avatars/" + user.getUid());
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build();


            avatarRef.putFile(file, metadata)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            final Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(personName)
                                    .setPhotoUri(downloadUrl)
                                    .build();
                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                HashMap<String, Object> userMap = new HashMap<>();
                                                userMap.put("id", user.getUid());
                                                userMap.put("username", personName);
                                                userMap.put("mobile", mPhoneNumber);
                                                userMap.put("photo", downloadUrl.toString());
                                                userMap.put("pushToken", FirebaseInstanceId.getInstance().getToken());
                                                userMap.put("notification", true);
                                                FirebaseManager.getInstance().getCurrentUserRef().setValue(userMap);
                                                Log.d(TAG, "User profile updated.");
                                                onSignupResponseListener.onBooleanResponse(true);
                                            } else {
                                                onSignupResponseListener.onBooleanResponse(false);
                                            }
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            onSignupResponseListener.onBooleanResponse(false);
                        }
                    });

        } else {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(personName)
                    .build();
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                HashMap<String, Object> userMap = new HashMap<>();
                                userMap.put("id", user.getUid());
                                userMap.put("username", personName);
                                userMap.put("mobile", mPhoneNumber);
                                userMap.put("pushToken", FirebaseInstanceId.getInstance().getToken());
                                userMap.put("notification", true);
                                FirebaseManager.getInstance().getCurrentUserRef().setValue(userMap);
                                Log.d(TAG, "User profile updated.");
                                onSignupResponseListener.onBooleanResponse(true);
                            } else {
                                onSignupResponseListener.onBooleanResponse(false);
                            }
                        }
                    });
        }
    }

    public User getUser(String userID, final OnUserResponseListener onUserResponseListener) {
        mUserRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("UserInfo:", String.valueOf(dataSnapshot));
                User user = dataSnapshot.getValue(User.class);
                if (onUserResponseListener != null) {
                    onUserResponseListener.onUserResponse(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (onUserResponseListener != null) {
                    onUserResponseListener.onUserResponse(null);
                }
            }
        });
        return null;
    }

    public void getAllUsers(final ResultListener listener) {
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> list = new ArrayList<User>();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {

                    try {
                        User user = snapshot.getValue(User.class);
                        if (user != null && user.id != null) {
                            if (!user.id.equals(getCurrentUserID()))
                                list.add(user);
                        }
                    } catch (Exception e) {
                        Log.d("getAllUsers", snapshot.toString());
                    }

                }
                listener.onResult(true, null, list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "getAllUsers: onCancelled");
                listener.onResult(false, databaseError.getMessage(), null);
            }
        });
    }

    public void getFriendUserIds(final ResultListener listener) {
        getCurrentUserFriendsRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> userIds = new ArrayList<String>();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    String id = snapshot.getKey();
                    userIds.add(id);
                }
                listener.onResult(true, null, userIds);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onResult(false, databaseError.getMessage(), null);
            }
        });
    }

    public FirebaseValueListener searchUsers(String keyword, final OnUpdateListener onUpdateListener) {
        if (keyword == null || keyword.length() == 0) {
            searchUserList.clear();
            onUpdateListener.onUpdate();
            return null;
        }

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                searchUserList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);

                    if (user.email.equalsIgnoreCase(getCurrentUserEmail()))
                        continue;

                    boolean bExist = false;
                    for (int i = 0; i < friendList.size(); i++) {
                        User tmpUser = friendList.get(i);
                        if (tmpUser.email.equalsIgnoreCase(user.email)) {
                            bExist = true;
                            break;
                        }
                    }

                    if (bExist == true) continue;

                    for (int j = 0; j < sentList.size(); j++) {
                        User tmpUser = sentList.get(j);
                        if (tmpUser.email.equalsIgnoreCase(user.email)) {
                            bExist = true;
                            break;
                        }
                    }

                    if (bExist == true) continue;

                    searchUserList.add(user);
                }
                onUpdateListener.onUpdate();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                searchUserList.clear();
                onUpdateListener.onUpdate();
            }
        };
        Query query = mUserRef.orderByChild("firstname")
                .startAt(keyword)
                .endAt(keyword + "zzz");
        query.addListenerForSingleValueEvent(valueEventListener);

        /*Query query = mUserRef.orderByChild("username")
                .startAt(keyword)
                .endAt(keyword + "zzz");
        query.addListenerForSingleValueEvent(valueEventListener);*/

        return new FirebaseValueListener(query, valueEventListener);
    }

    public FirebaseValueListener addUserListener(final OnUpdateListener onUpdateListener) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);

                    if (!user.email.equals(mAuth.getCurrentUser().getEmail())) {
                        userList.add(user);
                    }
                }
                onUpdateListener.onUpdate();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onUpdateListener.onUpdate();
            }
        };
        mUserRef.addValueEventListener(valueEventListener);
        return new FirebaseValueListener(mUserRef, valueEventListener);
    }

    public FirebaseValueListener addFriendListener(final OnUpdateListener onUpdateListener) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friendList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String id = userSnapshot.getKey();
                    getUser(id, new OnUserResponseListener() {
                        @Override
                        public void onUserResponse(User user) {
                            friendList.add(user);
                            onUpdateListener.onUpdate();
                        }
                    });
                }

                if (dataSnapshot.getChildrenCount() == 0) {
                    onUpdateListener.onUpdate();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onUpdateListener.onUpdate();
            }
        };
        Query query = getCurrentUserFriendsRef().orderByChild("username");
        query.addValueEventListener(valueEventListener);
        return new FirebaseValueListener(query, valueEventListener);
    }

    public FirebaseValueListener addGroupFriendListener(final OnUpdateListener onUpdateListener) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupFriendList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String id = userSnapshot.getKey();
                    getUser(id, new OnUserResponseListener() {
                        @Override
                        public void onUserResponse(User user) {
                            groupFriendList.add(user);
                            onUpdateListener.onUpdate();
                        }
                    });
                }

                if (dataSnapshot.getChildrenCount() == 0) {
                    onUpdateListener.onUpdate();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onUpdateListener.onUpdate();
            }
        };
        Query query = getCurrentUserFriendsRef().orderByChild("username");
        query.addValueEventListener(valueEventListener);
        return new FirebaseValueListener(query, valueEventListener);
    }

    public FirebaseValueListener addSentListener(final OnUpdateListener onUpdateListener) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sentList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String id = userSnapshot.getKey();
                    getUser(id, new OnUserResponseListener() {
                        @Override
                        public void onUserResponse(User user) {

                            boolean bExist = false;
                            for (int i = 0; i < sentList.size(); i++) {
                                User tmpUser = sentList.get(i);
                                if (tmpUser.email.equalsIgnoreCase(user.email)) {
                                    bExist = true;
                                    break;
                                }
                            }
                            if (!bExist)
                                sentList.add(user);

                            onUpdateListener.onUpdate();
                        }
                    });
                }

                if (dataSnapshot.getChildrenCount() == 0) {
                    onUpdateListener.onUpdate();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onUpdateListener.onUpdate();
            }
        };
        Query query = getCurrentUserSentRef()
                .orderByChild("username");
        query.addValueEventListener(valueEventListener);
        return new FirebaseValueListener(query, valueEventListener);
    }

    public FirebaseValueListener addRequestListener(final OnUpdateListener onUpdateListener) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                requestList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String id = userSnapshot.getKey();
                    getUser(id, new OnUserResponseListener() {
                        @Override
                        public void onUserResponse(User user) {

                            boolean bExist = false;
                            for (int i = 0; i < requestList.size(); i++) {
                                User tmpUser = requestList.get(i);
                                if (tmpUser.email.equalsIgnoreCase(user.email)) {
                                    bExist = true;
                                    break;
                                }
                            }
                            if (!bExist)
                                requestList.add(user);

                            onUpdateListener.onUpdate();
                        }
                    });
                }

                if (dataSnapshot.getChildrenCount() == 0) {
                    onUpdateListener.onUpdate();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onUpdateListener.onUpdate();
            }
        };
        Query query = getCurrentUserRequestsRef()
                .orderByChild("username");
        query.addValueEventListener(valueEventListener);
        return new FirebaseValueListener(query, valueEventListener);
    }

    public void refreshChatBadge(final OnUpdateListener onUpdateListener) {

        for (int i = 0; i < dialogList.size(); i++) {
            final Dialog dialog = dialogList.get(i);
            if (dialog.type == Dialog.DialogType.Individual) {
                String userID = dialog.occupantsIds.get(0);
                getIndividualRoomID(userID, new OnStringListener() {
                    @Override
                    public void onStringResponse(String roomName) {
//                                DatabaseReference query = mMessageRef.child(roomName);
//                                query.addChildEventListener(childEventListener);
//                                mMessageChildListener = new FirebaseChildListener(query, childEventListener);

                        Query query = mMessageRef.child(roomName).orderByChild("dateSent").startAt(dialog.lastSeenDate + 1);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                dialogBadges.put(dialog.dialogID, String.valueOf(dataSnapshot.getChildrenCount()));

                                if (dialogList.indexOf(dialog) == (dialogList.size() - 1)) {
                                    onUpdateListener.onUpdate();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                onUpdateListener.onUpdate();
                            }
                        });

                    }
                });
            } else if (dialog.type == Dialog.DialogType.Group) {
                Query query = mMessageRef.child(dialog.dialogID).orderByChild("dateSent").startAt(dialog.lastSeenDate + 1);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        dialogBadges.put(dialog.dialogID, String.valueOf(dataSnapshot.getChildrenCount()));
                        if (dialogList.indexOf(dialog) == (dialogList.size() - 1)) {
                            onUpdateListener.onUpdate();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        }
        onUpdateListener.onUpdate();
    }

    public FirebaseValueListener addDialogListener(final OnUpdateListener onUpdateListener) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dialogList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    final Dialog dialog = userSnapshot.getValue(Dialog.class);
//                    if ((dialog.lastMessage == null) || ((dialog.lastMessage != null) && (!TextUtils.isEmpty(dialog.lastMessage))))
                    if (!TextUtils.isEmpty(dialog.lastMessage))
                        dialogList.add(dialog);
                    else
                        continue;
                    if (dialog.type == Dialog.DialogType.Individual) {

                        if (dialog.occupantsIds == null || dialog.occupantsIds.size() == 0) {
                            String userID = dialog.dialogID.substring(11);
                            dialog.occupantsIds.add(userID);
                            getCurrentUsersDialogsRef().child(dialog.dialogID).child("occupantsIds").setValue(dialog.occupantsIds);
                        }

                        getUser(dialog.occupantsIds.get(0), new OnUserResponseListener() {
                            @Override
                            public void onUserResponse(User user) {
                                if (user != null) {
                                    if (user.photo != null)
                                        dialog.photo = user.photo;
                                    else
                                        dialog.photo = "";
                                    dialog.title = String.format("%s", user.username);
                                    if (dialogList.indexOf(dialog) == (dialogList.size() - 1))
                                        refreshChatBadge(onUpdateListener);
                                }
                            }
                        });
                    }
                }

//                if(dialogList.size() == 0)
                onUpdateListener.onUpdate();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onUpdateListener.onUpdate();
            }
        };
        Query query = getCurrentUsersDialogsRef()
                .orderByChild("lastMessageDateSent");
        query.addValueEventListener(valueEventListener);
        return new FirebaseValueListener(query, valueEventListener);
    }

    public void sendRequestToUser(String userID) {
        mUserRef.child(userID).child("requests").child(mAuth.getCurrentUser().getUid()).setValue(true);
        getCurrentUserRef().child("sentRequests").child(userID).setValue(true);
    }

    public void removeFriend(String userID) {
        getCurrentUserFriendsRef().child(userID).removeValue();
        mUserRef.child(userID).child("friends").child(mAuth.getCurrentUser().getUid()).setValue(false);
    }

    public void acceptFriendRequest(String userID, String friendUserID) {
        mUserRef.child(userID).child("friends").child(friendUserID).setValue(true);
        mUserRef.child(friendUserID).child("friends").child(userID).setValue(true);

    }

    public void blockUser(String userID) {
        mUserRef.child(userID).child("blockedUser").child(mAuth.getCurrentUser().getUid()).setValue(true);
        mUserRef.child(mAuth.getCurrentUser().getUid()).child("blockedUser").child(userID).setValue(true);
    }

    public void unBlockUser(String userID) {
        mUserRef.child(userID).child("blockedUser").child(mAuth.getCurrentUser().getUid()).removeValue();
        mUserRef.child(mAuth.getCurrentUser().getUid()).child("blockedUser").child(userID).removeValue();
    }

    public FirebaseValueListener getBlockUserList(final OnUpdateListener onUpdateListener) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                blockFriendList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String id = userSnapshot.getKey();
                    getUser(id, new OnUserResponseListener() {
                        @Override
                        public void onUserResponse(User user) {
                            blockFriendList.add(user);
                            onUpdateListener.onUpdate();
                        }
                    });
                }

                if (dataSnapshot.getChildrenCount() == 0) {
                    onUpdateListener.onUpdate();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                onUpdateListener.onUpdate();
            }
        };
        Query query = getCurrentUserRef().child("blockedUser");
        query.addValueEventListener(valueEventListener);
        return new FirebaseValueListener(query, valueEventListener);
    }

    public void markAsRead(String dialogID) {
        getCurrentUserRef().child("dialogs").child(dialogID).child("lastSeenDate").setValue(ServerValue.TIMESTAMP);
    }

    public void deleteGroup(Dialog dialog) {
        getCurrentUserRef().child("dialogs").child(dialog.dialogID).removeValue();
        for (int i = 0; i < dialog.occupantsIds.size(); i++) {
            mUserRef.child(dialog.occupantsIds.get(i)).child("dialogs").child(dialog.dialogID).removeValue();
        }
        mMessageRef.child(dialog.dialogID).removeValue();
    }

    public void removeDialog(Dialog dialog) {
        getCurrentUserRef().child("dialogs").child(dialog.dialogID).child("lastMessage").setValue("");
        getCurrentUserRef().child("dialogs").child(dialog.dialogID).child("lastMessageType").removeValue();
        getCurrentUserRef().child("dialogs").child(dialog.dialogID).child("lastMessageDateSent").removeValue();
        getCurrentUserRef().child("dialogs").child(dialog.dialogID).child("lastSeenDate").removeValue();

        if (dialog.type == Dialog.DialogType.Individual) {
            getIndividualRoomID(dialog.occupantsIds.get(0), new OnStringListener() {
                @Override
                public void onStringResponse(String roomName) {
                    mMessageRef.child(roomName).removeValue();
                }
            });
        } else
            mMessageRef.child(dialog.dialogID).removeValue();
    }

    public void rejectFriendRequest(String userID) {

        for (int i = 0; i < requestList.size(); i++) {
            User user = requestList.get(i);
            if (user.id == userID) {
                SharedPreferences sharedpreferences = HomeActivity.getInstance().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                String username = sharedpreferences.getString("user_name", "");

                new SendPushTask(Util.pushJsonObjectFrom("", username + " rejected your friend request", user.pushToken)).execute();
                break;
            }
        }

        getCurrentUserRequestsRef().child(userID).removeValue();
        mUserRef.child(userID).child("sentRequests").child(mAuth.getCurrentUser().getUid()).removeValue();
    }

    public String getIndividualDialogID(String userID) {
        return String.format("individual_%s", userID);
    }

    public void createIndividualDialog(final User user) {
        String currentUserDialogName = getIndividualDialogID(user.id);
        String friendUserDialogName = getIndividualDialogID(getCurrentUserID());

        ArrayList<String> currentUserOccupantsIds = new ArrayList<>();
        currentUserOccupantsIds.add(user.id);
        getCurrentUsersDialogsRef().child(currentUserDialogName).child("dialogID").setValue(currentUserDialogName);
        getCurrentUsersDialogsRef().child(currentUserDialogName).child("type").setValue(Dialog.DialogType.Individual);
        getCurrentUsersDialogsRef().child(currentUserDialogName).child("occupantsIds").setValue(currentUserOccupantsIds);

        ArrayList<String> otherUserOccupantsIds = new ArrayList<>();
        otherUserOccupantsIds.add(getCurrentUserID());
        mUserRef.child(user.id).child("dialogs").child(friendUserDialogName).child("dialogID").setValue(friendUserDialogName);
        mUserRef.child(user.id).child("dialogs").child(friendUserDialogName).child("type").setValue(Dialog.DialogType.Individual);
        mUserRef.child(user.id).child("dialogs").child(friendUserDialogName).child("occupantsIds").setValue(otherUserOccupantsIds);
    }

    public void updateGroup(final String groupID, String title, String photo, final OnUpdateFinishListener update) {
        final ArrayList<String> currentUserOccupantsIds = new ArrayList<>();
        for (User user : Util.getInstance().workingGroupMember) {
            currentUserOccupantsIds.add(user.id);
        }

        getCurrentUsersDialogsRef().child(groupID).child("dialogID").setValue(groupID);
        getCurrentUsersDialogsRef().child(groupID).child("type").setValue(Dialog.DialogType.Group);
        getCurrentUsersDialogsRef().child(groupID).child("occupantsIds").setValue(currentUserOccupantsIds);
        getCurrentUsersDialogsRef().child(groupID).child("title").setValue(title);
        getCurrentUsersDialogsRef().child(groupID).child("saveMedia").setValue(true);
        getCurrentUsersDialogsRef().child(groupID).child("mute").setValue(false);
        getCurrentUsersDialogsRef().child(groupID).child("notification").setValue(true);
        getCurrentUsersDialogsRef().child(groupID).child("adminId").setValue(getCurrentUserID());

        for (String userID : currentUserOccupantsIds) {
            ArrayList<String> occupantsIds = new ArrayList<>();
            occupantsIds.addAll(currentUserOccupantsIds);
            occupantsIds.remove(userID);
            occupantsIds.add(FirebaseManager.getInstance().getCurrentUserID());

            mUserRef.child(userID).child("dialogs").child(groupID).child("dialogID").setValue(groupID);
            mUserRef.child(userID).child("dialogs").child(groupID).child("type").setValue(Dialog.DialogType.Group);
            mUserRef.child(userID).child("dialogs").child(groupID).child("occupantsIds").setValue(occupantsIds);
            mUserRef.child(userID).child("dialogs").child(groupID).child("title").setValue(title);
            mUserRef.child(userID).child("dialogs").child(groupID).child("saveMedia").setValue(true);
            mUserRef.child(userID).child("dialogs").child(groupID).child("mute").setValue(false);
            mUserRef.child(userID).child("dialogs").child(groupID).child("notification").setValue(true);
            mUserRef.child(userID).child("dialogs").child(groupID).child("adminId").setValue(getCurrentUserID());
        }

        if (photo != null && photo.length() > 0) {
            Uri file = Uri.fromFile(new File(photo));
            StorageReference avatarRef = mStorageRef.child("images/group/" + groupID);
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build();

            avatarRef.putFile(file, metadata)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            @SuppressWarnings("VisibleForTests") final Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                            getCurrentUsersDialogsRef().child(groupID).child("photo").setValue(downloadUrl.toString());
                            for (String userID : currentUserOccupantsIds) {
                                mUserRef.child(userID).child("dialogs").child(groupID).child("photo").setValue(downloadUrl.toString());
                            }

                            update.onUpdate();
                        }
                    });
        } else {
            update.onUpdate();
        }
    }

    public void createGroupDialog(String title, String photo) {

        final ArrayList<String> currentUserOccupantsIds = new ArrayList<>();
        for (User user : Util.getInstance().workingFriends) {
            currentUserOccupantsIds.add(user.id);
        }

        final String groupID = getCurrentUsersDialogsRef().push().getKey();
        getCurrentUsersDialogsRef().child(groupID).child("dialogID").setValue(groupID);
        getCurrentUsersDialogsRef().child(groupID).child("type").setValue(Dialog.DialogType.Group);
        getCurrentUsersDialogsRef().child(groupID).child("occupantsIds").setValue(currentUserOccupantsIds);
        getCurrentUsersDialogsRef().child(groupID).child("title").setValue(title);
        getCurrentUsersDialogsRef().child(groupID).child("saveMedia").setValue(true);
        getCurrentUsersDialogsRef().child(groupID).child("mute").setValue(false);
        getCurrentUsersDialogsRef().child(groupID).child("notification").setValue(true);
        getCurrentUsersDialogsRef().child(groupID).child("adminId").setValue(getCurrentUserID());

        for (String userID : currentUserOccupantsIds) {
            ArrayList<String> occupantsIds = new ArrayList<>();
            occupantsIds.addAll(currentUserOccupantsIds);
            occupantsIds.remove(userID);
            occupantsIds.add(FirebaseManager.getInstance().getCurrentUserID());

            mUserRef.child(userID).child("dialogs").child(groupID).child("dialogID").setValue(groupID);
            mUserRef.child(userID).child("dialogs").child(groupID).child("type").setValue(Dialog.DialogType.Group);
            mUserRef.child(userID).child("dialogs").child(groupID).child("occupantsIds").setValue(occupantsIds);
            mUserRef.child(userID).child("dialogs").child(groupID).child("title").setValue(title);
            mUserRef.child(userID).child("dialogs").child(groupID).child("saveMedia").setValue(true);
            mUserRef.child(userID).child("dialogs").child(groupID).child("mute").setValue(false);
            mUserRef.child(userID).child("dialogs").child(groupID).child("notification").setValue(true);
            mUserRef.child(userID).child("dialogs").child(groupID).child("adminId").setValue(getCurrentUserID());
        }

        if (photo != null && photo.length() > 0) {
            Uri file = Uri.fromFile(new File(photo));
            StorageReference avatarRef = mStorageRef.child("images/group/" + groupID);
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpeg")
                    .build();

            avatarRef.putFile(file, metadata)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            @SuppressWarnings("VisibleForTests") final Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                            getCurrentUsersDialogsRef().child(groupID).child("photo").setValue(downloadUrl.toString());
                            for (String userID : currentUserOccupantsIds) {
                                mUserRef.child(userID).child("dialogs").child(groupID).child("photo").setValue(downloadUrl.toString());
                            }
                        }
                    });
        }
    }

    public void removeSingleMessage(final Context chatActivity, String userId, final String key) {

        getIndividualRoomID(userId, new OnStringListener() {
            @Override
            public void onStringResponse(String roomName) {
                //mMessageRef.child(roomName).child(key).removeValue();
                Log.d(TAG, "onStringResponse() called with: roomName = [" + roomName + "]");
                Query queryRef = mMessageRef.child(roomName).child(key);

                queryRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot snapshot, String previousChild) {
                        snapshot.getRef().removeValue();
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Toast.makeText(chatActivity, "message has been removed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });


    }

    public void addMessageListener(final Dialog dialog, final OnMessageListener messageListener) {
        final ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);
                message.key = dataSnapshot.getKey();
                messageListener.onMessageResponse(message);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        if (dialog.type == Dialog.DialogType.Individual) {
            String userID = dialog.occupantsIds.get(0);
            getIndividualRoomID(userID, new OnStringListener() {
                @Override
                public void onStringResponse(String roomName) {
                    DatabaseReference query = mMessageRef.child(roomName);
                    query.addChildEventListener(childEventListener);
                    mMessageChildListener = new FirebaseChildListener(query, childEventListener);
                }
            });
        } else if (dialog.type == Dialog.DialogType.Group) {
            DatabaseReference query = mMessageRef.child(dialog.dialogID);
            query.addChildEventListener(childEventListener);
            mMessageChildListener = new FirebaseChildListener(query, childEventListener);
        }
    }

    public void removeMessageListener() {
        if (mMessageChildListener != null) {
            mMessageChildListener.removeListener();
        }
    }

    public void createDialogMessage(final Dialog dialog, final Message message) {
        if (dialog.type == Dialog.DialogType.Individual) {
            getIndividualRoomID(dialog.occupantsIds.get(0), new OnStringListener() {
                @Override
                public void onStringResponse(String roomName) {
                    mMessageRef.child(roomName).push().setValue(message);
                }
            });

            String currentUserDialogName = getIndividualDialogID(dialog.occupantsIds.get(0));
            String friendUserDialogName = getIndividualDialogID(getCurrentUserID());

            getCurrentUsersDialogsRef().child(currentUserDialogName).child("lastMessageType").setValue(message.type);
            getCurrentUsersDialogsRef().child(currentUserDialogName).child("lastMessage").setValue(message.message);
            getCurrentUsersDialogsRef().child(currentUserDialogName).child("lastMessageDateSent").setValue(message.dateSent);
            getCurrentUsersDialogsRef().child(currentUserDialogName).child("lastSeenDate").setValue(message.dateSent);

            getCurrentUserRef().child("lastSeen").setValue(message.dateSent);

            if (message.message != null) {
                mUserRef.child(dialog.occupantsIds.get(0)).child("dialogs").child(friendUserDialogName).child("lastMessageType").setValue(message.type);
                mUserRef.child(dialog.occupantsIds.get(0)).child("dialogs").child(friendUserDialogName).child("lastMessage").setValue(message.message);
            }

            mUserRef.child(dialog.occupantsIds.get(0)).child("dialogs").child(friendUserDialogName).child("lastMessageDateSent").setValue(message.dateSent);

            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    searchUserList.clear();
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user.notification) {
                            SharedPreferences sharedpreferences = HomeActivity.getInstance().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                            String username = sharedpreferences.getString("user_name", "");

                            switch (message.type) {
                                case Photo:
                                    new SendPushTask(Util.pushJsonObjectFrom("", username + " sent a photo", user.pushToken)).execute();
                                    break;
                                case Document:
                                    new SendPushTask(Util.pushJsonObjectFrom("", username + " sent a document", user.pushToken)).execute();
                                    break;
                                default:
                                    new SendPushTask(Util.pushJsonObjectFrom("", username + " sent a message", user.pushToken)).execute();
                                    break;
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            Query query = mUserRef.orderByChild("id")
                    .equalTo(dialog.occupantsIds.get(0));
            query.addListenerForSingleValueEvent(valueEventListener);

        } else {
            mMessageRef.child(dialog.dialogID).push().setValue(message);
            getCurrentUsersDialogsRef().child(dialog.dialogID).child("lastMessageType").setValue(message.type);
            getCurrentUsersDialogsRef().child(dialog.dialogID).child("lastMessage").setValue(message.message);
            getCurrentUsersDialogsRef().child(dialog.dialogID).child("lastMessageDateSent").setValue(message.dateSent);
            getCurrentUsersDialogsRef().child(dialog.dialogID).child("lastSeenDate").setValue(message.dateSent);

            getCurrentUserRef().child("lastSeen").setValue(message.dateSent);

            for (final String userID : dialog.occupantsIds) {
                mUserRef.child(userID).child("dialogs").child(dialog.dialogID).child("lastMessageType").setValue(message.type);
                mUserRef.child(userID).child("dialogs").child(dialog.dialogID).child("lastMessage").setValue(message.message);
                mUserRef.child(userID).child("dialogs").child(dialog.dialogID).child("lastMessageDateSent").setValue(message.dateSent);

                if (!dialog.notification)
                    continue;

                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        searchUserList.clear();
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            User user = userSnapshot.getValue(User.class);
                            if (user.notification) {
                                SharedPreferences sharedpreferences = HomeActivity.getInstance().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                                String username = sharedpreferences.getString("user_name", "");
                                if (message.type == Message.MessageType.Photo)
                                    new SendPushTask(Util.pushJsonObjectFrom("", username + " sent a photo", user.pushToken)).execute();
                                else
                                    new SendPushTask(Util.pushJsonObjectFrom("", username + " sent a message", user.pushToken)).execute();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                };
                Query query = mUserRef.orderByChild("id")
                        .equalTo(userID);
                query.addListenerForSingleValueEvent(valueEventListener);
            }
        }
    }

    public void getIndividualRoomID(String userID, final OnStringListener roomNameListener) {
        final String roomID1 = String.format("%s_%s", getCurrentUserID(), userID);
        final String roomID2 = String.format("%s_%s", userID, getCurrentUserID());
        mMessageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(roomID1)) {
                    roomNameListener.onStringResponse(roomID1);
                } else {
                    roomNameListener.onStringResponse(roomID2);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                roomNameListener.onStringResponse(roomID1);
            }
        });
    }

    public void getEmailFromUsername(String username, UsernameExistQueryEventListener unExistListener) {
        Query queryRef = FirebaseDatabase.getInstance().getReference().child("usernameEmailLink").child(username.toLowerCase()).orderByValue();
        usernameQueryListener = unExistListener;

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    System.out.println("LYM:" + dataSnapshot);
//                    User user = iterator.next().getValue(User.class);
                    usernameQueryListener.onUsernameQueryDone(true, dataSnapshot.getValue().toString());
                } else {
                    Log.e("LYM", "Child Count is 0");
                    usernameQueryListener.onUsernameQueryDone(false, "");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("LYM", "Cancelled");
                usernameQueryListener.onUsernameQueryDone(false, "");
            }
        };
        queryRef.addListenerForSingleValueEvent(valueEventListener);
    }

    public void uploadPhoto(Uri file, final OnStringListener onStringListener) {
        StorageReference avatarRef = mStorageRef.child("images/chat/" + getCurrentUserID() + System.currentTimeMillis());
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build();

        avatarRef.putFile(file, metadata)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                        onStringListener.onStringResponse(downloadUrl.toString());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        onStringListener.onStringResponse(null);
                    }
                });
    }

    public void uploadDocument(Uri file, final OnStringListener onStringListener) {
//        StorageReference avatarRef = mStorageRef.child("doc/chat/" + getCurrentUserID() + System.currentTimeMillis());
//        StorageMetadata metadata = new StorageMetadata.Builder()
//                .build();
        StorageReference riversRef = mStorageRef.child("doc/chat/" + file.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(file);
// Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d(TAG, "onFailure() called with: exception = [" + exception + "]");
                onStringListener.onStringResponse(null);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.d(TAG, "onSuccess() called with: taskSnapshot = [" + downloadUrl.toString() + "]");
                onStringListener.onStringResponse(downloadUrl.toString());
            }
        });


    }

    public void resetPassword(String userEmail) {
        mAuth.sendPasswordResetEmail(userEmail);
    }

    public void signOut() {
        mAuth.signOut();
    }

    public interface OnBooleanListener {
        void onBooleanResponse(boolean success);
    }

    public interface OnUpdateListener {
        void onUpdate();
    }

    public interface OnUserResponseListener {
        void onUserResponse(User user);
    }

    public interface OnUpdateFinishListener {
        void onUpdate();
    }

    public interface OnStringListener {
        void onStringResponse(String value);
    }

    public interface OnMessageListener {
        void onMessageResponse(Message message);
    }
}
