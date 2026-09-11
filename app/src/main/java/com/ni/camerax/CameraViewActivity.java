package com.ni.camerax;


import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.os.Build.VERSION.SDK_INT;
import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;
import static com.ni.external.multipleimageselect.helpers.Constants.REQUEST_CODE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Insets;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowMetrics;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.ni.addons.InterstitialAdUtils;
import com.ni.external.multipleimageselect.activities.AlbumSelectActivity;
import com.ni.external.multipleimageselect.models.Photo;
import com.ni.helper.ImageStorage;
import com.ni.helper.PermissionsUtils;
import com.ni.helper.callback.OkayCancelCallback;
import com.ni.SnatchItUp.AddProductDetail;
import com.ni.SnatchItUp.BaseActivity;
import com.ni.SnatchItUp.BuildConfig;
import com.ni.SnatchItUp.JoysaleApplication;
import com.ni.SnatchItUp.R;
import com.ni.model.HomeItemResponse;
import com.ni.utils.Constants;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CameraViewActivity extends BaseActivity {

    private static final String TAG = CameraViewActivity.class.getSimpleName();
    public static final double RATIO_4_3_VALUE = 4.0 / 3.0;
    public static final double RATIO_16_9_VALUE = 16.0 / 9.0;
    private static final String FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final String PHOTO_EXTENSION = ".jpg";
    private static final String VIDEO_EXTENSION = ".mp4";
    private final long STATUS_DURATION = 30 * 1000;
    public int displayHeight;
    public int displayWidth;
    @SuppressLint("StaticFieldLeak")
    public ImagesAdapter imagesAdapter;
    public ArrayList<HomeItemResponse.Photo> temp = new ArrayList<>();
    public ArrayList<HomeItemResponse.Photo> selectedImages = new ArrayList<>();
    private ActivityResultLauncher<String[]> galleryResultLauncher;
    StorageManager storageManager;
    TextView btnGallery, next;
    int bottomNavHeight = 0, bottomMargin = 0;
    ImageStorage imageStorage;
    boolean isPermissionDialogNeeded = true;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler mainHandler = new Handler(Looper.myLooper());
    RecyclerView recyclerView;
    /**
     * Blocking camera operations are performed using this executor
     */
    String from = "";
    boolean flash = false, isSwitchCamera = false;
    NumberFormat numberFormat;
    private FrameLayout mainLay;
    private HomeItemResponse.Item itemMap = new HomeItemResponse().new Item();
    private String statusType = Constants.TAG_IMAGE;
    private ConstraintLayout bottomLay;
    private LinearLayout timerLay;
    private PreviewView cameraView;
    private ImageView btnFlash, btnSwitchCamera, backbtn;
    private ImageView btnImage, btnVideo, btnCapture, btnStatus;
    private Button permissionsButton;
    private TextView txtRecordTimer, txtTapHold;
    private CountDownTimer longClickTimer;
    private CountDownTimer videoTimer;
    private long clickTime;
    private boolean isVideoStarted = false, isCompleted = false, isVideo = true;
    private int flashMode = ImageCapture.FLASH_MODE_OFF;
    private SharedPreferences pref;
    private Context mContext;
    private ActivityResultLauncher<String[]> storagePermissionResult;
    private ExecutorService cameraExecutor = null;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    private CameraSelector cameraSelector;
    private int lensFacing;
    private Camera camera;
    public String location ="";

    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {
        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cameraview);
        //Addon Interstitial Ad
        InterstitialAdUtils.loadInterstitialAd(CameraViewActivity.this, new Bundle());
        numberFormat = NumberFormat.getInstance(new Locale("en", "US"));
        Window w = getWindow(); // in Activity's onCreate() for instance
        pref = getSharedPreferences("SavedPref", MODE_PRIVATE);
        mContext = this;
        getDisplayWidthHeight(mContext);
        cameraView = findViewById(R.id.camerapreview);
        mainLay = findViewById(R.id.mainLay);
        next = findViewById(R.id.next);
        btnSwitchCamera = findViewById(R.id.retakeBtn);
        btnFlash = findViewById(R.id.flashBtn);
        btnGallery = findViewById(R.id.galery);
        btnCapture = findViewById(R.id.snap);
        backbtn = findViewById(R.id.backbtn);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        imageStorage = new ImageStorage(this);

        bottomMargin = JoysaleApplication.dpToPx(this, 5);
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraProviderFuture = ProcessCameraProvider.getInstance(mContext);

        // Select lensFacing depending on the available cameras
        lensFacing = CameraSelector.LENS_FACING_BACK;
        from = getIntent().getExtras().getString(Constants.TAG_FROM);
        if (getIntent().hasExtra(Constants.TAG_LOCATION)) {
            location = getIntent().getStringExtra(Constants.TAG_LOCATION);
        }
        initLauncher();
        initPermissionResult();
        mainLay.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                bottomNavHeight = view.getPaddingBottom() + windowInsets.getSystemWindowInsetBottom() + bottomMargin;
                if (bottomLay != null) {
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
//                    params.bottomMargin = bottomNavHeight;
                    if (bottomLay != null) {
                        bottomLay.setPadding(0, 0, 0, bottomNavHeight + bottomMargin);
                        bottomLay.setLayoutParams(params);
                    }
                }
                return windowInsets.consumeSystemWindowInsets();
            }
        });


        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ArrayList<HomeItemResponse.Photo> tempImages = new ArrayList<>();
        if (getIntent().getExtras().containsKey(Constants.TAG_IMAGES)) {
            tempImages = (ArrayList<HomeItemResponse.Photo>) getIntent().getExtras().get(Constants.TAG_IMAGES);
        }
        if (getIntent().getExtras().get(Constants.TAG_DATA) != null) {
            itemMap = (HomeItemResponse.Item) getIntent().getExtras().get(Constants.TAG_DATA);
            if (itemMap != null && tempImages != null) {
                for (HomeItemResponse.Photo photo : tempImages) {
                    String imageurl = "";
                    if (photo.getItemUrlMainOriginal() != null) {
                        imageurl = photo.getItemUrlMainOriginal();
                    } else if (photo.getType() != null) {
                        imageurl = photo.getImage();
                    }
                    photo.setImage(imageurl);
                    if (!temp.contains(photo)) {
                        temp.add(photo);
                        selectedImages.add(photo);
                    }
                }
                if (imagesAdapter != null) {
                    imagesAdapter.notifyDataSetChanged();
                }
            }


        }


        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int totalCount = totalImageCount();
                if (temp.size() == 0 || totalCount < 0) {
                    Toast.makeText(CameraViewActivity.this, getString(R.string.please_add_image), Toast.LENGTH_SHORT).show();
                } else if (totalCount > Constants.IMAGE_COUNT) {
                    String number = numberFormat.format(Constants.IMAGE_COUNT);
                    Toast.makeText(CameraViewActivity.this, String.format(getString(R.string.error_msg_to_imgcount), number), Toast.LENGTH_SHORT).show();
                } else if (selectedImages.size() == 0 && totalCount == 0) {
                    Toast.makeText(CameraViewActivity.this, getString(R.string.please_select_images), Toast.LENGTH_SHORT).show();
                } else {
                    switch (from) {
                        case "edit": {
                            Intent i = new Intent();
                            i.putExtra("from", from);
                            itemMap.setPhotos(selectedImages);
                            i.putExtra("data", itemMap);
                            i.putExtra("from", from);
                            setResult(Activity.RESULT_OK, i);
                            finish();
                        }
                        break;
                        case "home": {
                            Intent i = new Intent(CameraViewActivity.this, AddProductDetail.class);
                            i.putExtra("from", from);
                            itemMap.setPhotos(selectedImages);
                            i.putExtra("data", itemMap);
                            i.putExtra("from", from);
                            if (Constants.ADDON_MAP_BOX_ENABLED) {
                                i.putExtra(Constants.TAG_LOCATION, location);
                            }
                            startActivity(i);
                            finish();
                        }
                        break;
                        case "add": {
                            Intent i = new Intent(CameraViewActivity.this, AddProductDetail.class);
                            i.putExtra("from", from);
                            itemMap.setPhotos(selectedImages);
                            i.putExtra("data", itemMap);
                            i.putExtra("from", from);
//                            startActivity(i);
                            setResult(Activity.RESULT_OK, i);
                            finish();
                        }
                        break;
                        default: {
                            Intent i = new Intent(CameraViewActivity.this, AddProductDetail.class);
                            i.putExtra("from", from);
                            startActivity(i);
                        }
                        break;
                    }

                }
            }
        });
        initListeners();
    }

    private void getDisplayWidthHeight(Context mContext) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = this.getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            displayHeight = windowMetrics.getBounds().height() - insets.bottom - insets.top;
            displayWidth = windowMetrics.getBounds().width() - insets.left - insets.right;
        } else {
            this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            displayHeight = displayMetrics.heightPixels;
            displayWidth = displayMetrics.widthPixels;
        }
    }

    private int totalImageCount() {
        return selectedImages.size();
    }

    private void initPermissionResult() {
        storagePermissionResult = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }

                if (granted) {
                    btnGallery.performClick();




                } /*else {
                    boolean neverAsked = false;
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(CameraViewActivity.this, x.getKey())) {
                                storagePermissionResult.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(mContext, new OkayCancelCallback() {
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
                                }, mContext.getString(R.string.storage_error));
                            }
                            break;
                        }
                    }
                }*/

            }
        });
    }

