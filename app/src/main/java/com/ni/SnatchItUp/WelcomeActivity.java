package com.ni.SnatchItUp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ni.addons.VideoAddon;
import com.ni.helper.NetworkReceiver;
import com.ni.helper.SharedPrefManager;
import com.ni.helper.location.LocationHelper;
import com.ni.model.LoginResponse;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hitasoft on 24/5/16.
 * <p>
 * This class is for Welcome Screen after displays Splash Screen.It contains Social Login.
 */

public class WelcomeActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = WelcomeActivity.class.getSimpleName();
    /**
     * Declare Variables
     **/
    private static final int RC_SIGN_IN = 9001;
    public static boolean fromSignout = false;
    public static int APP_REQUEST_CODE = 100;
    /**
     * Declare Layout Elements
     **/
    TextView login, signup, skip, fbTxt, twtTxt, gplusTxt;
    ImageView fbBtn, twtBtn, gplusBtn;
    ProgressDialog dialog;
    boolean mSignInClicked;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    /**
     * Declare Social Login Elements
     **/
//    CallbackManager callbackManager;
    ApiInterface apiInterface;
    DisplayMetrics displayMetrics;
    String cityName = "", stateName = "", countryName = "";
    LocationManager locationManager;
    //Addons PhoneOTP
    LinearLayout otpLay;
    String phonenum, countrynum = "";
    private PlacesClient placesClient;
    private GoogleSignInClient mGoogleSignInClient;
    private FusedLocationProviderClient fusedLocationClient;
    private List<Address> addresses = new ArrayList<>();
    private String deviceName, deviceModel;
    private ActivityResultLauncher<Intent> otpResultLauncher;
    private TextureView textureView;
    private View overLay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcomelay);
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Constants.pref = getApplicationContext().getSharedPreferences("JoysalePref", MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        login = (TextView) findViewById(R.id.login);
        signup = (TextView) findViewById(R.id.signup);
        skip = (TextView) findViewById(R.id.skip);
        fbTxt = (TextView) findViewById(R.id.fbTxt);
        twtTxt = (TextView) findViewById(R.id.twtTxt);
        gplusTxt = (TextView) findViewById(R.id.gpTxt);
        fbBtn = (ImageView) findViewById(R.id.fbBtn);
        twtBtn = (ImageView) findViewById(R.id.twtBtn);
        gplusBtn = (ImageView) findViewById(R.id.gpBtn);
        textureView = findViewById(R.id.textureView);
        overLay = findViewById(R.id.overLay);

        /**
         * Funtions for login using Mobile OTP
         **/

        //Addons PhoneOTP
        otpLay = (LinearLayout) findViewById(R.id.mOtpLay);

        deviceName = JoysaleApplication.getDeviceName();
        deviceModel = JoysaleApplication.getDeviceModel();

        login.setOnClickListener(this);
        signup.setOnClickListener(this);
        skip.setOnClickListener(this);
        fbTxt.setOnClickListener(this);
        twtTxt.setOnClickListener(this);
        gplusTxt.setOnClickListener(this);
        fbBtn.setOnClickListener(this);
        twtBtn.setOnClickListener(this);
        gplusBtn.setOnClickListener(this);
         //Addons PhoneOTP
        if (Constants.ADDON_MOBILE_OTP) {
            otpLay.setVisibility(View.VISIBLE);
            otpResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        countrynum = user.getPhoneNumber();
                        getPhoneLogin();
                    } else {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                }
            });
        } else {
            otpLay.setVisibility(View.GONE);
        }
        otpLay.setOnClickListener(this);

        //To init Facebook login
