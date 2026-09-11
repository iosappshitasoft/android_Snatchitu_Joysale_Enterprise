package com.ni.SnatchItUp;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ni.helper.LocaleManager;
import com.ni.helper.NetworkReceiver;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.Constants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by hitasoft.
 * <p>
 * This class is for User Sign Up.
 */

public class RegisterActivity extends BaseActivity implements OnClickListener {

    private static String TAG = RegisterActivity.class.getSimpleName();
    /**
     * Declare Layout Elements
     **/
    EditText email, password, userName, fullName, confirmpwd;
    TextView register, login, title, phonenumber, phonenumberText;
    ImageView backbtn;
    RelativeLayout main;
    private TextureView textureView;

    /**
     * Declare Variables
     **/
    String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    ApiInterface apiInterface;

    InputFilter filterWithoutSpace = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isLetterOrDigit(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };

    InputFilter filterWithSpace = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        }
    };
    String cityName = "", stateName = "", countryName = "";
    private ActivityResultLauncher<Intent> otpResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        userName = (EditText) findViewById(R.id.userName);
        fullName = (EditText) findViewById(R.id.fullName);
        login = (TextView) findViewById(R.id.login);
        register = (TextView) findViewById(R.id.register);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        main = (RelativeLayout) findViewById(R.id.main);
        confirmpwd = (EditText) findViewById(R.id.confirmpwd);
        //Addons PhoneOTP
        phonenumber = (TextView) findViewById(R.id.phoneNum);
        phonenumberText = (TextView) findViewById(R.id.phonenumberText);
        textureView = findViewById(R.id.textureView);

        JoysaleApplication.setupUI(RegisterActivity.this, main);

        if (getIntent().hasExtra(Constants.TAG_CITY_NAME)) {
            cityName = getIntent().getStringExtra(Constants.TAG_CITY_NAME);
            stateName = getIntent().getStringExtra(Constants.TAG_STATE_NAME);
            countryName = getIntent().getStringExtra(Constants.TAG_COUNTRY_NAME);
        }

        if (LocaleManager.isRTL(this)) {
            backbtn.setScaleX(-1);
        } else {
            backbtn.setScaleX(1);
        }

        backbtn.setOnClickListener(this);
        login.setOnClickListener(this);
        register.setOnClickListener(this);

        //Addons PhoneOTP
        if (Constants.ADDON_MOBILE_OTP) {
            phonenumber.setVisibility(View.VISIBLE);
            phonenumberText.setVisibility(View.VISIBLE);
            otpResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        phonenumber.setText(user.getPhoneNumber());
                    } else {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                }
            });
        } else {
            phonenumber.setVisibility(View.GONE);
            phonenumberText.setVisibility(View.GONE);
        }
             phonenumber.setOnClickListener(this);

        email.addTextChangedListener(new MyTextWatcher(email));
        password.addTextChangedListener(new MyTextWatcher(password));
        userName.addTextChangedListener(new MyTextWatcher(userName));
        fullName.addTextChangedListener(new MyTextWatcher(fullName));

        fullName.setFilters(new InputFilter[]{filterWithSpace, new InputFilter.LengthFilter(30)});
        userName.setFilters(new InputFilter[]{JoysaleApplication.USERNAME_FILTER, new InputFilter.LengthFilter(30)});
//        userName.setFilters(new InputFilter[]{filterWithoutSpace, new InputFilter.LengthFilter(30)});
    }

    public void dialog(Context ctx, String title, final String content) {
        final Dialog dialog = new Dialog(ctx, R.style.AlertDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_dialog);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        dialog.getWindow().setLayout(displayMetrics.widthPixels * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);

        TextView alertTitle = (TextView) dialog.findViewById(R.id.alert_title);
        TextView alertMsg = (TextView) dialog.findViewById(R.id.alert_msg);
        TextView alertOk = (TextView) dialog.findViewById(R.id.alert_button);

        alertTitle.setText(title);
        alertMsg.setText(content);

        alertOk.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (content.equals(getString(R.string.directsignup_true_response))) {
                    RegisterActivity.this.finish();
                    Intent in = new Intent(RegisterActivity.this, LoginActivity.class);
                    in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(in);
                } else {
                    RegisterActivity.this.finish();
                }
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                RegisterActivity.this.finish();
            }
        });
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
//            VideoAddon.initVideo(textureView, this);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        JoysaleApplication.networkError(RegisterActivity.this, isConnected);
    }

    private void registerUser(final ProgressDialog dialog) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_USERNAME, userName.getText().toString().trim());
            map.put(Constants.TAG_FULL_NAME, fullName.getText().toString().trim());
            map.put(Constants.TAG_EMAIL, email.getText().toString().trim());
            map.put(Constants.TAG_PASSWORD, password.getText().toString().trim());
            map.put(Constants.TAG_CITY_NAME, cityName != null ? cityName : "");
            map.put(Constants.TAG_STATE_NAME, stateName != null ? stateName : "");
            map.put(Constants.TAG_COUNTRY_NAME, countryName != null ? countryName : "");
