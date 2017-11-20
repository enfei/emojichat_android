package com.mema.muslimkeyboard.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.constants.Constants;
import com.mema.muslimkeyboard.constants.FileUtil;
import com.mema.muslimkeyboard.utility.AppConst;
import com.mema.muslimkeyboard.utility.BaseActivity;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.mema.muslimkeyboard.utility.UsernameExistQueryEventListener;
import com.mema.muslimkeyboard.utility.Util;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import static android.Manifest.permission.CAMERA;
import static com.mema.muslimkeyboard.activity.SignIn.MyPREFERENCES;

/**
 * Created by cano-08 on 30/3/17.
 */

public class SignUp extends BaseActivity implements View.OnClickListener {
    static final int CAMERA_REQUEST = 102;
    private static final int IMAGE_REQUEST = 101;
    private final static int ALL_PERMISSIONS_RESULT = 107;
    SharedPreferences sharedpreferences;
    Uri picUri;
    TextView txtSubmit;
    RelativeLayout overlayLayout;
    private EditText edt_dob, edtFirstName, edtLastName, edtEmail, edtPassword, edtUserName, edtBirth;
    private ImageView imgBack, ivUserImage;
    private LinearLayout changePhotoLayout;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private String tempFilePath = "";
    private String strFirstName, strLastName, strUserName, strEmail, strPass, strBirthDate;
    private String cameraPicturePath = "", camImgFilename = "";
    private Bitmap yourSelectedImage;
    private int strUserId = 0;
    private ArrayList permissionsToRequest;
    private ArrayList permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();
    private AppConst appConst = new AppConst();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initView();

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        permissions.add(CAMERA);
        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
        edt_dob.setShowSoftInputOnFocus(false);
        edt_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpd = new DatePickerDialog(SignUp.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                edt_dob.setText(dayOfMonth + "-"
                                        + (monthOfYear + 1) + "-" + year);

                            }
                        }, mYear, mMonth, mDay);
                dpd.show();
            }
        });

    }

    @Override
    protected void initUI() {

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
            SignUp.this.startActivityForResult(dataBack, CAMERA_REQUEST);
            closeContextMenu();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void initView() {

        overlayLayout = (RelativeLayout) findViewById(R.id.overlayLayout);
        overlayLayout.setVisibility(View.GONE);

        /* Init Popup */

        findViewById(R.id.layoutTakePhoto).setOnClickListener(this);
        findViewById(R.id.layoutChoosePhoto).setOnClickListener(this);
        findViewById(R.id.layoutCancel).setOnClickListener(this);
        findViewById(R.id.maskView).setOnClickListener(this);

        edt_dob = (EditText) findViewById(R.id.edBday);
        edtFirstName = (EditText) findViewById(R.id.edFirstName);
        edtLastName = (EditText) findViewById(R.id.edLastName);
        edtEmail = (EditText) findViewById(R.id.edEmailName);
        edtPassword = (EditText) findViewById(R.id.edPassword);
        edtUserName = (EditText) findViewById(R.id.edUserName);
        edtBirth = (EditText) findViewById(R.id.edBday);

        txtSubmit = (TextView) findViewById(R.id.reg);
        imgBack = (ImageView) findViewById(R.id.iv_back);
        ivUserImage = (ImageView) findViewById(R.id.imUser);
        changePhotoLayout = (LinearLayout) findViewById(R.id.changePhotoLayout);
        changePhotoLayout.setOnClickListener(this);
        txtSubmit.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        findViewById(R.id.txtTerms).setOnClickListener(this);
        findViewById(R.id.txtPrivacy).setOnClickListener(this);

//        registerForContextMenu(changePhotoLayout);


    }

    @Override
    public void onBackPressed() {
        if (overlayLayout.getVisibility() == View.VISIBLE) {
            hideAlertMenu();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.changePhotoLayout:
                // Crop.pickImage(this);
//                openContextMenu(changePhotoLayout);
                showAlertMenu();
                //startActivityForResult(getPickImageChooserIntent(), 200);
                break;
            case R.id.iv_back:
                this.onBackPressed();
                break;
            case R.id.txtTerms: {
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.muslimemoji.com/terms"));
                startActivity(intent);
            }
            break;
            case R.id.txtPrivacy: {
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.muslimemoji.com/privacy"));
                startActivity(intent);
            }
            break;
            case R.id.reg:
                strFirstName = edtFirstName.getText().toString().trim();
                strLastName = edtLastName.getText().toString().trim();
                strEmail = edtEmail.getText().toString().trim();
                strPass = edtPassword.getText().toString().trim();
                strBirthDate = edtBirth.getText().toString().trim();
                strUserName = edtUserName.getText().toString().trim().toLowerCase();

                if (isValid()) {
                    progressDialog.show();

                    UsernameExistQueryEventListener completionHandler = new UsernameExistQueryEventListener() {
                        @Override
                        public void onUsernameQueryDone(Boolean bSuccess, String email) {
                            if (bSuccess) {
                                progressDialog.hide();
                                Toast.makeText(SignUp.this, "Username is already exist.", Toast.LENGTH_SHORT).show();
                            } else {
                                FirebaseManager.getInstance().createAccount(SignUp.this, strFirstName, strLastName, strEmail, strPass, strUserName, strBirthDate, tempFilePath,
                                        new FirebaseManager.OnBooleanListener() {
                                            @Override
                                            public void onBooleanResponse(boolean success) {
                                                progressDialog.dismiss();
                                                if (success) {
                                                    FirebaseDatabase.getInstance().getReference().child("usernameEmailLink").child(strUserName.toLowerCase()).setValue(strEmail);

                                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                                    editor.putString("session", "login");
                                                    editor.putString("user_email", strEmail);
                                                    editor.putString("user_pass", strPass);
                                                    editor.putString("push_token", FirebaseInstanceId.getInstance().getToken());
                                                    editor.apply();

                                                    FirebaseManager.getInstance().updatePushToken(FirebaseInstanceId.getInstance().getToken());

                                                    Intent i = new Intent(SignUp.this, HomeActivity.class);
                                                    startActivity(i);
                                                    finish();
                                                } else {
                                                    Toast.makeText(SignUp.this, "Signup failed", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }
                    };

                    FirebaseManager.getInstance().getEmailFromUsername(strUserName, completionHandler);

                }
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
            default: {

            }

        }
    }

    private void showAlertMenu() {
        overlayLayout.setVisibility(View.VISIBLE);
    }

    private void hideAlertMenu() {
        overlayLayout.setVisibility(View.GONE);
    }

    private boolean isValid() {
        if (strUserName != null && strUserName.equalsIgnoreCase("")) {
            Toast.makeText(SignUp.this, "Please enter user name", Toast.LENGTH_SHORT).show();
            return false;
        } else if (strEmail != null && strEmail.equalsIgnoreCase("")) {
            Toast.makeText(SignUp.this, "Please enter your email id", Toast.LENGTH_SHORT).show();
            return false;
        } else if (strPass != null && strPass.equalsIgnoreCase("")) {
            Toast.makeText(SignUp.this, "Please enter password", Toast.LENGTH_SHORT).show();
            return false;
        } else if (strEmail != null && !Constants.isValidEmail(strEmail)) {
            Toast.makeText(SignUp.this, "Please enter valid email id", Toast.LENGTH_SHORT).show();
            return false;
        } else if (strPass != null && strPass.length() < 7) {
            Toast.makeText(SignUp.this, "Password length can not be less than 8 character", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("pic_uri", picUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        picUri = savedInstanceState.getParcelable("pic_uri");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IMAGE_REQUEST:
                break;

            case CAMERA_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        yourSelectedImage = (Bitmap) data.getExtras().get("data");
                        Uri tempUri = Util.getInstance().getImageUri(SignUp.this, yourSelectedImage);
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
                yourSelectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Crop.getOutput(result));
                ivUserImage.setImageBitmap(yourSelectedImage);
                Uri tempUri = Util.getInstance().getImageUri(SignUp.this, yourSelectedImage);
                //  createImageFromBitmap(yourSelectedImage);
                //  tempFilePath = FileUtil.getPath(this,tempUri);
                tempFilePath = FileUtil.getPath(SignUp.this, tempUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            // Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
