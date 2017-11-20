package com.mema.muslimkeyboard.activity.group;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.bean.Dialog;
import com.mema.muslimkeyboard.bean.User;
import com.mema.muslimkeyboard.constants.FileUtil;
import com.mema.muslimkeyboard.utility.BaseActivity;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.mema.muslimkeyboard.utility.Util;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;

public class GroupEditActivity extends BaseActivity implements View.OnClickListener {

    static final int CAMERA_REQUEST = 102;
    private final static int ALL_PERMISSIONS_RESULT = 107;
    Boolean bTMute = true, btMedia = true, bEnable = false;
    ImageView tgMute, tgMedia;
    CircleImageView ivGroupPhoto;
    TextView tvSave, tvParticipants;
    EditText etGroupName;
    TextView tvGroupName;
    LinearLayout editPenLayout;
    Bitmap yourSelectedImage;
    String tempFilePath = "";
    ArrayList permissionsToRequest;
    ArrayList permissionsRejected = new ArrayList();
    ArrayList permissions = new ArrayList();
    LinearLayout popupDelete, popupMedia;

    //    ArrayList<User> userList;
    ListView listView;
    GroupEditActivity.GroupEditListAdapter adapter;
    Dialog dialog;
    RelativeLayout overlayLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);

//        userList = new ArrayList<>();

        dialog = (Dialog) getIntent().getSerializableExtra("dialog");

        popupDelete = (LinearLayout) findViewById(R.id.popup_delete_group);
        popupMedia = (LinearLayout) findViewById(R.id.popup_pick_media);
        popupDelete.setVisibility(View.GONE);
        popupMedia.setVisibility(View.GONE);

        permissions.add(CAMERA);
        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }

        initView();
    }

    @Override
    protected void initUI() {

    }

    public void initView() {


        listView = (ListView) findViewById(R.id.list);

        Util.getInstance().workingGroupMember.clear();
        for (int i = 0; i < dialog.occupantsIds.size(); i++) {
            String strID = dialog.occupantsIds.get(i);
            FirebaseManager.getInstance().getUser(strID, new FirebaseManager.OnUserResponseListener() {
                @Override
                public void onUserResponse(User user) {
                    Util.getInstance().workingGroupMember.add(user);
//                    userList.add(user);
                    adapter.notifyDataSetChanged();
                }
            });
        }

        adapter = new GroupEditActivity.GroupEditListAdapter(this);
        listView.setAdapter(adapter);


        overlayLayout = (RelativeLayout) findViewById(R.id.overlayLayout);
        overlayLayout.setVisibility(View.GONE);

        /* Init Popup */

        editPenLayout = (LinearLayout) findViewById(R.id.layout_edit_group);
        editPenLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPenLayout.setVisibility(View.GONE);
                tvGroupName.setVisibility(View.GONE);
                etGroupName.setVisibility(View.VISIBLE);
                etGroupName.setText(tvGroupName.getText());
                etGroupName.requestFocus();
            }
        });

        findViewById(R.id.layoutTakePhoto).setOnClickListener(this);
        findViewById(R.id.layoutChoosePhoto).setOnClickListener(this);
        findViewById(R.id.layoutCancel).setOnClickListener(this);
        findViewById(R.id.maskView).setOnClickListener(this);


        findViewById(R.id.ivBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.layoutDeleteGroup).setOnClickListener(this);
        findViewById(R.id.layoutCancel1).setOnClickListener(this);
        findViewById(R.id.maskView).setOnClickListener(this);

        etGroupName = (EditText) findViewById(R.id.edtGroupName);
        tvGroupName = (TextView) findViewById(R.id.tvGroupName);
        etGroupName.setVisibility(View.GONE);
        tvGroupName.setText(dialog.title);
        etGroupName.setText(dialog.title);

        tvParticipants = (TextView) findViewById(R.id.tvParticipants);
        tvParticipants.setText("Participants: " + String.valueOf(dialog.occupantsIds.size()));


        tvSave = (TextView) findViewById(R.id.tvSave);
        tvSave.setOnClickListener(this);

        tvParticipants.setText("Participants: " + dialog.occupantsIds.size());

        ivGroupPhoto = (CircleImageView) findViewById(R.id.iv_group_photo);
        ivGroupPhoto.setOnClickListener(this);
        if (dialog.photo != null && dialog.photo.length() > 0) {
            Picasso.with(GroupEditActivity.this).load(dialog.photo).into(ivGroupPhoto);
        }

        tgMute = (ImageView) findViewById(R.id.toggleMute);
        tgMedia = (ImageView) findViewById(R.id.toggleSaveMedia);

        findViewById(R.id.muteLayout).setOnClickListener(this);
        findViewById(R.id.saveMediaLayout).setOnClickListener(this);
        findViewById(R.id.deleteGroupLayout).setOnClickListener(this);
        findViewById(R.id.layoutCancel).setOnClickListener(this);
        findViewById(R.id.layout_add_partner).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.maskView:
                hideAlertMenu();
                break;
            case R.id.muteLayout:
                bTMute = !bTMute;
                if (bTMute) {
                    tgMute.setImageResource(R.mipmap.switch_on);
                } else {
                    tgMute.setImageResource(R.mipmap.switch_off);
                }

                break;
            case R.id.saveMediaLayout:
                btMedia = !btMedia;
                if (btMedia) {
                    tgMedia.setImageResource(R.mipmap.switch_on);
                } else {
                    tgMedia.setImageResource(R.mipmap.switch_off);
                }
                break;
            case R.id.tvSave:
                save();
                break;
            case R.id.deleteGroupLayout:
                popupDelete.setVisibility(View.VISIBLE);
                popupMedia.setVisibility(View.GONE);
                showAlertMenu();
                break;
            case R.id.layoutDeleteGroup:
                FirebaseManager.getInstance().deleteGroup(dialog);
                hideAlertMenu();
                break;
            case R.id.layout_add_partner:
                Intent intent = new Intent(this, GroupAddActivity.class);
                intent.putExtra("Dialog", dialog);
                startActivity(intent);
                break;
            case R.id.layoutCancel:
                hideAlertMenu();
                break;

            case R.id.layoutTakePhoto:
                hideAlertMenu();
                Intent dataBack = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivityForResult(dataBack, CAMERA_REQUEST);
                break;
            case R.id.layoutChoosePhoto:
                hideAlertMenu();
                Crop.pickImage(this);
                break;

            case R.id.layoutCancel1:
                hideAlertMenu();
                break;
            case R.id.iv_group_photo:
                popupDelete.setVisibility(View.GONE);
                popupMedia.setVisibility(View.VISIBLE);
                showAlertMenu();
                break;
            default:
                break;
        }
    }

    private void save() {
        String groupName = etGroupName.getText().toString().trim();
        if (groupName.length() == 0) {
            Toast.makeText(this, "Please enter group name", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        FirebaseManager.getInstance().updateGroup(dialog.dialogID, groupName, tempFilePath, new FirebaseManager.OnUpdateFinishListener() {
            @Override
            public void onUpdate() {
                finish();
                progressDialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CAMERA_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        yourSelectedImage = (Bitmap) data.getExtras().get("data");
                        Uri tempUri = Util.getInstance().getImageUri(this, yourSelectedImage);
                        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                        File output = new File(dir, "camera.jpg");
                        Uri outputUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", output);
                        Crop.of(tempUri, outputUri).asSquare().start(this);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                break;

            default:
                if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
                    beginCrop(data.getData());
                } else if (requestCode == Crop.REQUEST_CROP) {
                    handleCrop(resultCode, data);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showAlertMenu() {
        overlayLayout.setVisibility(View.VISIBLE);
    }

    private void hideAlertMenu() {
        overlayLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    /**
     * This method is used to handle crop
     *
     * @param source
     */
    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    /*
     Handling crop functionality of user profile image
      */
    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            try {
                Uri uri = Crop.getOutput(result);
                yourSelectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                ivGroupPhoto.setImageBitmap(yourSelectedImage);
                Uri tempUri = Util.getInstance().getImageUri(this, yourSelectedImage);
                tempFilePath = FileUtil.getPath(this, tempUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            // Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(Object permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission((String) permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (Object perms : permissionsToRequest) {
                    if (hasPermission(perms)) {

                    } else {

                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale((String) permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                                //Log.d("API123", "permisionrejected " + permissionsRejected.size());

                                                requestPermissions((String[]) permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }

    public class GroupEditListAdapter extends BaseAdapter {
        Context context1;
        LayoutInflater inflter;

        public GroupEditListAdapter(Context context) {
            this.context1 = context;
            inflter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return Util.getInstance().workingGroupMember.size();
        }

        @Override
        public Object getItem(int i) {
            return Util.getInstance().workingGroupMember.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {

            view = inflter.inflate(R.layout.member_list_item, null);

            final GroupEditActivity.GroupEditListAdapter.ViewHolder viewHolder = new GroupEditActivity.GroupEditListAdapter.ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.tv_name);
            viewHolder.circleImageView = (CircleImageView) view.findViewById(R.id.profile_image);
            viewHolder.deleteImageView = (ImageView) view.findViewById(R.id.delete_img);

            viewHolder.deleteImageView.setVisibility(View.VISIBLE);
            viewHolder.deleteImageView.setTag(String.valueOf(position));

            User user = Util.getInstance().workingGroupMember.get(position);
//            if ((!TextUtils.isEmpty(dialog.adminId)) && dialog.adminId.equals(FirebaseManager.getInstance().getCurrentUserID())) {
//
//            }
            viewHolder.deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String strTag = (String) v.getTag();
                    int position = Integer.valueOf(strTag);
                    Util.getInstance().workingGroupMember.remove(position);

                    runOnUiThread(new Runnable() {

                        public void run() {
                            notifyDataSetChanged();
                        }
                    });
                }
            });

            viewHolder.name.setText(user.username);

            if (user.photo != null && user.photo.length() > 0) {
                Picasso.with(context1).load(user.photo).into(viewHolder.circleImageView);
            } else {
                viewHolder.circleImageView.setImageResource(R.mipmap.profile);
            }

            return view;
        }

        private class ViewHolder {
            public TextView name;
            public CircleImageView circleImageView;
            public ImageView deleteImageView;
        }
    }
}
