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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mema.muslimkeyboard.R;
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

/**
 * Created by Super on 5/13/2017.
 */

public class NewGroupActivity extends BaseActivity implements View.OnClickListener {
    static final int CAMERA_REQUEST = 102;
    private final static int ALL_PERMISSIONS_RESULT = 107;

    ListView listView;
    EditText edtGroupName;
    ImageView imvGroupPhoto;
    TextView tvParticipants;
    TextView btnSave;
    MemberListAdapter adapter;
    Bitmap yourSelectedImage;
    String tempFilePath = "";
    ArrayList permissionsToRequest;
    ArrayList permissionsRejected = new ArrayList();
    ArrayList permissions = new ArrayList();

    RelativeLayout overlayLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        permissions.add(CAMERA);
        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }

        initUI();
    }

    @Override
    protected void initUI() {
        overlayLayout = (RelativeLayout) findViewById(R.id.overlayLayout);
        overlayLayout.setVisibility(View.GONE);

        /* Init Popup */

        findViewById(R.id.layoutTakePhoto).setOnClickListener(this);
        findViewById(R.id.layoutChoosePhoto).setOnClickListener(this);
        findViewById(R.id.layoutCancel).setOnClickListener(this);
        findViewById(R.id.maskView).setOnClickListener(this);

        /* */

        findViewById(R.id.ivBack).setOnClickListener(this);
        findViewById(R.id.tvSave).setOnClickListener(this);
        btnSave = (TextView) findViewById(R.id.tvSave);


        listView = (ListView) findViewById(R.id.list);
        tvParticipants = (TextView) findViewById(R.id.tvParticipants);
        edtGroupName = (EditText) findViewById(R.id.edtGroupName);
        edtGroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!edtGroupName.getText().toString().isEmpty()) {
                    btnSave.setTextColor(0xffffffff);
                } else {
                    btnSave.setTextColor(0x80ffffff);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        imvGroupPhoto = (ImageView) findViewById(R.id.iv_group_photo);
        imvGroupPhoto.setOnClickListener(this);
//        registerForContextMenu(imvGroupPhoto);

        adapter = new MemberListAdapter(this);
        listView.setAdapter(adapter);
        refreshMembers();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Upload profile picture from");
        menu.add(0, v.getId(), 0, "Gallery");
        menu.add(0, v.getId(), 0, "Camera");
        menu.add(0, v.getId(), 0, "Cancel");

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle() == "Gallery") {
            Crop.pickImage(this);
            return true;
        } else if (item.getTitle() == "Camera") {
            Intent dataBack = new Intent("android.media.action.IMAGE_CAPTURE");
            startActivityForResult(dataBack, CAMERA_REQUEST);
            closeContextMenu();
            return true;
        } else if (item.getTitle() == "Cancel") {
            closeContextMenu();
            return true;
        }
        return super.onContextItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivBack:
                this.onBackPressed();
                break;

            case R.id.tvSave:
                save();
                break;

            case R.id.iv_group_photo:
//                openContextMenu(imvGroupPhoto);
                showAlertMenu();
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
            case R.id.layoutCancel:
                hideAlertMenu();
                break;
            case R.id.maskView:
                hideAlertMenu();
                break;
            default:
                break;
        }
    }

    private void showAlertMenu() {
        overlayLayout.setVisibility(View.VISIBLE);
    }

    private void hideAlertMenu() {
        overlayLayout.setVisibility(View.GONE);
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
                imvGroupPhoto.setImageBitmap(yourSelectedImage);
                Uri tempUri = Util.getInstance().getImageUri(this, yourSelectedImage);
                tempFilePath = FileUtil.getPath(this, tempUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            // Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void refreshMembers() {
        adapter.notifyDataSetChanged();
        tvParticipants.setText("Participants: " + Util.getInstance().workingFriends.size());
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

    @Override
    public void onBackPressed() {
        if (overlayLayout.getVisibility() == View.VISIBLE) {
            hideAlertMenu();
            return;
        }
        super.onBackPressed();
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

    private void save() {
        String groupName = edtGroupName.getText().toString().trim();
        if (groupName.length() == 0) {
            Toast.makeText(this, "Please enter group name", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseManager.getInstance().createGroupDialog(groupName, tempFilePath);
        setResult(RESULT_OK);
        finish();
    }

    public class MemberListAdapter extends BaseAdapter {
        Context context1;
        LayoutInflater inflter;

        public MemberListAdapter(Context context) {
            this.context1 = context;
            inflter = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return Util.getInstance().workingFriends.size();
        }

        @Override
        public Object getItem(int i) {
            return Util.getInstance().workingFriends.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {

            view = inflter.inflate(R.layout.member_list_item, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.tv_name);
            viewHolder.circleImageView = (CircleImageView) view.findViewById(R.id.profile_image);
            viewHolder.deleteImageView = (ImageView) view.findViewById(R.id.delete_img);

            User user = Util.getInstance().workingFriends.get(position);
            viewHolder.name.setText(user.username);

            if (user.photo != null && user.photo.length() > 0) {
                Picasso.with(context1).load(user.photo).into(viewHolder.circleImageView);
            } else {
                viewHolder.circleImageView.setImageResource(R.mipmap.profile);
            }

            viewHolder.deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Util.getInstance().removeFriend(Util.getInstance().workingFriends.get(position));
                    refreshMembers();
                }
            });

            return view;
        }

        private class ViewHolder {
            public TextView name;
            public CircleImageView circleImageView;
            public ImageView deleteImageView;
        }

    }


}