//Addons PhoneOTP
            if (Constants.ADDON_MOBILE_OTP && !TextUtils.isEmpty(phonenumber.getText())) {
                String phoneNumber = phonenumber.getText().toString().trim();
                String phone = phoneNumber.replace("+", "");
                map.put(Constants.TAG_OTP_USER, phone);
            }
            Call<HashMap<String, String>> call = apiInterface.signUp(map);
            Log.d(TAG, "registerUser: " + map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, retrofit2.Response<HashMap<String, String>> response) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }

                    if (response.isSuccessful()) {
                        try {
                            HashMap<String, String> result = response.body();
                            if (result.get(Constants.TAG_STATUS).equalsIgnoreCase("true")) {
                                if (result.get(Constants.TAG_MESSAGE).equals("account has been created, Amazing products are waiting for you, kindly login.")) {
                                    dialog(RegisterActivity.this, getString(R.string.success), getString(R.string.directsignup_true_response));
                                } else {
                                    dialog(RegisterActivity.this, getString(R.string.success), getString(R.string.signup_true_response));
                                }

                            } else {
                                // email.setText("");
                                password.setText("");

                                if (result.get(Constants.TAG_MESSAGE).equalsIgnoreCase("Sorry, unable to create user, please try again later")) {
                                    JoysaleApplication.dialog(RegisterActivity.this, getString(R.string.alert), getString(R.string.unable_to_create_user));
                                } else if (result.get(Constants.TAG_MESSAGE).equalsIgnoreCase("Email already exists")) {
                                    JoysaleApplication.dialog(RegisterActivity.this, getString(R.string.alert), getString(R.string.email_already_exists));
                                } else if (result.get(Constants.TAG_MESSAGE).equalsIgnoreCase("Mobile number already exists")) {
                                    JoysaleApplication.dialog(RegisterActivity.this, getString(R.string.alert), getString(R.string.number_already_exists_various));
                                } else if (result.get(Constants.TAG_MESSAGE).equalsIgnoreCase("Username already exists")) {
                                    JoysaleApplication.dialog(RegisterActivity.this, getString(R.string.alert), getString(R.string.username_already_exists));
                                } else {
                                    JoysaleApplication.dialog(RegisterActivity.this, getString(R.string.alert), result.get(Constants.TAG_MESSAGE));
                                }
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            JoysaleApplication.dialog(RegisterActivity.this, getString(R.string.error), e.getMessage());
                        } catch (Exception e) {
                            e.printStackTrace();
                            JoysaleApplication.dialog(RegisterActivity.this, getString(R.string.error), e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    public static class MyTextWatcher implements TextWatcher {

        private EditText view;

        MyTextWatcher(EditText view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (view != null) {
                view.setError(null);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    /**
     * Function for OnClick Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                if (!JoysaleApplication.isNetworkAvailable(this)) {
                    JoysaleApplication.dialog(RegisterActivity.this, getString(R.string.error), getString(R.string.network_error));
                } else if (fullName.getText().toString().trim().length() == 0) {
                    fullName.setError(getString(R.string.please_fill));
                } else if (userName.getText().toString().trim().length() == 0) {
                    userName.setError(getString(R.string.please_fill));
                    //Addons PhoneOTP
                } else if (Constants.ADDON_MOBILE_OTP && TextUtils.isEmpty(phonenumber.getText())) {
                    phonenumber.setError(getString(R.string.mobile_number_verifiy));
                } else if ((!email.getText().toString().matches(emailPattern))
                            || (email.getText().toString().trim().length() == 0)) {
                        email.setError(getString(R.string.please_verify_mail));
                    } else if (password.getText().toString().trim().length() < 6) {
                        password.setError(getString(R.string.passwordshould));
                    } else if (!(password.getText().toString().trim())
                            .equals(confirmpwd.getText().toString().trim())) {
                        password.setError(getString(R.string.passwordmismatched));
                    } else {
                        ProgressDialog dialog = new ProgressDialog(RegisterActivity.this, R.style.AppCompatAlertDialogStyle);
                        dialog.setMessage(getString(R.string.pleasewait));
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                            Drawable drawable = new ProgressBar(this).getIndeterminateDrawable().mutate();
                            drawable.setColorFilter(ContextCompat.getColor(this, R.color.progressColor),
                                    PorterDuff.Mode.SRC_IN);
                            dialog.setIndeterminateDrawable(drawable);
                        }
                        dialog.show();

                        //To call register Api
                        registerUser(dialog);

                    }
                    break;
                //Addons PhoneOTP
                    case R.id.phoneNum:
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        verifyMobileNumber();
                        break;
                    case R.id.backbtn:
                        finish();
                        break;
                    case R.id.login:
                        Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(i);
                        break;
                }
        }

    //Addons PhoneOTP
        private void verifyMobileNumber () {
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.PhoneBuilder().build());
            // Create and launch sign-in intent
            Intent intent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setTheme(R.style.OTPTheme)
                    .build();
            otpResultLauncher.launch(intent);
        }

    }