//        loginToFacebook();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //To initialize dialog
        dialog = new ProgressDialog(WelcomeActivity.this, R.style.AppCompatAlertDialogStyle);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                    PorterDuff.Mode.SRC_IN);
            dialog.setIndeterminateDrawable(drawable);
        }

    }

    /**
     * Function for login using facebook
     */


    public void loginToFacebook() {/*
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject profile,
                                            GraphResponse response) {
                                        final HashMap<String, String> fbdata = new HashMap<String, String>();
                                        Log.v(TAG, "Fbobject" + profile);
                                        // Application code
                                        try {
                                            if (profile.has(Constants.TAG_EMAIL)) {
                                                fbdata.put(Constants.TAG_TYPE, "facebook");
                                                fbdata.put(Constants.TAG_EMAIL, profile.getString(Constants.TAG_EMAIL));
                                                fbdata.put(Constants.TAG_ID, profile.getString(Constants.TAG_ID));
                                                fbdata.put(Constants.TAG_FIRST_NAME, profile.getString(Constants.TAG_FIRST_NAME));
                                                fbdata.put(Constants.TAG_LAST_NAME, profile.getString(Constants.TAG_LAST_NAME));
                                                fbdata.put(Constants.TAG_IMAGE_URL, "http://graph.facebook.com/" + profile.getString("id") + "/picture?type=large");
                                                Log.v(TAG, "fbdata=" + fbdata);
                                                GetSet.setImageUrl("http://graph.facebook.com/" + profile.getString("id") + "/picture?type=large");
                                                InputStream imageStream = null;
*//*
                                                try {
                                                    imageStream = new URL("http://graph.facebook.com/" + profile.getString("id") + "/picture?type=large").openStream();
                                                    uploadImage(AppUtils.getBytes(imageStream));
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
*//*
                                                WelcomeActivity.this.runOnUiThread(new Runnable() {

                                                    @SuppressWarnings("unchecked")
                                                    @Override
                                                    public void run() {
                                                        if (dialog != null && dialog.isShowing()) {
                                                            dialog.dismiss();
                                                        }
                                                        try {
                                                            dialog.show();
                                                        } catch (WindowManager.BadTokenException e) {
                                                            e.printStackTrace();
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        sentDetails(fbdata);
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(WelcomeActivity.this, "Please check your Facebook permissions", Toast.LENGTH_SHORT).show();
                                            }

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
                        Toast.makeText(WelcomeActivity.this, "Facebook - Cancelled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(WelcomeActivity.this, "Facebook - " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        if (exception instanceof FacebookAuthorizationException) {
                            if (com.facebook.AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();
                            }
                        }
                    }
                });
    */}

    private void uploadImage(byte[] imageBytes) {

        RequestBody requestFile = RequestBody.create(imageBytes, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("user", "image.jpg", requestFile);
        RequestBody userid = RequestBody.create(GetSet.getUserId(), MediaType.parse("multipart/form-data"));
        Call<Map<String, String>> call3 = apiInterface.uploadProfileImage(body, userid);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                //   Map<String, String> data = response.body();
                Log.d(TAG, "onResponse: " + response.toString());

            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {

                call.cancel();
            }
        });
    }


    /**
     * Function for send a social login user details to server
     **/

    private void sentDetails(final HashMap<String, String> datas) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_TYPE, safeTrim(datas.get(Constants.TAG_TYPE)));
            map.put(Constants.TAG_ID, safeTrim(datas.get(Constants.TAG_ID)));
            map.put(Constants.TAG_FIRST_NAME, safeTrim(datas.get(Constants.TAG_FIRST_NAME)));
            map.put(Constants.TAG_LAST_NAME, safeTrim(datas.get(Constants.TAG_LAST_NAME)));
            map.put(Constants.TAG_EMAIL, safeTrim(datas.get(Constants.TAG_EMAIL)));
            map.put(Constants.TAG_IMAGE_URL, safeTrim(datas.get(Constants.TAG_IMAGE_URL)));
            map.put(Constants.TAG_CITY_NAME, safeTrim(cityName));
            map.put(Constants.TAG_STATE_NAME, safeTrim(stateName));
            map.put(Constants.TAG_COUNTRY_NAME, safeTrim(countryName));
            Log.d(TAG, "sentDetails: " + map);
            Call<LoginResponse> call = apiInterface.socialLogIn(map);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, retrofit2.Response<LoginResponse> response) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse login = response.body();
                            String status = login.getStatus();
                            String msg = login.getMessage() != null ? login.getMessage() : getString(R.string.somethingwrong);

                            if ("true".equalsIgnoreCase(status)) {
                                GetSet.setLogged(true);
                                GetSet.setEmail(login.getEmail());
                                GetSet.setPassword("");
                                GetSet.setUserId(login.getUserId());
                                GetSet.setUserName(login.getUserName());
                                GetSet.setFullName(login.getFullName());
                                GetSet.setImageUrl(login.getPhoto());
                                GetSet.setRating(login.getRating());
                                GetSet.setRatingUserCount(login.getRatingUserCount());
                                GetSet.setAccessToken(login.getAccessToken());

                                SharedPrefManager.putStringIntoStore(Constants.TAG_ACCESS_TOKENS, login.getAccessToken());

                                Constants.editor.putBoolean(Constants.PREF_ISLOGGED, true);
                                Constants.editor.putString(Constants.TAG_USER_ID, GetSet.getUserId());
                                Constants.editor.putString(Constants.TAG_USERNAME, GetSet.getUserName());
                                Constants.editor.putString(Constants.TAG_EMAIL, GetSet.getEmail());
                                Constants.editor.putString(Constants.TAG_PASSWORD, "");
                                Constants.editor.putString(Constants.TAG_PHOTO, GetSet.getImageUrl());
                                Constants.editor.putString(Constants.TAG_FULL_NAME, GetSet.getFullName());
                                Constants.editor.putString(Constants.TAG_RATING, GetSet.getRating());
                                Constants.editor.putString(Constants.TAG_RATING_USER_COUNT, GetSet.getRatingUserCount());
                                Constants.editor.putString(Constants.PREF_LANGUAGE, Constants.pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE));
                                Constants.editor.putString(Constants.TAG_ACCESS_TOKENS, GetSet.getAccessToken());
                                Constants.editor.commit();
                                addDeviceId();
                                finish();
                                Intent i = new Intent(WelcomeActivity.this, FragmentMainActivity.class);
                                Constants.REFRESHCHECK = true;
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(i);
                            } else if ("false".equalsIgnoreCase(status)) {
                                if ("Account not found".equalsIgnoreCase(msg)) {
                                    getEmailForTwitter(datas);
                                } else if ("Email Already Exist".equalsIgnoreCase(msg)) {
                                    JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.alert), getString(R.string.email_already_exists));
                                } else if ("Your account has been blocked by admin".equalsIgnoreCase(msg)) {
                                    JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.alert), getString(R.string.your_account_blocked_by_admin));
                                } else {
                                    JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.alert), msg);
                                }
                            } else {
                                JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.alert), msg);
                            }
                        } else {
                            Toast.makeText(WelcomeActivity.this,
                                    "Server error (" + response.code() + "). Please try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.error),
                                e.getMessage() != null ? e.getMessage() : getString(R.string.somethingwrong));
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    call.cancel();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Toast.makeText(WelcomeActivity.this,
                            "Network error: " + (t.getMessage() != null ? t.getMessage() : getString(R.string.somethingwrong)),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Function for register push notification in FCM Server
     **/

    private void addDeviceId() {
        if (NetworkReceiver.isConnected()) {
            final String token = SharedPrefManager.getInstance(this).getDeviceToken();
            Map<String, String> map = new HashMap<String, String>();
            String[] languages = getResources().getStringArray(R.array.languages);
            String[] langCode = getResources().getStringArray(R.array.languageCode);
            String selectedLang = Constants.pref.getString(Constants.PREF_LANGUAGE, Constants.LANGUAGE);
            int index = Arrays.asList(languages).indexOf(selectedLang);
            final String languageCode = Arrays.asList(langCode).get(index);

            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_DEVICE_ID, Constants.ANDROID_ID);
            map.put(Constants.TAG_FCM_USERID, GetSet.getUserId());
            map.put(Constants.TAG_DEVICE_TYPE, "1");
            map.put(Constants.TAG_DEVICE_MODE, "1");
            map.put(Constants.TAG_LANG_TYPE, languageCode);
            map.put(Constants.TAG_DEVICE_TOKEN, token);
            map.put(Constants.TAG_DEVICE_NAME, deviceName != null ? deviceName : "");
            map.put(Constants.TAG_DEVICE_MODEL, deviceModel != null ? deviceModel : "");
            map.put(Constants.TAG_DEVICE_OS, "" + Build.VERSION.SDK_INT);
            map.put(Constants.TAG_VOIP_TOKEN, "");
            Call<ResponseBody> call = apiInterface.addDeviceId(map);
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

    /**
     * Funtions for login using Twitter
     **/

    private void getEmailForTwitter(final HashMap<String, String> twitterData) {
        final Dialog dialog = new Dialog(this, R.style.AlertDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.forget_password);
        dialog.getWindow().setLayout(displayMetrics.widthPixels * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView title = (TextView) dialog.findViewById(R.id.alert_title);
        final EditText msg = (EditText) dialog.findViewById(R.id.alert_msg);
        TextView ok = (TextView) dialog.findViewById(R.id.alert_button);

        title.setText(getString(R.string.twitter));
        ok.setText(getString(R.string.ok));

        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if ((!msg.getText().toString().matches(emailPattern))
                        || (msg.getText().toString().trim().length() == 0)) {
                    msg.setError(getString(R.string.please_verify_mail));
                } else {
                    twitterData.put(Constants.TAG_EMAIL, msg.getText().toString().trim());
                    try {
                        dialog.show();
                    } catch (WindowManager.BadTokenException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    sentDetails(twitterData);
                }
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }

    }

    /**
     * Funtion for login using Google Sign In
     **/

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        Log.v(TAG, "handleSignInResult:" + completedTask.isSuccessful());
        if (completedTask.isSuccessful()) {
            // Signed in successfully, show authenticated UI.
            try {
                GoogleSignInAccount acct = completedTask.getResult(ApiException.class);

                String personPhoto = "";

                if (acct.getPhotoUrl() == null) {
                    personPhoto = "";

                } else {
                    personPhoto = acct.getPhotoUrl().toString();
                }

                HashMap<String, String> gplusData = new HashMap<String, String>();

                gplusData.put(Constants.TAG_TYPE, "google");
                gplusData.put(Constants.TAG_EMAIL, safeTrim(acct.getEmail()));
                gplusData.put(Constants.TAG_ID, safeTrim(acct.getId()));
                gplusData.put(Constants.TAG_FIRST_NAME, safeTrim(acct.getDisplayName()));
                gplusData.put(Constants.TAG_LAST_NAME, "");
                gplusData.put(Constants.TAG_IMAGE_URL, safeTrim(personPhoto));

                try {
                    dialog.show();
                } catch (WindowManager.BadTokenException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                sentDetails(gplusData);

                Log.v(TAG, "personName" + acct.getDisplayName());
                Log.v(TAG, "personPhoto" + personPhoto);
                Log.v(TAG, "email" + acct.getEmail());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onactivity");
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        /*if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }*/
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Addon Login Video
        if (Constants.ADDON_LOGIN_VIDEO) {
            overLay.setVisibility(View.VISIBLE);
            VideoAddon.initVideo(textureView, this);
        }
        if (checkGPS()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    new GetCurrentLocationTask().execute();
                }
            } else {
                new GetCurrentLocationTask().execute();
            }
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(WelcomeActivity.this, isConnected);
    }

    private boolean checkGPS() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (fromSignout) {
            fromSignout = false;
            finish();
            Intent y = new Intent(WelcomeActivity.this, FragmentMainActivity.class);
            y.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(y);
        } else {
            finish();
        }
    }

    /**
     * Funtion for OnClick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                Intent i = new Intent(WelcomeActivity.this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                break;
            case R.id.signup:
                Intent e = new Intent(WelcomeActivity.this, RegisterActivity.class);
                e.putExtra(Constants.TAG_CITY_NAME, cityName);
                e.putExtra(Constants.TAG_STATE_NAME, stateName);
                e.putExtra(Constants.TAG_COUNTRY_NAME, countryName);
                e.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(e);
                break;
            case R.id.skip:
                if (fromSignout) {
                    fromSignout = false;
                    finish();
                    Intent y = new Intent(WelcomeActivity.this, FragmentMainActivity.class);
                    startActivity(y);
                } else {
                    finish();
                }
                break;
            case R.id.fbBtn:
            case R.id.fbTxt:
//                LoginManager.getInstance().logOut();
//                LoginManager.getInstance().logInWithReadPermissions(WelcomeActivity.this, Arrays.asList("public_profile", "email"));
                break;
            case R.id.twtBtn:
                break;
            case R.id.mOtpLay:
                verifyMobileNumber();
                break;
            case R.id.twtTxt:
                break;
            case R.id.gpBtn:
            case R.id.gpTxt:
                mSignInClicked = true;
                googleLogout();
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
        }
    }

    // To logout from gplus account
    private void googleLogout() {
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...
                        }
                    });
        }
    }

    /**
     * Funtions for login using Mobile OTP
     **/
    //Addons PhoneOTP
    private void verifyMobileNumber() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build());
        // Create and launch sign-in intent
        Intent intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .setTheme(R.style.OTPTheme)
                .build();
        otpResultLauncher.launch(intent);
    }

    //Addons PhoneOTP
    private void getPhoneLogin() {
        if (NetworkReceiver.isConnected()) {
            String phoneNumber = countrynum.trim();
            String phone = phoneNumber.replace("+", "");
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_OTP_USER, phone);

            Call<LoginResponse> call = apiInterface.phoneLogin(map);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        LoginResponse profile = response.body();
                        String status = profile.getStatus();
                        String msg = profile.getMessage() != null ? profile.getMessage() : getString(R.string.somethingwrong);

                        if ("true".equalsIgnoreCase(status)) {
                            GetSet.setLogged(true);
                            GetSet.setUserId(profile.getUserId());
                            GetSet.setUserName(profile.getUserName());
                            GetSet.setFullName(profile.getFullName());
                            GetSet.setImageUrl(profile.getPhoto());
                            GetSet.setRating(profile.getRating());
                            GetSet.setRatingUserCount(profile.getRatingUserCount());
                            GetSet.setAccessToken(profile.getAccessToken());

                            Constants.editor.putBoolean(Constants.PREF_ISLOGGED, true);
                            Constants.editor.putString(Constants.TAG_USER_ID, GetSet.getUserId());
                            Constants.editor.putString(Constants.TAG_USERNAME, GetSet.getUserName());
                            Constants.editor.putString(Constants.TAG_PHOTO, GetSet.getImageUrl());
                            Constants.editor.putString(Constants.TAG_FULL_NAME, GetSet.getFullName());
                            Constants.editor.putString(Constants.TAG_RATING, GetSet.getRating());
                            Constants.editor.putString(Constants.TAG_RATING_USER_COUNT, GetSet.getRatingUserCount());
                            Constants.editor.putString(Constants.TAG_ACCESS_TOKENS, GetSet.getAccessToken());
                            Constants.editor.commit();

                            addDeviceId(profile.getUserId());
                            finish();
                            Intent i = new Intent(WelcomeActivity.this, FragmentMainActivity.class);
                            startActivity(i);

                            Toast.makeText(WelcomeActivity.this, "Login Successfull", Toast.LENGTH_SHORT).show();
                        } else if ("false".equalsIgnoreCase(status)) {
                            if ("Please activate your account by the email sent to you".equalsIgnoreCase(msg)) {
                                JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.alert), getString(R.string.please_activate_your_account));
                            } else if ("User not registered yet".equalsIgnoreCase(msg)) {
                                dialogUserNotRegister(WelcomeActivity.this, getString(R.string.alert), getString(R.string.user_no_not_registered_yet));
                            } else {
                                JoysaleApplication.dialog(WelcomeActivity.this, getString(R.string.alert), msg);
                            }
                        } else {
                            Toast.makeText(WelcomeActivity.this, getString(R.string.register_mobile), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(WelcomeActivity.this,
                                "Server error (" + response.code() + "). Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(WelcomeActivity.this,
                            "Network error: " + (t.getMessage() != null ? t.getMessage() : getString(R.string.somethingwrong)),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void dialogUserNotRegister(Context ctx, String title, String content) {
        final Dialog dialog = new Dialog(ctx, R.style.AlertDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog.getWindow().setLayout(displayMetrics.widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);

        alertTitle.setText(title);
        alertMsg.setText(content);

        alertOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent e = new Intent(WelcomeActivity.this, RegisterActivity.class);
                e.putExtra(Constants.TAG_CITY_NAME, cityName);
                e.putExtra(Constants.TAG_STATE_NAME, stateName);
                e.putExtra(Constants.TAG_COUNTRY_NAME, countryName);
                e.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(e);

            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }

    }

    private void addDeviceId(final String userId) {
        if (NetworkReceiver.isConnected()) {
            final String token = SharedPrefManager.getInstance(this).getDeviceToken();
            Map<String, String> map = new HashMap<String, String>();
            String[] languages = getResources().getStringArray(R.array.languages);
            String[] langCode = getResources().getStringArray(R.array.languageCode);
            String selectedLang = Constants.pref.getString("language", Constants.LANGUAGE);
            int index = Arrays.asList(languages).indexOf(selectedLang);
            final String languageCode = Arrays.asList(langCode).get(index);
            Log.v(TAG, "languageCode=" + languageCode);

            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_DEVICE_ID, Constants.ANDROID_ID);
            map.put(Constants.TAG_FCM_USERID, userId);
            map.put(Constants.TAG_DEVICE_TYPE, "1");
            map.put(Constants.TAG_DEVICE_MODE, "1");
            map.put(Constants.TAG_LANG_TYPE, languageCode);
            map.put(Constants.TAG_DEVICE_TOKEN, token);
            map.put(Constants.TAG_DEVICE_NAME, deviceName != null ? deviceName : "");
            map.put(Constants.TAG_DEVICE_MODEL, deviceModel != null ? deviceModel : "");
            map.put(Constants.TAG_DEVICE_OS, "" + Build.VERSION.SDK_INT);
            map.put(Constants.TAG_VOIP_TOKEN, "");
            Call<ResponseBody> call = apiInterface.addDeviceId(map);
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

    @SuppressLint("StaticFieldLeak")
    private class GetCurrentLocationTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (ActivityCompat.checkSelfPermission(WelcomeActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(WelcomeActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // GPS location can be null if GPS is switched off
                            if (location != null) {
                                addresses = LocationHelper.getLocationFromLatLng(WelcomeActivity.this, "register", location.getLatitude(), location.getLongitude());
                                try {
                                    if (addresses != null && !addresses.isEmpty()) {
                                        cityName = addresses.get(0).getLocality();
                                        stateName = addresses.get(0).getAdminArea();
                                        countryName = addresses.get(0).getCountryName();
                                        Log.i(TAG, "onSuccess: " + cityName);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
            return null;
        }
    }


}