/*
    private void requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            storagePermissionResult.launch(PermissionsUtils.READ_STORAGE_PERMISSION);
        } else {
            storagePermissionResult.launch(PermissionsUtils.READ_WRITE_PERMISSIONS);
        }
    }
*/

    private void initListeners() {

        runOnUiThread(new Runnable() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {

                imagesAdapter = new ImagesAdapter(CameraViewActivity.this, temp);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(imagesAdapter);
                imagesAdapter.notifyDataSetChanged();

               /* if (flash) {
                    btnFlash.setSelected(true);
                    btnFlash.setColorFilter(getResources().getColor(R.color.colorPrimary));
                } else {
                    btnFlash.setSelected(false);
                    btnFlash.setColorFilter(null);
                }*/
            }
        });


        btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                    lensFacing = CameraSelector.LENS_FACING_BACK;
//                    setViewVisibility(btnFlash, View.INVISIBLE);
                } else {
                    lensFacing = CameraSelector.LENS_FACING_FRONT;
//                    setViewVisibility(btnFlash, View.INVISIBLE);
                }
                // Re-bind use cases to update selected camera
                startCamera();
                Animation scaling = AnimationUtils.loadAnimation(CameraViewActivity.this, R.anim.fade_in);
                btnSwitchCamera.startAnimation(scaling);
            }
        });

        btnFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: " + btnFlash.isSelected());
                if (btnFlash.isSelected()) {
                    flash = false;
                    btnFlash.setColorFilter(null);
                    btnFlash.setSelected(false);
                    flashMode = ImageCapture.FLASH_MODE_OFF;
                } else {
                    flash = true;
                    btnFlash.setColorFilter(getResources().getColor(R.color.colorPrimary));
                    btnFlash.setSelected(true);
                    flashMode = ImageCapture.FLASH_MODE_ON;
                }
                Animation scaling = AnimationUtils.loadAnimation(CameraViewActivity.this, R.anim.fade_in);
                btnFlash.startAnimation(scaling);
                // Re-bind use cases to update selected camera
                if (imageCapture != null)
                    imageCapture.setFlashMode(flashMode);
