package com.mema.muslimkeyboard.activity.profile;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mema.muslimkeyboard.BuildConfig;
import com.mema.muslimkeyboard.R;
import com.mema.muslimkeyboard.activity.HomeActivity;
import com.mema.muslimkeyboard.constants.FileUtil;
import com.mema.muslimkeyboard.utility.FirebaseManager;
import com.mema.muslimkeyboard.utility.FontUtils;
import com.mema.muslimkeyboard.utility.Util;
import com.google.firebase.iid.FirebaseInstanceId;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.Manifest.permission.CAMERA;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = UserProfileActivity.class.getSimpleName();
    @BindView(R.id.txt_back)
    TextView txtBack;
    @BindView(R.id.img_userprofile)
    ImageView imgUserprofile;
    @BindView(R.id.txt_personname)
    EditText txtPersonname;
    @BindView(R.id.btn_next)
    Button btnNext;
    @BindView(R.id.userProfileLayout)
    LinearLayout userProfileLayout;
    @BindView(R.id.progressIndicator)
    ProgressBar progressDialog;
    @BindView(R.id.edt_email)
    EditText edtEmail;
    private BottomSheetDialog dialog;
    static final int CAMERA_REQUEST = 102;
    private static final int IMAGE_REQUEST = 101;
    private final static int ALL_PERMISSIONS_RESULT = 107;
    SharedPreferences sharedpreferences;
    Uri picUri;
    private ArrayList permissionsToRequest;
    private ArrayList permissionsRejected = new ArrayList();
    private ArrayList<String> permissions = new ArrayList<>();
    private Bitmap yourSelectedImage;
    private String tempFilePath = null;
    private String mPhoneNumber;
    public static final String MyPREFERENCES = "MyPrefs";
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);
        FontUtils.setFont(userProfileLayout, FontUtils.AvenirLTStdBook);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        mPhoneNumber = getIntent().getStringExtra(Intent.EXTRA_PHONE_NUMBER);
//        mPassword = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        permissions.add(CAMERA);
        permissionsToRequest = findUnAskedPermissions(permissions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
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

    public void OnNextClick(View view) {

    }

    @OnClick({R.id.txt_back, R.id.img_userprofile, R.id.btn_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.txt_back:
                finish();
                break;
            case R.id.img_userprofile:
                openImageSelectionSheet();
                break;
            case R.id.btn_next:
                signUpUserWIthPhoneAuth();
                break;
        }
    }

    private void signUpUserWIthPhoneAuth() {
        final String personName = txtPersonname.getText().toString();
        if (!TextUtils.isEmpty(personName)) {
            progressDialog.setVisibility(View.VISIBLE);
            FirebaseManager.getInstance().createAccountWithPhoneNumber(personName, mPhoneNumber, tempFilePath, new FirebaseManager.OnBooleanListener() {
                @Override
                public void onBooleanResponse(boolean success) {
                    if (success) {
                        progressDialog.setVisibility(View.INVISIBLE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("session", "login");
                        editor.putString("personName", personName);
                        editor.putString("push_token", FirebaseInstanceId.getInstance().getToken());
                        editor.apply();

                        FirebaseManager.getInstance().updatePushToken(FirebaseInstanceId.getInstance().getToken());

                        Intent i = new Intent(UserProfileActivity.this, HomeActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(UserProfileActivity.this, "SignUp failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } else {
            txtPersonname.setError("Please enter name");
            progressDialog.setVisibility(View.INVISIBLE);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {

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
                        Uri tempUri = Util.getInstance().getImageUri(UserProfileActivity.this, yourSelectedImage);
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

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void openImageSelectionSheet() {
        dialog = new BottomSheetDialog(UserProfileActivity.this);
        dialog.setContentView(R.layout.dialog_image_selection);
        LinearLayout dialogLayout = (LinearLayout) dialog.findViewById(R.id.imageSelectionDialog);
        FontUtils.setFont(dialogLayout, FontUtils.AvenirLTStdBook);
        dialog.show();
    }

    public void OnTakePhotoClick(View view) {
        dialogDismiss();
        Intent dataBack = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(dataBack, CAMERA_REQUEST);
    }

    public void OnChoosePhotoClick(View view) {
        dialogDismiss();
        Crop.pickImage(this);
    }

    public void OnCancelClick(View view) {
        dialogDismiss();
    }

    void dialogDismiss() {
        if (dialog.isShowing())
            dialog.dismiss();
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

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
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
                imgUserprofile.setImageBitmap(yourSelectedImage);
                Uri tempUri = Util.getInstance().getImageUri(UserProfileActivity.this, yourSelectedImage);
                tempFilePath = FileUtil.getPath(UserProfileActivity.this, tempUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            photoFile = createImageFile();
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(UserProfileActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        createImageFile());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }


    private File createImageFile() {
        File image = null;
        try {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM), "Camera");
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;

    }

}
