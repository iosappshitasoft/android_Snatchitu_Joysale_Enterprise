package com.ni.SnatchItUp;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.os.Build.VERSION.SDK_INT;

import static com.ni.external.multipleimageselect.helpers.Constants.REQUEST_CODE;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ni.external.imagepicker.ImagePicker;
import com.ni.external.multipleimageselect.activities.AlbumSelectActivity;
import com.ni.external.multipleimageselect.models.Photo;
import com.ni.helper.ImageCompression;
import com.ni.helper.ImageStorage;
import com.ni.helper.LocaleManager;
import com.ni.helper.NetworkReceiver;
import com.ni.helper.PermissionsUtils;
import com.ni.helper.callback.OkayCancelCallback;
//import com.ni.mapbox.MainActivity;
import com.ni.model.DeleteResponse;
import com.ni.model.HomeItemResponse;
import com.ni.model.ProfileResponse;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hitasoft on 11/6/16.
 * <p>
 * This class is for Edit User Profile
 */

public class EditProfile extends BaseActivity implements View.OnClickListener, TextWatcher {

    private final static String TAG = EditProfile.class.getSimpleName();
    Context context;
    /**
     * Declare Layout Elements
     **/
    public ImageView logout, backBtn, userImage, btnLocation, deleteimage;
    LinearLayout logoutLay, parentLay, callLay, mailVerifyLay, deleteLay;
    RelativeLayout changepassword, editphoto, languageLay, mobileLayout, stripeLay;
    ImageView mailverifiedIcon, mobilverifiedIcon, fbverifiedIcon, imagebtn, langbtn, passbtn, btnStripe;
    TextView title, mobilverified, mailverified, fbverified, linkfb, save, language, showphoneno, verify;
    EditText username, name, email, edtLocation, edtStripe;
    TextInputLayout passwrdLay, locationInputLay;
    TextView blockMsg;