//                startCamera();
            }
        });

        /*permissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.requestPermissions(true, true);
            }
        });*/


        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });


        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int readPermission =0;
                if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    readPermission = ContextCompat.checkSelfPermission(CameraViewActivity.this, READ_MEDIA_IMAGES);
                } else {
                    readPermission = ContextCompat.checkSelfPermission(CameraViewActivity.this, READ_EXTERNAL_STORAGE);
                }
                //Log.d(TAG, "CheckPermission: " + readPermission);
                if (readPermission == PackageManager.PERMISSION_GRANTED) {
                    Intent in = new Intent(CameraViewActivity.this, AlbumSelectActivity.class);
                    startActivityForResult(in, REQUEST_CODE);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        galleryResultLauncher.launch(PermissionsUtils.READ_MEDIA_IMAGESPERMISSION);
                    } else {
                        ActivityCompat.requestPermissions(CameraViewActivity.this, new String[]{READ_EXTERNAL_STORAGE}, 103);
                    }
                }
            }
        });

      /*  btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statusType = Constants.TAG_IMAGE;
                btnCapture.setImageDrawable(ContextCompat.getDrawable(CameraViewActivity.this, R.drawable.camera_normal));
                btnVideo.setImageDrawable(ContextCompat.getDrawable(CameraViewActivity.this, R.drawable.video));
                btnImage.setImageDrawable(ContextCompat.getDrawable(CameraViewActivity.this, R.drawable.camera_selected));
            }
        });

        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                statusType = Constants.TAG_VIDEO;
                btnCapture.setImageDrawable(ContextCompat.getDrawable(CameraViewActivity.this, R.drawable.camera_recorder));
                btnVideo.setImageDrawable(ContextCompat.getDrawable(CameraViewActivity.this, R.drawable.video_selected));
                btnImage.setImageDrawable(ContextCompat.getDrawable(CameraViewActivity.this, R.drawable.camera_unselected));
            }
        });*/

