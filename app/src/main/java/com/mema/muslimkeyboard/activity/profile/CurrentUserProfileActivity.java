package com.mema.muslimkeyboard.activity.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.activity.BlockListActivity;
import com.mema.muslimkeyboard.activity.ChangePwdActivity;
import com.mema.muslimkeyboard.bean.User;
import com.mema.muslimkeyboard.constants.FileUtil;
import com.mema.muslimkeyboard.utility.BaseActivity;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.mema.muslimkeyboard.utility.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by Super on 5/16/2017.
 */

public class CurrentUserProfileActivity extends BaseActivity implements View.OnClickListener {
    public static final String MyPREFERENCES = "MyPrefs";
    static final int CAMERA_REQUEST = 102;
    ImageView imvAvatar;
    TextView tvFullname;
    User user;
    LinearLayout changePhotoLayout;
    RelativeLayout overlayLayout;
    private TextView tvPhoneNumber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_user_profile);

        initUI();
        getUserInfo();
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

        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.changePhotoLayout).setOnClickListener(this);
        findViewById(R.id.logoutLayout).setOnClickListener(this);
        changePhotoLayout = (LinearLayout) findViewById(R.id.changePhotoLayout);
        imvAvatar = (ImageView) findViewById(R.id.iv_avatar);
        tvFullname = (TextView) findViewById(R.id.tv_fullname);
        tvPhoneNumber = (TextView) findViewById(R.id.txt_phoneNumber);
//        registerForContextMenu(changePhotoLayout);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                this.onBackPressed();
                break;
            case R.id.changePhotoLayout:
                showAlertMenu();
                break;
            case R.id.logoutLayout:
                logOut();
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

    @Override
    public void onBackPressed() {
        if (overlayLayout.getVisibility() == View.VISIBLE) {
            hideAlertMenu();
            return;
        }
        super.onBackPressed();
    }

    private void showAlertMenu() {
        overlayLayout.setVisibility(View.VISIBLE);
    }

    private void hideAlertMenu() {
        overlayLayout.setVisibility(View.GONE);
    }

    private void getUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tvPhoneNumber.setText(user.getPhoneNumber());
            tvFullname.setText(user.getDisplayName());
            Picasso.with(CurrentUserProfileActivity.this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.mipmap.profile)
                    .error(R.mipmap.profile)
                    .into(imvAvatar);
        }
    }

    public void moveToChangePwdActivity() {
        Intent intent = new Intent(this, ChangePwdActivity.class);
        startActivity(intent);
    }

    private void logOut() {
        FirebaseManager.getInstance().signOut();

        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        /*
        user_pass
editor.putString("session", "login"); editor.putString("user_email", userName); editor.putString("user_pass", userPass); editor.putString("user_id", task.getResult().getUser().getUid());
         */
        editor.remove("user_pass");
        editor.remove("session");
        editor.remove("user_email");
        editor.remove("user_pass");
        editor.remove("user_id");
        editor.apply();
        setResult(RESULT_OK);
        finish();
    }

    private void changePhoto() {
        openContextMenu(changePhotoLayout);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Upload profile picture from");
        menu.add(0, v.getId(), 0, "Gallery");
        menu.add(0, v.getId(), 0, "Camera");
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
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CAMERA_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        Bitmap yourSelectedImage = (Bitmap) data.getExtras().get("data");
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
                final Bitmap yourSelectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Crop.getOutput(result));
                Uri tempUri = Util.getInstance().getImageUri(this, yourSelectedImage);
                String tempFilePath = FileUtil.getPath(this, tempUri);

                progressDialog.show();
                FirebaseManager.getInstance().updateProfilePhoto(tempFilePath, new FirebaseManager.OnBooleanListener() {
                    @Override
                    public void onBooleanResponse(boolean success) {
                        progressDialog.dismiss();
                        if (success) {
                            imvAvatar.setImageBitmap(yourSelectedImage);
                        }
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            // Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickBlockList(View view) {
//        FirebaseManager.getInstance().blockUser(String.valueOf(System.currentTimeMillis()));
        Intent intent = new Intent(CurrentUserProfileActivity.this, BlockListActivity.class);
        startActivity(intent);
    }
}