    AVLoadingIndicatorView progress;
    Dialog dialog;
    SwitchCompat callSwitch;
Double latitude =0.0;
Double longitude =0.0;
    /**
     * Declare Variables
     **/
    private static final int RC_SIGN_IN = 100;
    int count;
    String fullname = "", facebookid = "", uploadedImage = "", viewUrl = "", confirmedPhone = "", showPhone = "";
    //    CallbackManager callbackManager;
    HashMap<String, String> fbData = new HashMap<String, String>();
    ApiInterface apiInterface;
    String cityName = "", stateName = "", countryName = "";
    private ProfileResponse.Result profileResult;
    private boolean isRTL = false;
    private ImageStorage imageStorage;
    public static Boolean deleteaccount = false;
    private ActivityResultLauncher<String[]> locations;
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );
    private ActivityResultLauncher<String[]> galleryResultLauncher;
    private ActivityResultLauncher<String[]> cameraResultLauncher;

    private File currentPhotoFile;
    private Uri mCurrentPhotoUri;
    private static final int REQUEST_CODE_IMAGE_INTENT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editprofile);
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        context = this;
        imageStorage = new ImageStorage(EditProfile.this);
        isRTL = LocaleManager.isRTL(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        backBtn = (ImageView) findViewById(R.id.backbtn);
        title = (TextView) findViewById(R.id.title);
        logout = (ImageView) findViewById(R.id.logout);
        editphoto = (RelativeLayout) findViewById(R.id.editphoto);
        changepassword = (RelativeLayout) findViewById(R.id.changePassword);
        logoutLay = (LinearLayout) findViewById(R.id.logoutLay);
        username = (EditText) findViewById(R.id.user_name);
        name = (EditText) findViewById(R.id.name);
        passwrdLay = (TextInputLayout) findViewById(R.id.passwordInput);
        email = (EditText) findViewById(R.id.emailid);
        userImage = (ImageView) findViewById(R.id.user_image);
        mobilverified = (TextView) findViewById(R.id.mobilverified);
        mailverified = (TextView) findViewById(R.id.mailverified);
        mailVerifyLay = findViewById(R.id.mailVerify);
        fbverified = (TextView) findViewById(R.id.fbverified);
        linkfb = (TextView) findViewById(R.id.linkfb);
        save = (TextView) findViewById(R.id.save);
        languageLay = (RelativeLayout) findViewById(R.id.languageLay);
        language = (TextView) findViewById(R.id.language);
        parentLay = (LinearLayout) findViewById(R.id.parentLay);
        progress = (AVLoadingIndicatorView) findViewById(R.id.progress);
        showphoneno = (TextView) findViewById(R.id.phoneno);
        verify = (TextView) findViewById(R.id.verify);
        mailverifiedIcon = (ImageView) findViewById(R.id.mailverifiedIcon);
        mobilverifiedIcon = (ImageView) findViewById(R.id.mobilverifiedIcon);
        fbverifiedIcon = (ImageView) findViewById(R.id.fbverifiedIcon);
        callLay = (LinearLayout) findViewById(R.id.callLay);
        callSwitch = (SwitchCompat) findViewById(R.id.callSwitch);
        imagebtn = (ImageView) findViewById(R.id.imagebtn);
        langbtn = (ImageView) findViewById(R.id.langbtn);
        passbtn = (ImageView) findViewById(R.id.passbtn);
        mobileLayout = (RelativeLayout) findViewById(R.id.mobileLayout);
        locationInputLay = findViewById(R.id.locationInputLay);
        edtLocation = findViewById(R.id.edtLocation);
        btnLocation = findViewById(R.id.btnLocation);
        stripeLay = findViewById(R.id.stripeLay);
        edtStripe = findViewById(R.id.edtStripe);
        btnStripe = findViewById(R.id.btnStripe);
        deleteimage = findViewById(R.id.deleteImage);
        deleteLay = findViewById(R.id.deleteAccount);
        backBtn.setVisibility(View.VISIBLE);
        title.setVisibility(View.VISIBLE);
        blockMsg = findViewById(R.id.blockMsg);

        title.setText(getString(R.string.edit_profile));
        passwrdLay.setHint(getResources().getString(R.string.changepassword).toUpperCase());

        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref",
                MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        //email enable/disable
        if (!Constants.pref.getBoolean(Constants.PREF_EMAILVERIFICATION, Constants.EMAILVERIFICATION)) {
            mailVerifyLay.setVisibility(View.GONE);
        } else {
            mailVerifyLay.setVisibility(View.VISIBLE);
        }

        progress.setVisibility(View.VISIBLE);
        parentLay.setVisibility(View.GONE);
        save.setVisibility(View.GONE);

        getProfileInformation();

        loginToFacebook();

        backBtn.setOnClickListener(this);
        changepassword.setOnClickListener(this);
        editphoto.setOnClickListener(this);
        imagebtn.setOnClickListener(this);
        logoutLay.setOnClickListener(this);
        linkfb.setOnClickListener(this);
        save.setOnClickListener(this);
        verify.setOnClickListener(this);
        languageLay.setOnClickListener(this);
        mobileLayout.setOnClickListener(this);
        locationInputLay.setOnClickListener(this);
        edtLocation.setOnClickListener(this);
        btnLocation.setOnClickListener(this);
        edtStripe.setOnClickListener(this);
        deleteLay.setOnClickListener(this);

        btnStripe.setOnClickListener(this);

        name.setFilters(new InputFilter[]{JoysaleApplication.EMOJI_FILTER, new InputFilter.LengthFilter(30)});

        if (isRTL) {
            imagebtn.setRotation(180);
            langbtn.setRotation(180);
            passbtn.setRotation(180);
            btnLocation.setRotation(180);
            logout.setRotation(180);
            btnStripe.setRotation(180);
            name.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            username.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            email.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            showphoneno.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        } else {
            imagebtn.setRotation(0);
            langbtn.setRotation(0);
            passbtn.setRotation(0);
            btnLocation.setRotation(0);
            btnStripe.setRotation(0);
            logout.setRotation(0);
            name.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            username.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            email.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            showphoneno.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        }

        if (Constants.BUYNOW) {
            stripeLay.setVisibility(View.VISIBLE);
        } else {
            stripeLay.setVisibility(View.GONE);
        }
        initLauncher();


        Constants.mapboxpref= getApplicationContext().getSharedPreferences("Mapboxpref",
                MODE_PRIVATE);
        Constants.mapboxeditor = Constants.mapboxpref.edit();
    }

    /**
     * set the information to elements
     *
     * @param profile
     */

    private void setProfileInformation(ProfileResponse.Result profile) {
        try {
            language.setText(Constants.pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE));
            username.setText(profile.getUserName());
            name.setText(profile.getFullName());
            fullname = profile.getFullName();
            email.setText(profile.getEmail());
            viewUrl = profile.getUserImg();
            edtLocation.setText(profile.getLocation() != null ? profile.getLocation() : "");
            Picasso.get().load(viewUrl).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(userImage);

            cityName = profile.getCity();
            stateName = profile.getState();
            countryName = profile.getCountry();
            if (profile.getVerification().getFacebook().equals("true")) {
                fbverified.setText(getString(R.string.verified));
                fbverifiedIcon.setImageResource(R.drawable.tick_green);
                linkfb.setEnabled(false);
                linkfb.setVisibility(View.GONE);
            } else {
                fbverified.setText(getString(R.string.unverified));
                fbverifiedIcon.setImageResource(R.drawable.cancel);
                fbverifiedIcon.setColorFilter(getResources().getColor(R.color.red));
                linkfb.setVisibility(View.VISIBLE);
                linkfb.setEnabled(true);
            }
            if (profile.getVerification().getMobNo().equals("true")) {
                verify.setText(getString(R.string.change));
                mobilverified.setText(getString(R.string.verified));
                mobilverifiedIcon.setImageResource(R.drawable.tick_green);
                showphoneno.setVisibility(View.VISIBLE);
                showphoneno.setText(profile.getMobileNo());
                mobilverified.setEnabled(true);
                mobilverifiedIcon.setImageResource(R.drawable.tick_green);
                callLay.setVisibility(View.VISIBLE);
            } else {
                verify.setText(getString(R.string.verify));
                mobilverified.setText(getString(R.string.unverified));
                mobilverifiedIcon.setImageResource(R.drawable.cancel);
                mobilverifiedIcon.setColorFilter(getResources().getColor(R.color.red));
                mobilverified.setEnabled(false);
                callLay.setVisibility(View.GONE);
            }
            if (profile.getVerification().getEmail().equals("true")) {
                mailverified.setText(getString(R.string.verified));
                mailverifiedIcon.setImageResource(R.drawable.tick_green);
            } else {
                mailverified.setText(getString(R.string.unverified));
                mailverifiedIcon.setImageResource(R.drawable.cancel);
                mailverifiedIcon.setColorFilter(getResources().getColor(R.color.red));
            }

            if (profile.getShowMobileNo().equals("true")) {
                callSwitch.setChecked(true);
            } else {
                callSwitch.setChecked(false);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * dialog for confirm the user to signout
     **/

    public void signOutDialog() {
        final Dialog dialog = new Dialog(context, R.style.AlertDialog);
        Display display = getWindowManager().getDefaultDisplay();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog.getWindow().setLayout(displayMetrics.widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        ImageView alertIcon = (ImageView) dialog.findViewById(R.id.alert_icon);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);
        TextView alertCancel = (TextView) dialog.findViewById(R.id.cancel_button);

        alertMsg.setText(getString(R.string.reallySignOut));
        alertOk.setText(getString(R.string.yes));
        alertCancel.setText(getString(R.string.no));

        alertCancel.setVisibility(View.VISIBLE);

        alertOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                userSignout();
                String selectedLang = Constants.pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE);
                String selectedLangCode = Constants.pref.getString(Constants.PREF_LANGUAGE_CODE, Constants.LANGUAGE_CODE);
                Log.i(TAG, "onClick: " + selectedLang);
                Log.i(TAG, "onClick: " + selectedLangCode);
                Constants.editor.clear();
                Constants.editor.putString(Constants.PREF_LANGUAGE, selectedLang);
                Constants.editor.putString(Constants.PREF_LANGUAGE_CODE, selectedLangCode);
                Constants.editor.apply();
                GetSet.reset();
                FragmentMainActivity.homeItemList.clear();
                if (FragmentMainActivity.itemAdapter != null) {
                    FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                }
                WelcomeActivity.fromSignout = true;
                finish();
                Intent p = new Intent(context, WelcomeActivity.class);
                p.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(p);
            }
        });

        alertCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    /**
     * Function for User Sign out
     **/

    private void userSignout() {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_DEVICE_ID, Constants.ANDROID_ID);

            Call<ResponseBody> call = apiInterface.removeDeviceId(map);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    call.cancel();
                }
            });

        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mobilverified.setText(getString(R.string.change));
        mobilverified.setEnabled(true);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult1111111: "+resultCode+"  "+requestCode+"  "+data);
       if(resultCode==-1 && requestCode == 2000){
           ArrayList<Photo> photoList = (ArrayList<Photo>) data.getExtras().get("galleryData");
           Log.d(TAG, "onActivityResult: "+photoList);
           for (Photo selectedImage : photoList) {
               HomeItemResponse.Photo photo = new HomeItemResponse().new Photo();
               photo.setType(selectedImage.getType());
               photo.setImage("" + selectedImage.getImage());
               photo.setPathType(Constants.TAG_GALLERY);

               Log.e("selectedImage", "-" + selectedImage.getImage());
               ImageCompression imageCompression = new ImageCompression(EditProfile.this) {
                   @Override
                   protected void onPostExecute(String imagePath) {
                       new UploadProfileImage().execute(imagePath);
                   }
               };
               imageCompression.execute(selectedImage.getImage());
           }
       }
       else if (resultCode == -1 && requestCode == 234) {
            final File file = new File(ImagePicker.getImageFilePath(this, requestCode, resultCode, data));
            if (file.exists()) {
                String filepath = file.getAbsolutePath();
                Log.i(TAG, "selectedImageFile: " + filepath);
                ImageCompression imageCompression = new ImageCompression(EditProfile.this) {
                    @Override
                    protected void onPostExecute(String imagePath) {
                        new UploadProfileImage().execute(imagePath);
                    }
                };
                imageCompression.execute(filepath);
            } else {
                Toast.makeText(context, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == Constants.LOCATION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (Constants.ADDON_MAP_BOX_ENABLED) {
                    cityName = data.getStringExtra(Constants.TAG_CITY_NAME);
                    stateName = data.getStringExtra(Constants.TAG_STATE_NAME);
                    countryName = data.getStringExtra(Constants.TAG_COUNTRY_NAME);
                    latitude = data.getDoubleExtra(Constants.TAG_LAT,0);
                    longitude = data.getDoubleExtra(Constants.TAG_LON,0);
                    edtLocation.setText("" + cityName + ", " + stateName + ", " + countryName);
                    Constants.mapboxeditor.putString(Constants.TAG_LAT, "" + data.getDoubleExtra(Constants.TAG_LAT,0));
                    Constants.mapboxeditor.putString(Constants.TAG_LON, "" + data.getDoubleExtra(Constants.TAG_LON,0));
                    Constants.mapboxeditor.putString(Constants.TAG_LOCATION, data.getStringExtra(Constants.TAG_LOCATION));
                    Constants.mapboxeditor.apply();
                    Constants.mapboxeditor.commit();
                    Log.d(TAG, "onActivityResult: "+cityName+stateName+countryName+latitude+longitude);

                } else {
                    cityName = data.getStringExtra(Constants.TAG_CITY_NAME);
                    stateName = data.getStringExtra(Constants.TAG_STATE_NAME);
                    countryName = data.getStringExtra(Constants.TAG_COUNTRY_NAME);
                    edtLocation.setText("" + cityName + ", " + stateName + ", " + countryName);
                }
            }
        } else if (requestCode == Constants.STRIPE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (profileResult != null) {
                    profileResult.getStripeDetails().setStripePrivateKey(data.getStringExtra(Constants.TAG_STRIPE_PRIVATEKEY));
                    profileResult.getStripeDetails().setStripePublicKey(data.getStringExtra(Constants.TAG_STRIPE_PUBLICKEY));
                }
            }
        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_IMAGE_INTENT) {

            // CAMERA image
            if (currentPhotoFile != null && currentPhotoFile.exists()) {
                handleImagePath(currentPhotoFile.getAbsolutePath());
                return;
            }

            // GALLERY image
            if (data != null && data.getData() != null) {
                Uri imageUri = data.getData();
                handleImageUri(imageUri);
                return;
            }

            Toast.makeText(this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
        }
        /*if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }*/
    }
    private void handleImageUri(Uri uri) {
        try {
            File file = createImageFile();

            InputStream inputStream = getContentResolver().openInputStream(uri);
            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();

            handleImagePath(file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImagePath(String filepath) {
        ImageCompression imageCompression = new ImageCompression(EditProfile.this) {
            @Override
            protected void onPostExecute(String imagePath) {
                new UploadProfileImage().execute(imagePath);
            }
        };
        imageCompression.execute(filepath);
    }

    /**
     * for fb confirmation
     **/
    public void loginToFacebook() {/*
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {

                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.v("loginToFacebook", "onSuccess");
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject profile,
                                                            GraphResponse response) {
                                        Log.v("json", "object" + profile);
                                        // Application code
                                        try {
                                            String email = "";
                                            if (profile.has(Constants.TAG_EMAIL)) {
                                                email = profile.getString(Constants.TAG_EMAIL);
                                            } else {
                                                email = "";
                                            }

                                            fbData.put("id", profile.getString("id"));
                                            fbData.put(Constants.TAG_EMAIL, email);
                                            fbData.put("first_name", profile.getString("first_name"));
                                            fbData.put("last_name", profile.getString("last_name"));
                                            fbData.put("profile_url", "https://www.facebook.com/app_scoped_user_id/" + profile.getString("id") + "/");

                                            facebookid = profile.getString("id");
                                            runOnUiThread(new Runnable() {

                                                @SuppressWarnings("unchecked")
                                                @Override
                                                public void run() {
                                                    if (dialog != null && dialog.isShowing()) {
                                                        dialog.dismiss();
                                                    }
                                                    editUserProfile("facebook");
                                                    //new EditProfile.Editprofile("facebook").execute();

                                                }
                                            });
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,first_name,last_name");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(context, "Facebook - Cancelled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.v(TAG, "loginToFacebook-onError=" + exception);
                        Toast.makeText(context, "Facebook - " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        if (exception instanceof FacebookAuthorizationException) {
                            if (com.facebook.AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();
                            }
                        }
                    }
                });
    */
    }

    @Override
    public void onBackPressed() {
        if (profileResult != null) {
            Intent intent = new Intent();
            intent.putExtra(Constants.TAG_DATA, profileResult);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (EditProfilePhoto.editPhoto) {
            EditProfilePhoto.editPhoto = false;
            new UploadProfileImage().execute(EditProfilePhoto.imgPath);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (checkPermissions(permissions, EditProfile.this)) {
                ImagePicker.pickImage(this, getString(R.string.select_your_image));
            } else {
                if (shouldShowRationale(permissions, EditProfile.this)) {
                    ActivityCompat.requestPermissions(EditProfile.this, permissions, 100);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivityForResult(intent, 100);
                }
            }
        }
    }

    private boolean checkPermissions(String[] permissionList, EditProfile activity) {
        boolean isPermissionsGranted = false;
        for (String permission : permissionList) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                isPermissionsGranted = true;
            } else {
                isPermissionsGranted = false;
                break;
            }
        }
        return isPermissionsGranted;
    }

    private boolean shouldShowRationale(String[] permissions, EditProfile activity) {
        boolean showRationale = false;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                showRationale = true;
            } else {
                showRationale = false;
                break;
            }
        }
        return showRationale;
    }

    /**
     * Function for get profile information of user
     **/

    private void getProfileInformation() {
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());

            Call<ProfileResponse> call = apiInterface.getProfileInformation(map);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, retrofit2.Response<ProfileResponse> response) {
                    if (response.isSuccessful()) {
                        ProfileResponse profile = response.body();
                        if (profile.getStatus().equals("true")) {
                            profileResult = profile.getResult();
                            if (profileResult.getUserId().equalsIgnoreCase(GetSet.getUserId())) {
                                save.setVisibility(View.VISIBLE);
                                parentLay.setVisibility(View.VISIBLE);
                                GetSet.setStripePrivateKey(profileResult.getStripeDetails().getStripePrivateKey());
                                GetSet.setStripePublicKey(profileResult.getStripeDetails().getStripePublicKey());
                                setProfileInformation(profileResult);
                            }
                        } else if (profile.getStatus().equalsIgnoreCase("error")) {
                            JoysaleApplication.disabledialog(EditProfile.this, "" + profile.getMessage(), GetSet.getUserId());
                        } else if (profile.getStatus().equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                            JoysaleApplication.disabledialog(EditProfile.this, "" + profile.getMessage(), GetSet.getUserId());
                        } else {
                            Toast.makeText(context, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    }
                    progress.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    Toast.makeText(context, getString(R.string.somethingwrong), Toast.LENGTH_SHORT).show();
                    call.cancel();
                }
            });
        }
    }

    /**
     * save the edited details to server
     **/

    private void editUserProfile(final String from) {
        String phoneNumber = confirmedPhone.trim();
        String phone = phoneNumber.replace("+", "");
        if (NetworkReceiver.isConnected()) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.TAG_FULL_NAME, fullname);
            map.put(Constants.TAG_USERIMG, uploadedImage);
            map.put(Constants.TAG_FACEBOOK_ID, facebookid);
            map.put(Constants.TAG_MOBILE_NO, phone);
            map.put(Constants.TAG_SHOW_MOBILE_NO, showPhone);
            map.put(Constants.TAG_CITY_NAME, cityName != null ? cityName : "");
            map.put(Constants.TAG_STATE_NAME, stateName != null ? stateName : "");
            map.put(Constants.TAG_COUNTRY_NAME, countryName != null ? countryName : "");
            if (fbData.size() > 0) {
                map.put(Constants.TAG_FB_EMAIL, fbData.get(Constants.TAG_EMAIL));
                map.put(Constants.TAG_FB_FIRSTNAME, fbData.get(Constants.TAG_FIRST_NAME));
                map.put(Constants.TAG_FB_LASTNAME, fbData.get(Constants.TAG_LAST_NAME));
                map.put(Constants.TAG_FB_PHONE, "");
                map.put(Constants.TAG_FB_PROFILEURL, fbData.get(Constants.TAG_PROFILE_URL));
            }

            Call<ProfileResponse> call = apiInterface.editProfile(map);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, retrofit2.Response<ProfileResponse> response) {
                    try {
                        if (response.isSuccessful()) {
                            String status = response.body().getStatus();
                            if (status.equalsIgnoreCase("true")) {
                                profileResult = response.body().getResult();
                                GetSet.setFullName(fullname);
                                GetSet.setImageUrl(viewUrl);
                                if (FragmentMainActivity.userImage != null && FragmentMainActivity.username != null) {
                                    FragmentMainActivity.username.setText(GetSet.getFullName());
                                    Picasso.get().load(viewUrl).placeholder(R.drawable.appicon).error(R.drawable.appicon).into(FragmentMainActivity.userImage);
                                }
                                Constants.editor.putString(Constants.TAG_PHOTO, viewUrl);
                                Constants.editor.putString(Constants.TAG_FULL_NAME, fullname);
                                Constants.editor.commit();

                                if (!from.equals("facebook")) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.your_changes_saved), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent();
                                    intent.putExtra(Constants.TAG_DATA, profileResult);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                } else {
                                    fbverified.setText(getString(R.string.verified));
                                    fbverifiedIcon.setImageResource(R.drawable.tick_green);
                                    linkfb.setEnabled(false);
                                    linkfb.setVisibility(View.GONE);
                                }
                            } else if (status.equalsIgnoreCase("false")) {
                                if (response.body().getMessage().equalsIgnoreCase("Mobile number already exists")) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.number_already_exists_various), Toast.LENGTH_SHORT).show();
                                }
                            } else if (status.equalsIgnoreCase(Constants.TAG_FALSE_CODE)) {
                                JoysaleApplication.disabledialog(EditProfile.this, "" + response.body().getMessage(), GetSet.getUserId());
                            }

                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    /**
     * Function to Verify Given Number using Account Kit
     **/

    public void verifyMobileNo(View v) {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder()
                        .build()
        );
        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.OTPTheme)
                .build();
        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getPhoneNumber() != null) {
                confirmedPhone = user.getPhoneNumber();
                mobilverified.setText(getString(R.string.verified));
                mobilverifiedIcon.setImageResource(R.drawable.tick_green);
                showphoneno.setVisibility(View.VISIBLE);
                showphoneno.setText(confirmedPhone);
                verify.setText(getResources().getString(R.string.change));
                if (!TextUtils.isEmpty(fullname)) {
                    editUserProfile("otherdetails");
                } else {
                    Toast.makeText(context, getString(R.string.please_enter_name), Toast.LENGTH_SHORT).show();
                }
            } else {
                mobilverified.setText(getString(R.string.unverified));
                mobilverifiedIcon.setImageResource(R.drawable.cancel);
                mobilverifiedIcon.setColorFilter(getResources().getColor(R.color.red));
            }
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
        }
    }

    /**
     * class for upload user image
     **/

    @SuppressLint("StaticFieldLeak")
    class UploadProfileImage extends AsyncTask<String, Integer, Integer> {
        JSONObject jsonobject = null;
        String Json = "", status, message = "";
        ProgressDialog pd;

        @Override
        protected Integer doInBackground(String... imgpath) {
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            DataInputStream inStream = null;
            StringBuilder builder = new StringBuilder();
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            String urlString = Constants.API_UPLOAD_IMAGE;
            try {
                String existingFileName = imgpath[0];
                Log.v(" existingFileName", existingFileName);
                FileInputStream fileInputStream = new FileInputStream(new File(existingFileName));
                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);
                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data;name=\"type\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes("user");
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"images\";filename=\""
                        + existingFileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                Log.e("MediaPlayer", "Headers are written");
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null)
                    builder.append(inputLine);

                Log.e("MediaPlayer", "File is written");
                fileInputStream.close();
                Json = builder.toString();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                Log.e("MediaPlayer", "error: " + ex.getMessage(), ex);
            } catch (IOException ioe) {
                Log.e("MediaPlayer", "error: " + ioe.getMessage(), ioe);
            }
            try {
                inStream = new DataInputStream(conn.getInputStream());
                String str;
                while ((str = inStream.readLine()) != null) {
                    Log.e("MediaPlayer", "Server Response" + str);
                }
                inStream.close();
            } catch (IOException ioex) {
                Log.e("MediaPlayer", "error: " + ioex.getMessage(), ioex);
            }
            try {
                jsonobject = new JSONObject(Json);
                Log.v("json", "json" + Json);
                status = jsonobject.getString(Constants.TAG_STATUS);
                if (jsonobject.has("message")) {
                    message = jsonobject.getString("message");
                }
                if (status.equals("true")) {
                    JSONObject image = jsonobject.getJSONObject("Image");
                    String msg = image.getString("Message");
                    uploadedImage = image.getString("Name");
//                    viewUrl = Constants.url + "user/resized/150/" + uploadedImage;
                    viewUrl = image.optString("View_url", "");
                } else if (status.equals("false")) {
                    Toast.makeText(getApplicationContext(), "please upload a valid image", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                status = "false";
                e.printStackTrace();
            } catch (NullPointerException e) {
                status = "false";
                e.printStackTrace();
            } catch (Exception e) {
                status = "false";
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(EditProfile.this, R.style.AppCompatAlertDialogStyle);
            pd.setMessage(getString(R.string.loading));
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                Drawable drawable = new ProgressBar(EditProfile.this).getIndeterminateDrawable().mutate();
                drawable.setColorFilter(ContextCompat.getColor(EditProfile.this, R.color.progressColor),
                        PorterDuff.Mode.SRC_IN);
                pd.setIndeterminateDrawable(drawable);
            }
            pd.show();
        }

        @Override
        protected void onPostExecute(Integer unused) {
            if (status.equalsIgnoreCase("true")) {
                if(!viewUrl.isEmpty()){
                Picasso.get().load(viewUrl).into(userImage);}
                pd.dismiss();
            } else if (status.equalsIgnoreCase("false")) {
                if (message.equalsIgnoreCase("Image cannot be uploaded")) {
                    pd.dismiss();
                    JoysaleApplication.uploadAlertDialog(EditProfile.this, getString(R.string.alert), getString(R.string.image_cannot_moderator));

                }

            }

        }

    }

    /**
     * Onclick Event
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save:
                fullname = name.getText().toString();
                if (callSwitch.isChecked()) {
                    showPhone = "true";
                } else {
                    showPhone = "false";
                }
                if (!TextUtils.isEmpty(fullname)) {
                    editUserProfile("otherdetails");
                } else {
                    Toast.makeText(this, getString(R.string.please_enter_name), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.editphoto:
            case R.id.imagebtn:
                if (SDK_INT>=Build.VERSION_CODES.TIRAMISU){
                    if (ContextCompat.checkSelfPermission(EditProfile.this, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
                    {
                        galleryResultLauncher.launch(new String[]{READ_MEDIA_IMAGES});
                    }
                    else{
                        pickImage();
                    }
                    //readPermission = ContextCompat.checkSelfPermission(this, READ_MEDIA_IMAGES);

                }
               else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (ContextCompat.checkSelfPermission(EditProfile.this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(EditProfile.this, new String[]{CAMERA}, 100);
                    } else {
                        ImagePicker.pickImage(this, "Select your image:");
                    }
                } else {
                  /*  if (ContextCompat.checkSelfPermission(EditProfile.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(EditProfile.this, WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(EditProfile.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 100);
                    } else {
                        ImagePicker.pickImage(this, "Select your image:");
                    }*/

                }

                break;
            case R.id.changePassword:
                Intent j = new Intent(context, ChangePassword.class);
                startActivity(j);
                break;
            case R.id.deleteAccount:
                openDeleteDialog();
                break;
            case R.id.logoutLay:
                signOutDialog();
                break;
            case R.id.backbtn:
                onBackPressed();
                break;
            case R.id.verify:
            case R.id.mobileLayout:
                verifyMobileNo(v);
                break;
            case R.id.linkfb:
//                LoginManager.getInstance().logInWithReadPermissions(EditProfile.this, Arrays.asList("public_profile", "email"));
                break;
            case R.id.languageLay:
                Intent i = new Intent(context, LanguageActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                i.putExtra(Constants.TAG_FROM, "EditProfile");
                startActivity(i);
                break;
            case R.id.locationInputLay:
            case R.id.edtLocation:
            case R.id.btnLocation:
                //Addon Mapbox
                if (Constants.ADDON_MAP_BOX_ENABLED) {
                /*    if (ContextCompat.checkSelfPermission(this,ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED&&ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
locations.launch(PermissionsUtils.LOCATIONS);
                    }else {
                        Intent location = new Intent(context, MainActivity.class);
                        location.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                        location.putExtra(Constants.TAG_LOCATION, Constants.mapboxpref.getString(Constants.TAG_LOCATION,""));
                        location.putExtra(Constants.TAG_LAT,Double.parseDouble(Constants.mapboxpref.getString(Constants.TAG_LAT, "0")));
                        location.putExtra(Constants.TAG_LON,Double.parseDouble(Constants.mapboxpref.getString(Constants.TAG_LON,"0")));
                        location.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivityForResult(location, Constants.LOCATION_REQUEST_CODE);
                    }*/
                } else {
                    Intent location = new Intent(context, LocationActivity.class);
                    location.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                    location.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivityForResult(location, Constants.LOCATION_REQUEST_CODE);
                }
                break;
            case R.id.stripeLay:
            case R.id.edtStripe:
            case R.id.btnStripe:
                Intent stripe = new Intent(context, ManageStripeActivity.class);
                stripe.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                stripe.putExtra(Constants.TAG_FROM, Constants.TAG_PROFILE);
                if (GetSet.getStripePrivateKey() != null) {
                    stripe.putExtra(Constants.TAG_STRIPE_PRIVATEKEY, GetSet.getStripePrivateKey());
                    stripe.putExtra(Constants.TAG_STRIPE_PUBLICKEY, GetSet.getStripePublicKey());
                }
                startActivityForResult(stripe, Constants.STRIPE_REQUEST_CODE);
                break;
        }
    }


    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(EditProfile.this, isConnected);
    }

    private void deleteAccount() {

        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.TAG_USERID, GetSet.getUserId());
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_DEVICE_ID, Constants.ANDROID_ID);
            Call<DeleteResponse> call = apiInterface.deleteAccount(map);
            call.enqueue(new Callback<DeleteResponse>() {


                @Override
                public void onResponse(Call<DeleteResponse> call, Response<DeleteResponse> response) {
                    DeleteResponse deleteResponse = response.body();
                    userSignout();
                    Log.e(TAG, "onResponse: " + deleteResponse.getMessage());
                    if (deleteResponse.getMessage().equalsIgnoreCase("The account has been deleted successfully")) {
                        String selectedLang = Constants.pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE);
                        String selectedLangCode = Constants.pref.getString(Constants.PREF_LANGUAGE_CODE, Constants.LANGUAGE_CODE);
                        Log.i(TAG, "onClick: " + selectedLang);
                        Log.i(TAG, "onClick: " + selectedLangCode);
                        Constants.editor.clear();
                        Constants.editor.putString(Constants.PREF_LANGUAGE, selectedLang);
                        Constants.editor.putString(Constants.PREF_LANGUAGE_CODE, selectedLangCode);
                        Constants.editor.apply();
                        GetSet.reset();
                        FragmentMainActivity.homeItemList.clear();
                        if (FragmentMainActivity.itemAdapter != null) {
                            FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                        }
                        Log.e(TAG, "onResponse: " + deleteResponse.getMessage());
                        deleteaccount = true;
                        finish();
                        Intent p = new Intent(context, FragmentMainActivity.class);
                        p.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(p);
                    }else if (response.body().getStatus().equalsIgnoreCase("false")) {
                        JoysaleApplication.openDeleteDialog(response.body().getMessage(), getString(R.string.ok));
                    }
                }


                @Override
                public void onFailure(Call<DeleteResponse> call, Throwable t) {

                }
            });
        }
    }

    private void openDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfile.this, com.google.android.material.R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        builder.setMessage(getString(R.string.really_deleteaccount));
        builder.setPositiveButton(getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                deleteAccount();
            }
        });
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        Typeface typeface = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = getResources().getFont(R.font.font_regular);
        } else {
            typeface = ResourcesCompat.getFont(this, R.font.font_regular);
        }
        TextView textView = dialog.findViewById(android.R.id.message);
        textView.setTypeface(typeface);

        Button btn1 = dialog.findViewById(android.R.id.button1);
        btn1.setTypeface(typeface);

        Button btn2 = dialog.findViewById(android.R.id.button2);
        btn2.setTypeface(typeface);

    }
    public void initLauncher() {
        locations = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);

                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) {
                        if (ContextCompat.checkSelfPermission(EditProfile.this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(EditProfile.this,ACCESS_FINE_LOCATION )!= PackageManager.PERMISSION_GRANTED) {
                            granted = false;
                        } else {
                            granted = true;
                        }
                    }
                    Log.d(TAG, "onActivityResultk: "+granted);
                }

                if (granted) {

                    /*}*/

                } else {
                    boolean neverAsked = false;
                    if (ContextCompat.checkSelfPermission(EditProfile.this,ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(EditProfile.this,ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    }
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(EditProfile.this, x.getKey())) {
                                locations.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                if (ContextCompat.checkSelfPermission(EditProfile.this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(EditProfile.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    PermissionsUtils.openPermissionDialog(EditProfile.this, new OkayCancelCallback() {
                                        @Override
                                        public void onOkayClicked(Object o) {
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onCancelClicked(Object o) {

                                        }
                                    }, EditProfile.this.getString(R.string.ask_for_locations));
                                }
                                break;
                            }
                        }
                    }
                }

            }
        });

        galleryResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }

                if (granted) {
                   /* Intent in = new Intent(EditProfile.this, AlbumSelectActivity.class);
                    in.putExtra("from","profile");
                    in.putExtra("fromEditProfile", true);
                    startActivityForResult(in, REQUEST_CODE);*/
                    pickImage();
                } else {
                    boolean neverAsked = false;
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(EditProfile.this, x.getKey())) {
                                galleryResultLauncher.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(EditProfile.this, new OkayCancelCallback() {
                                    @Override
                                    public void onOkayClicked(Object o) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelClicked(Object o) {

                                    }
                                }, EditProfile.this.getString(R.string.storage_error));
                            }
                            break;
                        }
                    }
                }

            }
        });

        cameraResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }

                if (granted) {
                   /* Intent in = new Intent(EditProfile.this, AlbumSelectActivity.class);
                    in.putExtra("from","profile");
                    in.putExtra("fromEditProfile", true);
                    startActivityForResult(in, REQUEST_CODE);*/

                } else {
                    boolean neverAsked = false;
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(EditProfile.this, x.getKey())) {
                                cameraResultLauncher.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(EditProfile.this, new OkayCancelCallback() {
                                    @Override
                                    public void onOkayClicked(Object o) {
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelClicked(Object o) {

                                    }
                                }, EditProfile.this.getString(R.string.camera_error));
                            }
                            break;
                        }
                    }
                }

            }
        });


    }

    private void pickImage() {

        View contentView = getLayoutInflater().inflate(R.layout.bottom_sheet_image_pick_options, findViewById(R.id.parentLay), false);
        BottomSheetDialog pickerOptionsSheet = new BottomSheetDialog(this, R.style.SimpleBottomDialog);
        pickerOptionsSheet.setCanceledOnTouchOutside(true);
        pickerOptionsSheet.setContentView(contentView);
        //    pickerOptionsSheet.setDismissWithAnimation(true);

        View layoutCamera = contentView.findViewById(R.id.container_camera_option);
        View layoutGallery = contentView.findViewById(R.id.container_gallery_option);

        layoutCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this,CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                cameraResultLauncher.launch(new String[]{CAMERA});
            } else {
                // Permission already granted, proceed with camera intent
//                startCameraIntent();
                Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                if (captureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    currentPhotoFile = null;
                    try {
                        currentPhotoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        //     Timber.e(ex);
                    }
                    // Continue only if the File was successfully created
                    if (currentPhotoFile != null) {
                        mCurrentPhotoUri = FileProvider.getUriForFile(this,
                                getString(R.string.file_provider_authority),
                                currentPhotoFile);
                        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoUri);
                        startActivityForResult(captureIntent, REQUEST_CODE_IMAGE_INTENT);
                    }
                }
                pickerOptionsSheet.dismiss();
            }


        });
        layoutGallery.setOnClickListener(v -> {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE){
                Log.d(TAG, "pickImage:11111111111111 ");
                /*ArrayList<String> testUris = new ArrayList<>();
                testUris.add("content://Phone/Download/4wy10xithu681.jpg");*/
//                openGalleryFragment(selectedImageUris);

                Intent in = new Intent(EditProfile.this, AlbumSelectActivity.class);
                in.putExtra("from","profile");
                in.putExtra("fromEditProfile", true);
                startActivityForResult(in, REQUEST_CODE);

                pickerOptionsSheet.dismiss();
            } else{
                Log.d(TAG, "pickImage: 222222222222");
                Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Intent pickIntent = new Intent(Intent.ACTION_PICK, collection);
                pickIntent.setType("image/jpeg");

                Intent chooserIntent = Intent.createChooser(pickIntent, "Select a picture");

                if (chooserIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(chooserIntent, REQUEST_CODE_IMAGE_INTENT);
                }
                pickerOptionsSheet.dismiss();
            }



            pickerOptionsSheet.dismiss();
        });

        pickerOptionsSheet.show();
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = imageStorage.getExtCacheDir();
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoFile = image;
        return image;
    }
}