/*
        btnStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CameraViewActivity.this, TextStatus.class);
                startActivityForResult(intent, Constants.STATUS_TEXT_CODE);
            }
        });
*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 103) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent in = new Intent(CameraViewActivity.this, AlbumSelectActivity.class);
                startActivityForResult(in, REQUEST_CODE);
            } else {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 100);
                Toast.makeText(CameraViewActivity.this, getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (from.equals("add") || from.equals("edit")) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        } else {
            finish();
        }
        super.onBackPressed();
    }

    private void captureImage() {
        statusType = Constants.TAG_IMAGE;
        File photoFile = imageStorage.getTempFileNew(CameraViewActivity.this, System.currentTimeMillis() + ".JPG");
        ImageCapture.Metadata metaData = new ImageCapture.Metadata();
        metaData.setReversedHorizontal(lensFacing == CameraSelector.LENS_FACING_FRONT);
        // Create output options object which contains file + metadata
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile)
                .setMetadata(metaData)
                .build();
        imageCapture.takePicture(outputOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                Log.d(TAG, "onImageSaved: " + "Captured");
                Log.d(TAG, "onImageSaved: " + outputFileResults.getSavedUri());
                HomeItemResponse.Photo photo = new HomeItemResponse().new Photo();
                photo.setType("path");
                photo.setPathType(Constants.TAG_CAMERA);
                photo.setImage(photoFile.getAbsolutePath());
                temp.add(photo);
                refreshGallery(imageStorage.getTempFileNew(CameraViewActivity.this, System.currentTimeMillis() + ".JPG"));
                selectedImages.add(photo);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imagesAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {

            }
        });
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }


/*
    private void saveFileInGallery(File srcFile, String from, String fileType) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String fileName = mContext.getString(R.string.app_name) + "_" + (System.currentTimeMillis() / 1000) + "." + FilenameUtils.getExtension(srcFile.getName());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    storageManager.saveFileInStorageV10(srcFile, fileName, from, fileType);
                } else {
                    storageManager.saveFileInGallery(srcFile, fileName, from);
                }
            }
        });
    }
*/


    // function setup countdown timerHandler for video recording

    public void resetVideoRecording() {
        if (longClickTimer != null) longClickTimer.cancel();
        if (videoTimer != null) {
            videoTimer.cancel();
//            videoTimer = null;
        }
    }

    @SuppressLint("RestrictedApi")
    private void stopRecorder() {
        isVideoStarted = false;
        txtTapHold.setVisibility(View.VISIBLE);
        if (videoCapture != null) {
            videoCapture.stopRecording();
        }
    }

    /**
     * Returns true if the device has an available back camera. False otherwise
     */
    private boolean hasBackCamera() {
        try {
            return cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Returns true if the device has an available front camera. False otherwise
     */
    private boolean hasFrontCamera() {
        try {
            return cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA);
        } catch (CameraInfoUnavailableException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setViewVisibility(View view, int visible) {
        view.setVisibility(visible);
    }


    @Override
    public void onResume() {
        super.onResume();

        statusType = Constants.TAG_IMAGE;
        startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mContext = null;
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }

    private void startCamera() {
        // Wait for the views to be properly laid out
        cameraView.post(new Runnable() {
            @Override
            public void run() {
                // Bind use cases
                bindCameraUseCases();
            }
        });
    }

    private void bindCameraUseCases() {
        int screenAspectRatio = aspectRatio(displayWidth, displayHeight);
        if (cameraView != null && cameraView.getDisplay() != null) {
            // Bind the CameraProvider to the LifeCycleOwner
            CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
            int rotation = cameraView.getDisplay().getRotation();
            // CameraSelector
            cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
            CameraSelector finalCameraSelector = cameraSelector;
            cameraProviderFuture.addListener(new Runnable() {
                @SuppressLint("RestrictedApi")
                @Override
                public void run() {
                    try {
                        cameraProvider = cameraProviderFuture.get();
                        // Preview
                        preview = new Preview.Builder()
                                // We request aspect ratio but no resolution
                                .setTargetAspectRatio(screenAspectRatio)
                                // Set initial target rotation
                                .setTargetRotation(rotation)
                                .build();
                        // Attach the viewfinder's surface provider to preview use case
                        preview.setSurfaceProvider(cameraView.getSurfaceProvider());
                        // Build and bind the camera use cases
                        // ImageCapture
                        imageCapture = new ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                // We request aspect ratio but no resolution to match preview config, but letting
                                // CameraX optimize for whatever specific resolution best fits our use cases
                                .setTargetAspectRatio(screenAspectRatio)
                                // Set initial target rotation, we will have to call this again if rotation changes
                                // during the lifecycle of this use case
                                .setTargetRotation(rotation)
                                .setFlashMode(flashMode)
                                .build();

                        // Must unbind the use-cases before rebinding them
                        cameraProvider.unbindAll();

                        try {
                            Log.d(TAG, "bindCameraUseCases: " + statusType);
                            // A variable number of use-cases can be passed here -
                            // camera provides access to CameraControl & CameraInfo
                            camera = cameraProvider.bindToLifecycle(
                                    (LifecycleOwner) mContext, finalCameraSelector, preview, imageCapture);
                        } catch (Exception exc) {
                            Log.e(TAG, "bindCameraUseCases: " + exc.getMessage());
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, ContextCompat.getMainExecutor(mContext));
        }
    }


    /**
     * [androidx.camera.core.ImageAnalysisConfig] requires enum value of
     * [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     * <p>
     * Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     * of preview ratio to one of the provided values.
     *
     * @param width  - preview width
     * @param height - preview height
     * @return suitable aspect ratio
     */
    private int aspectRatio(int width, int height) {
        int previewRatio = Math.max(width, height) / Math.min(width, height);
        if (Math.abs(previewRatio - RATIO_4_3_VALUE) <= Math.abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3;
        }
        return AspectRatio.RATIO_16_9;
    }

    /**
     * Inflate camera controls and update the UI manually upon config changes to avoid removing
     * and re-adding the view finder from the view hierarchy; this provides a seamless rotation
     * transition on devices that support it.
     * <p>
     * NOTE: The flag is supported starting in Android 8 but there still is a small flash on the
     * screen for devices that run Android 9 or below.
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Rebind the camera with the updated display metrics
        startCamera();
    }

    /**
     * Use external media if it is available, our app's file directory otherwise
     */
    public File getImageOutputFile(File mDestDir, String FILENAME, String PHOTO_EXTENSION) {
        File mDestFile = new File(mDestDir.getPath() + File.separator + new SimpleDateFormat(FILENAME, Locale.US)
                .format(System.currentTimeMillis()) + PHOTO_EXTENSION);
        return mDestFile;
    }

    public File getVideoOutputFile(File mDestDir, String FILENAME, String VIDEO_EXTENSION) {
        File mDestFile = new File(mDestDir.getPath() + File.separator + new SimpleDateFormat(FILENAME, Locale.US)
                .format(System.currentTimeMillis()) + VIDEO_EXTENSION);
        return mDestFile;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                try {
                    if (data.getStringExtra("fromGallery") != null) {
                        ArrayList<Photo> photoList = (ArrayList<Photo>) data.getExtras().get("galleryData");
                        Log.i(TAG, "onActivityResult: " + photoList.size());
                        for (Photo selectedImage : photoList) {
                            HomeItemResponse.Photo photo = new HomeItemResponse().new Photo();
                            photo.setType(selectedImage.getType());
                            photo.setImage("" + selectedImage.getImage());
                            photo.setPathType(Constants.TAG_GALLERY);
                            temp.add(photo);
                            selectedImages.add(photo);
                            Log.e("selectedImage", "-" + selectedImage.getImage());
                        }
                    }
                    //temp.addAll(GalleryActivity.images);
                    else {
                        temp.addAll(null);
                        selectedImages.addAll(temp);
                    }
                    imagesAdapter.notifyDataSetChanged();

                    //	new ImageUploadTask().execute(picturePath);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (OutOfMemoryError ome) {
                    ome.printStackTrace();
                }
            }
        }
    }


    public String getMimeType(Uri uri) {
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = this.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        Log.d(TAG, "getMimeType: " + mimeType);
        return mimeType;
    }


/*
    private void previewMedia(String filePath, String type) {
        if (type.equals(Constants.TAG_VIDEO)) {
            if (!TextUtils.isEmpty(filePath)) {
                setActivity(filePath, Constants.TAG_GALLERY);
            }
            */
/*
            Bitmap thumb = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    thumb = getContentResolver().loadThumbnail(storageManager.getUriFromFile(new File(filePath)), Utils.getBitmapSize(mContext), null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                thumb = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
            }
            if (thumb != null) {
                String fileName = (System.currentTimeMillis() / 1000L) + ".jpg";
                Uri savedThumbUri = null;
                String savedThumbPath = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    savedThumbUri = storageManager.saveImageInStorageV10(thumb, fileName, StorageManager.TAG_THUMB);
                } else {
                    savedThumbPath = storageManager.saveImageInStorage(thumb, fileName, StorageManager.TAG_THUMB).getAbsolutePath();
                }
                if (savedThumbUri != null || savedThumbPath != null) {
                    setActivity(filePath, Constants.TAG_GALLERY);
                }
            }
        *//*

        } else {
            setActivity(filePath, Constants.TAG_GALLERY);
        }
    }
*/

    private int getMediaDuration(Uri uriOfFile) {
        MediaPlayer mp = MediaPlayer.create(this, uriOfFile);
        return mp.getDuration();
    }

    private String getTime(long time) {
        int sec = (int) (time / 1000);
        String seconds;
        if (sec >= 10) {
            seconds = "00:" + sec;
        } else {
            seconds = "00:0" + sec;
        }
        Log.i(TAG, "getTime: " + seconds);
        return seconds;
    }


    public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {
        private final Context mContext;
        ArrayList<HomeItemResponse.Photo> imgAry;

        public ImagesAdapter(Context ctx, ArrayList<HomeItemResponse.Photo> data) {
            mContext = ctx;
            imgAry = data;
        }

        @Override
        public ImagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.singleimage, parent, false);
            return new ImagesAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ImagesAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            holder.singleImage.setVisibility(View.VISIBLE);

            final HomeItemResponse.Photo tempMap = imgAry.get(position);
            Log.d(TAG, "onBindViewHolder: " + new Gson().toJson(tempMap));
            if (selectedImages.contains(tempMap)) {
                holder.gradient.setVisibility(View.VISIBLE);
                holder.tick.setVisibility(View.VISIBLE);
            } else {
                holder.gradient.setVisibility(View.GONE);
                holder.tick.setVisibility(View.GONE);
            }

            holder.singleImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!selectedImages.contains(tempMap)) {
                        selectedImages.add(tempMap);
                    } else {
                        selectedImages.remove(tempMap);
                    }
                    notifyItemChanged(position);
                }
            });
            if (tempMap.getType() != null && tempMap.getType().equals(Constants.TAG_PATH)) {
                Picasso.get().load("file://" + tempMap.getImage()).resize(100, 100).centerCrop().into(holder.singleImage);
            } else if (tempMap.getItemUrlMainOriginal() != null) {
                Picasso.get().load(tempMap.getItemUrlMainOriginal()).resize(100, 100).centerCrop().into(holder.singleImage);
            } else if (tempMap.getImage() != null) {
                Picasso.get().load(tempMap.getImage()).resize(100, 100).centerCrop().into(holder.singleImage);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return imgAry.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView singleImage, gradient, tick;

            public ViewHolder(View itemView) {
                super(itemView);
                singleImage = (ImageView) itemView.findViewById(R.id.imageView);
                gradient = (ImageView) itemView.findViewById(R.id.imageView2);
                tick = (ImageView) itemView.findViewById(R.id.tick);

            }
        }

    }

    public void initLauncher() {
        galleryResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                boolean granted = true;
                Log.d(TAG, "onActivityResult: " + result);
                for (Map.Entry<String, Boolean> x : result.entrySet()) {
                    if (!x.getValue()) granted = false;
                }

                if (granted) {
                    Intent in = new Intent(CameraViewActivity.this, AlbumSelectActivity.class);
                    startActivityForResult(in, REQUEST_CODE);

                } else {
                    boolean neverAsked = false;
                    Log.d(TAG, "requestPermissions: ");
                    for (Map.Entry<String, Boolean> x : result.entrySet()) {
                        if (!x.getValue()) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(CameraViewActivity.this, x.getKey())) {
                                galleryResultLauncher.launch(result.keySet().toArray(new String[result.size()]));
                            } else {
                                PermissionsUtils.openPermissionDialog(CameraViewActivity.this, new OkayCancelCallback() {
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
                                }, CameraViewActivity.this.getString(R.string.read_media_image));
                            }
                            break;
                        }
                    }
                }

            }
        });
    }

}
