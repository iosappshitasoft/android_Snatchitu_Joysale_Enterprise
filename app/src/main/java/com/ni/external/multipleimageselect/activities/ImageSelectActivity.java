
package com.ni.external.multipleimageselect.activities;

import static com.ni.external.FileUtils.isDownloadsDocument;
import static com.ni.external.FileUtils.isExternalStorageDocument;
import static com.ni.external.FileUtils.isGooglePhotosUri;
import static com.ni.external.FileUtils.isMediaDocument;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.ni.external.multipleimageselect.adapters.CustomImageSelectAdapter;
import com.ni.external.multipleimageselect.helpers.Constants;
import com.ni.external.multipleimageselect.models.Image;
import com.ni.external.multipleimageselect.models.Photo;
import com.ni.helper.ImageStorage;
import com.ni.SnatchItUp.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

public class ImageSelectActivity extends HelperActivity {
    /*
     * private static final String TAG = ImageSelectActivity.class.getSimpleName();
     * private ArrayList<Image> images;
     * private String album;
     * 
     * private TextView errorDisplay;
     * 
     * private ProgressBar progressBar;
     * private GridView gridView;
     * private CustomImageSelectAdapter adapter;
     * 
     * private ActionBar actionBar;
     * 
     * private ActionMode actionMode;
     * private int countSelected = 0;
     * 
     * private ContentObserver observer;
     * private Handler handler;
     * private Thread thread;
     * 
     * private final String[] projection = new String[]{MediaStore.Images.Media._ID,
     * MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA};
     * public ArrayList<Photo> selectedImgLists = new ArrayList<>();
     * private ArrayList<Integer> selectedPosLists = new ArrayList<>();
     * private int counter = 0;
     * 
     * @Override
     * protected void onCreate(Bundle savedInstanceState) {
     * super.onCreate(savedInstanceState);
     * setContentView(R.layout.activity_image_select);
     * setView(findViewById(R.id.layout_image_select));
     * 
     * Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
     * setSupportActionBar(toolbar);
     * selectedImgLists = new ArrayList<>();
     * actionBar = getSupportActionBar();
     * if (actionBar != null) {
     * actionBar.setDisplayHomeAsUpEnabled(true);
     * actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
     * 
     * actionBar.setDisplayShowTitleEnabled(true);
     * actionBar.setTitle(R.string.gallery);
     * }
     * 
     * Intent intent = getIntent();
     * if (intent == null) {
     * finish();
     * }
     * album = intent.getStringExtra(Constants.INTENT_EXTRA_ALBUM);
     * 
     * errorDisplay = (TextView) findViewById(R.id.text_view_error);
     * errorDisplay.setVisibility(View.INVISIBLE);
     * 
     * progressBar = (ProgressBar) findViewById(R.id.progress_bar_image_select);
     * gridView = (GridView) findViewById(R.id.grid_view_image_select);
     * gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
     * 
     * @Override
     * public void onItemClick(AdapterView<?> parent, View view, int position, long
     * id) {
     * String fileExtension;
     * fileExtension =
     * images.get(position).name.substring(images.get(position).name.lastIndexOf("."
     * ));
     * if (fileExtension.equalsIgnoreCase(".webp")) {
     * Toast.makeText(
     * getApplicationContext(),
     * String.format(getString(R.string.image_format_not_supported),
     * Constants.limit),
     * Toast.LENGTH_SHORT)
     * .show();
     * } else {
     * if (actionMode == null) {
     * actionMode = ImageSelectActivity.this.startActionMode(callback);
     * }
     * toggleSelection(position);
     * actionMode.setTitle(countSelected + " " + getString(R.string.selected));
     * if (countSelected == 0) {
     * actionMode.finish();
     * }
     * }
     * 
     * }
     * });
     * }
     * 
     * @Override
     * protected void onStart() {
     * super.onStart();
     * 
     * handler = new Handler() {
     * 
     * @Override
     * public void handleMessage(Message msg) {
     * switch (msg.what) {
     * case Constants.PERMISSION_GRANTED: {
     * loadImages();
     * break;
     * }
     * 
     * case Constants.FETCH_STARTED: {
     * progressBar.setVisibility(View.VISIBLE);
     * gridView.setVisibility(View.INVISIBLE);
     * break;
     * }
     * 
     * case Constants.FETCH_COMPLETED: {
     *//*
        * If adapter is null, this implies that the loaded images will be shown
        * for the first time, hence send FETCH_COMPLETED message.
        * However, if adapter has been initialised, this thread was run either
        * due to the activity being restarted or content being changed.
        *//*
           * if (adapter == null) {
           * adapter = new CustomImageSelectAdapter(getApplicationContext(), images);
           * gridView.setAdapter(adapter);
           * 
           * progressBar.setVisibility(View.INVISIBLE);
           * gridView.setVisibility(View.VISIBLE);
           * orientationBasedUI(getResources().getConfiguration().orientation);
           * 
           * } else {
           * adapter.notifyDataSetChanged();
           *//*
              * Some selected images may have been deleted
              * hence update action mode title
              *//*
                 * if (actionMode != null) {
                 * countSelected = msg.arg1;
                 * actionMode.setTitle(countSelected + " " + getString(R.string.selected));
                 * }
                 * }
                 * break;
                 * }
                 * 
                 * case Constants.ERROR: {
                 * progressBar.setVisibility(View.INVISIBLE);
                 * errorDisplay.setVisibility(View.VISIBLE);
                 * break;
                 * }
                 * 
                 * default: {
                 * super.handleMessage(msg);
                 * }
                 * }
                 * }
                 * };
                 * observer = new ContentObserver(handler) {
                 * 
                 * @Override
                 * public void onChange(boolean selfChange) {
                 * loadImages();
                 * }
                 * };
                 * getContentResolver().registerContentObserver(MediaStore.Images.Media.
                 * EXTERNAL_CONTENT_URI, false, observer);
                 * 
                 * checkPermission();
                 * }
                 * 
                 * @Override
                 * protected void onStop() {
                 * super.onStop();
                 * 
                 * stopThread();
                 * 
                 * getContentResolver().unregisterContentObserver(observer);
                 * observer = null;
                 * 
                 * if (handler != null) {
                 * handler.removeCallbacksAndMessages(null);
                 * handler = null;
                 * }
                 * }
                 * 
                 * @Override
                 * protected void onDestroy() {
                 * super.onDestroy();
                 * 
                 * if (actionBar != null) {
                 * actionBar.setHomeAsUpIndicator(null);
                 * }
                 * images = null;
                 * if (adapter != null) {
                 * adapter.releaseResources();
                 * }
                 * gridView.setOnItemClickListener(null);
                 * }
                 * 
                 * @Override
                 * public void onConfigurationChanged(Configuration newConfig) {
                 * super.onConfigurationChanged(newConfig);
                 * orientationBasedUI(newConfig.orientation);
                 * }
                 * 
                 * private void orientationBasedUI(int orientation) {
                 * final WindowManager windowManager = (WindowManager)
                 * getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                 * final DisplayMetrics metrics = new DisplayMetrics();
                 * windowManager.getDefaultDisplay().getMetrics(metrics);
                 * 
                 * if (adapter != null) {
                 * int size = orientation == Configuration.ORIENTATION_PORTRAIT ?
                 * metrics.widthPixels / 3 : metrics.widthPixels / 5;
                 * adapter.setLayoutParams(size);
                 * }
                 * gridView.setNumColumns(orientation == Configuration.ORIENTATION_PORTRAIT ? 3
                 * : 5);
                 * }
                 * 
                 * @Override
                 * public boolean onOptionsItemSelected(MenuItem item) {
                 * switch (item.getItemId()) {
                 * case android.R.id.home: {
                 * onBackPressed();
                 * return true;
                 * }
                 * 
                 * default: {
                 * return false;
                 * }
                 * }
                 * }
                 * 
                 * private ActionMode.Callback callback = new ActionMode.Callback() {
                 * 
                 * @Override
                 * public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                 * MenuInflater menuInflater = mode.getMenuInflater();
                 * menuInflater.inflate(R.menu.menu_contextual_action_bar, menu);
                 * 
                 * actionMode = mode;
                 * countSelected = 0;
                 * 
                 * return true;
                 * }
                 * 
                 * @Override
                 * public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                 * return false;
                 * }
                 * 
                 * @Override
                 * public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                 * int i = item.getItemId();
                 * if (i == R.id.menu_item_add_image) {
                 * sendIntent();
                 * return true;
                 * }
                 * return false;
                 * }
                 * 
                 * @Override
                 * public void onDestroyActionMode(ActionMode mode) {
                 * if (countSelected > 0) {
                 * deselectAll();
                 * }
                 * actionMode = null;
                 * }
                 * };
                 * 
                 * private void toggleSelection(int position) {
                 * if (!images.get(position).isSelected && countSelected >= Constants.limit) {
                 * Toast.makeText(
                 * getApplicationContext(),
                 * String.format(getString(R.string.limit_exceeded), Constants.limit),
                 * Toast.LENGTH_SHORT)
                 * .show();
                 * return;
                 * }
                 * 
                 * images.get(position).isSelected = !images.get(position).isSelected;
                 * if (images.get(position).isSelected) {
                 * countSelected++;
                 * } else {
                 * countSelected--;
                 * }
                 * adapter.notifyDataSetChanged();
                 * }
                 * 
                 * private void deselectAll() {
                 * for (int i = 0, l = images.size(); i < l; i++) {
                 * images.get(i).isSelected = false;
                 * }
                 * countSelected = 0;
                 * adapter.notifyDataSetChanged();
                 * }
                 * 
                 * 
                 * //checking image height, width and Size
                 * public boolean getDropboxIMGSize(String uri){
                 * BitmapFactory.Options options = new BitmapFactory.Options();
                 * options.inJustDecodeBounds = true;
                 * BitmapFactory.decodeFile(uri, options);
                 * int imageHeight = options.outHeight;
                 * int imageWidth = options.outWidth;
                 * Log.d(TAG, "getDropboxIMGSize: "+imageHeight);
                 * Log.d(TAG, "getDropboxIMGSize: "+imageWidth);
                 * return imageHeight >= 200 && imageWidth <= 640;
                 * }
                 * 
                 * private void sendIntent() {
                 * Intent intent = new Intent();
                 * selectedImgLists = getSelected();
                 * Log.d(TAG, "sendIntent: "+selectedImgLists.size());
                 * intent.putExtra("fromGallery", "enable");
                 * intent.putExtra("galleryData", selectedImgLists);
                 * //intent.putParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES,
                 * getSelected());
                 * setResult(RESULT_OK, intent);
                 * finish();
                 * }
                 * 
                 * private ArrayList<Photo> getSelected() {
                 * ArrayList<Photo> selectedImgs = new ArrayList<>();
                 * for (int i = 0, l = images.size(); i < l; i++) {
                 * if (images.get(i).isSelected) {
                 * Photo tmpMap = new Photo();
                 * tmpMap.setType("path");
                 * tmpMap.setImage(images.get(i).path);
                 * Log.i(TAG, "getSelected: " + tmpMap.getImage());
                 * // if(getDropboxIMGSize(images.get(i).path))
                 * selectedImgs.add(tmpMap);
                 *//*
                    * else
                    * Toast.makeText(getApplicationContext(),"Please select the valid image",Toast.
                    * LENGTH_LONG).show();
                    *//*
                       * }
                       * }
                       * return selectedImgs;
                       * }
                       * 
                       * private void loadImages() {
                       * startThread(new ImageLoaderRunnable());
                       * }
                       * 
                       * private class ImageLoaderRunnable implements Runnable {
                       * 
                       * @Override
                       * public void run() {
                       * Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                       *//*
                          * If the adapter is null, this is first time this activity's view is
                          * being shown, hence send FETCH_STARTED message to show progress bar
                          * while images are loaded from phone
                          *//*
                             * if (adapter == null) {
                             * sendMessage(Constants.FETCH_STARTED);
                             * }
                             * 
                             * File file;
                             * HashSet<Long> selectedImages = new HashSet<>();
                             * if (images != null) {
                             * Image image;
                             * for (int i = 0, l = images.size(); i < l; i++) {
                             * image = images.get(i);
                             * file = new File(image.path);
                             * if (file.exists() && image.isSelected) {
                             * selectedImages.add(image.id);
                             * }
                             * }
                             * }
                             * 
                             * Cursor cursor;
                             * 
                             *//*
                                * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                * cursor =
                                * getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                * projection,
                                * MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =?", new String[]{album},
                                * MediaStore.Images.Media.DATE_ADDED);
                                * } else {
                                * cursor =
                                * getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                * projection,
                                * MediaStore.Images.Media.DISPLAY_NAME + " =?", new String[]{album},
                                * MediaStore.Images.Media.DATE_ADDED);
                                * }
                                *//*
                                   * cursor =
                                   * getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                   * projection,
                                   * MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " =?", new String[]{album},
                                   * MediaStore.Images.Media.DATE_ADDED);
                                   * if (cursor == null) {
                                   * sendMessage(Constants.ERROR);
                                   * return;
                                   * }
                                   * 
                                   * 
                                   *//*
                                      * In case this runnable is executed to onChange calling loadImages,
                                      * using countSelected variable can result in a race condition. To avoid that,
                                      * tempCountSelected keeps track of number of selected images. On handling
                                      * FETCH_COMPLETED message, countSelected is assigned value of
                                      * tempCountSelected.
                                      *//*
                                         * int tempCountSelected = 0;
                                         * ArrayList<Image> temp = new ArrayList<>(cursor.getCount());
                                         * if (cursor.moveToLast()) {
                                         * do {
                                         * if (Thread.interrupted()) {
                                         * return;
                                         * }
                                         * 
                                         * long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                                         * String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                                         * String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                                         * boolean isSelected = selectedImages.contains(id);
                                         * if (isSelected) {
                                         * tempCountSelected++;
                                         * }
                                         * 
                                         * file = new File(path);
                                         * if (file.exists()) {
                                         * temp.add(new Image(id, name, path, isSelected));
                                         * }
                                         * 
                                         * } while (cursor.moveToPrevious());
                                         * }
                                         * cursor.close();
                                         * 
                                         * if (images == null) {
                                         * images = new ArrayList<>();
                                         * }
                                         * images.clear();
                                         * images.addAll(temp);
                                         * 
                                         * sendMessage(Constants.FETCH_COMPLETED, tempCountSelected);
                                         * }
                                         * }
                                         * 
                                         * private void startThread(Runnable runnable) {
                                         * stopThread();
                                         * thread = new Thread(runnable);
                                         * thread.start();
                                         * }
                                         * 
                                         * private void stopThread() {
                                         * if (thread == null || !thread.isAlive()) {
                                         * return;
                                         * }
                                         * 
                                         * thread.interrupt();
                                         * try {
                                         * thread.join();
                                         * } catch (InterruptedException e) {
                                         * e.printStackTrace();
                                         * }
                                         * }
                                         * 
                                         * private void sendMessage(int what) {
                                         * sendMessage(what, 0);
                                         * }
                                         * 
                                         * private void sendMessage(int what, int arg1) {
                                         * if (handler == null) {
                                         * return;
                                         * }
                                         * 
                                         * Message message = handler.obtainMessage();
                                         * message.what = what;
                                         * message.arg1 = arg1;
                                         * message.sendToTarget();
                                         * }
                                         * 
                                         * @Override
                                         * protected void permissionGranted() {
                                         * sendMessage(Constants.PERMISSION_GRANTED);
                                         * }
                                         * 
                                         * @Override
                                         * protected void hideViews() {
                                         * progressBar.setVisibility(View.INVISIBLE);
                                         * gridView.setVisibility(View.INVISIBLE);
                                         * }
                                         */
    private static final String TAG = ImageSelectActivity.class.getSimpleName();
    private ArrayList<Image> images;
    private ArrayList<Uri> imagesselects;
    private String album;
    private long albumId;

    private TextView errorDisplay;

    private ProgressBar progressBar;
    private GridView gridView;
    private CustomImageSelectAdapter adapter;

    private ActionBar actionBar;

    private ActionMode actionMode;
    private int countSelected = 0;

    private ContentObserver observer;
    private Handler handler;
    private Thread thread;
    static Context context;
    private final String[] projection = new String[] {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.MIME_TYPE
    };
    public ArrayList<Photo> selectedImgLists = new ArrayList<>();
    private ArrayList<Integer> selectedPosLists = new ArrayList<>();
    private int counter = 0;
    private static final int DEFAULT_MIN_WIDTH_QUALITY = 400; // min pixels
    private static final int DEFAULT_MIN_HEIGHT_QUALITY = 400; // min pixels

    private static int minWidthQuality = DEFAULT_MIN_WIDTH_QUALITY;
    private static int minHeightQuality = DEFAULT_MIN_HEIGHT_QUALITY;
    private boolean fromEditProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);
        setView(findViewById(R.id.layout_image_select));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        selectedImgLists = new ArrayList<>();
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            Drawable backIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_back);
            if (backIcon != null) {
                backIcon.setColorFilter(ContextCompat.getColor(this, R.color.primary), PorterDuff.Mode.SRC_IN);
                actionBar.setHomeAsUpIndicator(backIcon);
            }

            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.gallery);
        }
        imagesselects = new ArrayList<Uri>();

        context = getApplicationContext();

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }

        fromEditProfile = intent.getBooleanExtra("fromEditProfile", false); // Add this line
        album = intent.getStringExtra(Constants.INTENT_EXTRA_ALBUM);
        albumId = intent.getLongExtra(Constants.INTENT_EXTRA_ALBUM_ID, -1);

        errorDisplay = (TextView) findViewById(R.id.text_view_error);
        errorDisplay.setVisibility(View.INVISIBLE);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar_image_select);
        gridView = (GridView) findViewById(R.id.grid_view_image_select);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (actionMode == null) {
                    actionMode = ImageSelectActivity.this.startActionMode(callback);
                }

                toggleSelection(position);
                actionMode.setTitle(countSelected + " " + getString(R.string.selected));

                if (countSelected == 0) {
                    actionMode.finish();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.PERMISSION_GRANTED: {
                        loadImages();
                        break;
                    }

                    case Constants.FETCH_STARTED: {
                        progressBar.setVisibility(View.VISIBLE);
                        gridView.setVisibility(View.INVISIBLE);
                        break;
                    }

                    case Constants.FETCH_COMPLETED: {
                        /*
                         * If adapter is null, this implies that the loaded images will be shown
                         * for the first time, hence send FETCH_COMPLETED message.
                         * However, if adapter has been initialised, this thread was run either
                         * due to the activity being restarted or content being changed.
                         */
                        if (adapter == null) {
                            adapter = new CustomImageSelectAdapter(getApplicationContext(), images);
                            gridView.setAdapter(adapter);

                            progressBar.setVisibility(View.INVISIBLE);
                            gridView.setVisibility(View.VISIBLE);
                            orientationBasedUI(getResources().getConfiguration().orientation);

                        } else {
                            adapter.notifyDataSetChanged();
                            /*
                             * Some selected images may have been deleted
                             * hence update action mode title
                             */
                            if (actionMode != null) {
                                countSelected = msg.arg1;
                                actionMode.setTitle(countSelected + " " + getString(R.string.selected));
                            }
                        }
                        break;
                    }

                    case Constants.ERROR: {
                        progressBar.setVisibility(View.INVISIBLE);
                        errorDisplay.setVisibility(View.VISIBLE);
                        break;
                    }

                    default: {
                        super.handleMessage(msg);
                    }
                }
            }
        };
        observer = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                loadImages();
            }
        };
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer);

        checkPermission();
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopThread();

        getContentResolver().unregisterContentObserver(observer);
        observer = null;

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(null);
        }
        images = null;
        if (adapter != null) {
            adapter.releaseResources();
        }
        gridView.setOnItemClickListener(null);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        orientationBasedUI(newConfig.orientation);
    }

    private void orientationBasedUI(int orientation) {
        final WindowManager windowManager = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        if (adapter != null) {
            int size = orientation == Configuration.ORIENTATION_PORTRAIT ? metrics.widthPixels / 3
                    : metrics.widthPixels / 5;
            adapter.setLayoutParams(size);
        }
        gridView.setNumColumns(orientation == Configuration.ORIENTATION_PORTRAIT ? 3 : 5);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }

            default: {
                return false;
            }
        }
    }

    private ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater menuInflater = mode.getMenuInflater();
            menuInflater.inflate(R.menu.menu_contextual_action_bar, menu);

            actionMode = mode;
            countSelected = 0;

            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
            }

            MenuItem addItem = menu.findItem(R.id.menu_item_add_image);
            if (addItem != null && addItem.getIcon() != null) {
                Drawable icon = addItem.getIcon();
                icon.setColorFilter(ContextCompat.getColor(context, R.color.primary), PorterDuff.Mode.SRC_IN);
                addItem.setIcon(icon);
            }

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int i = item.getItemId();
            if (i == R.id.menu_item_add_image) {
                sendIntent();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (countSelected > 0) {
                deselectAll();
            }
            actionMode = null;

            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(true);
            }
        }
    };

    private void toggleSelection(int position) {
        /*
         * if (!images.get(position).isSelected && countSelected >= Constants.limit) {
         * Toast.makeText(
         * getApplicationContext(),
         * String.format(getString(R.string.limit_exceeded), Constants.limit),
         * Toast.LENGTH_SHORT)
         * .show();
         * return;
         * }
         * 
         * images.get(position).isSelected = !images.get(position).isSelected;
         * if (images.get(position).isSelected) {
         * if (!imagesselects.contains(images.get(position).path)) {
         * imagesselects.add(images.get(position).path);
         * }
         * countSelected++;
         * } else {
         * imagesselects.remove(images.get(position).path);
         * countSelected--;
         * }
         * adapter.notifyDataSetChanged();
         */
        if (fromEditProfile) { // If fromEditProfile is true
            if (countSelected == 1 && !images.get(position).isSelected) {
                Toast.makeText(
                        getApplicationContext(),
                        "Kindly select one image",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if (!images.get(position).isSelected && countSelected >= Constants.limit) {
                Toast.makeText(
                        getApplicationContext(),
                        String.format(getString(R.string.limit_exceeded), Constants.limit),
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        images.get(position).isSelected = !images.get(position).isSelected;
        if (images.get(position).isSelected) {
            if (!imagesselects.contains(images.get(position).path)) {
                imagesselects.add(images.get(position).path);
            }
            countSelected++;
        } else {
            imagesselects.remove(images.get(position).path);
            countSelected--;
        }
        adapter.notifyDataSetChanged();
    }

    private void deselectAll() {
        for (int i = 0, l = images.size(); i < l; i++) {
            images.get(i).isSelected = false;
        }
        countSelected = 0;
        adapter.notifyDataSetChanged();
    }

    private void sendIntent() {
        Intent intent = new Intent();
        selectedImgLists = getSelected();
        intent.putExtra("fromGallery", "enable");
        intent.putExtra("galleryData", selectedImgLists);
        setResult(RESULT_OK, intent);
        finish();
    }

    private ArrayList<Photo> getSelected() {
        ArrayList<Photo> selectedImgs = new ArrayList<>();
        for (int i = 0, l = imagesselects.size(); i < l; i++) {
            Photo tmpMap = new Photo();
            tmpMap.setType("path");
            Log.d(TAG, "getSelected: " + imagesselects.get(i).toString());
            tmpMap.setImage(getRealPath(this, Uri.parse(imagesselects.get(i).toString()), i));
            Log.d(TAG, "getSelected: " + images.get(i).path);
            // Log.d(TAG, "getSelectedssssssss: " + getRealPath(this,images.get(i).path));
            selectedImgs.add(tmpMap);
        }
        return selectedImgs;
    }

    public String getRealPath(Context context, Uri uri, int pos) {
        String path = "";
        /*
         * if (path == null) {
         * path = getPathFromRemoteUri(context, uri);
         * }
         */

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {

            Bitmap bm = decodeBitmap(context, uri);
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000L) + pos;
            ImageStorage imageStorage = new ImageStorage(ImageSelectActivity.this);
            path = imageStorage.saveImageInStorage(bm, "", timestamp + ".jpg");
            Log.d(TAG, "getRealPath10: " + path + " timestamp" + timestamp + ".jpg");

        } else {
            path = getPathFromLocalUri(context, uri);
            Log.d(TAG, "getRealPath: " + path + " ");
        }

        return path;
    }

    private static Bitmap decodeBitmap(Context context, Uri theUri) {
        Bitmap outputBitmap = null;
        AssetFileDescriptor fileDescriptor = null;

        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(theUri, "r");

            // Get size of bitmap file
            BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
            boundsOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, boundsOptions);

            // Get desired sample size. Note that these must be powers-of-two.
            int[] sampleSizes = new int[] { 8, 4, 2, 1 };
            int selectedSampleSize = 1; // 1 by default (original image)

            for (int sampleSize : sampleSizes) {
                selectedSampleSize = sampleSize;
                int targetWidth = boundsOptions.outWidth / sampleSize;
                int targetHeight = boundsOptions.outHeight / sampleSize;
                if (targetWidth >= minWidthQuality && targetHeight >= minHeightQuality) {
                    break;
                }
            }

            // Decode bitmap at desired size
            BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
            decodeOptions.inSampleSize = selectedSampleSize;
            outputBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, decodeOptions);
            if (outputBitmap != null) {
                Log.i(TAG, "Loaded image with sample size " + decodeOptions.inSampleSize + "\t\t"
                        + "Bitmap width: " + outputBitmap.getWidth()
                        + "\theight: " + outputBitmap.getHeight());
            }
            fileDescriptor.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputBitmap;
    }

    @SuppressLint("NewApi")
    public String getPathFromLocalUri(final Context context, final Uri uri) {
        // check here to is it KITKAT or new version
        final boolean isKitKatOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        String selection = null;
        String[] selectionArgs = null;
        // DocumentProvider
        if (isKitKatOrAbove && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                String fullPath = getPathFromExtSD(split);
                if (fullPath != "") {
                    return fullPath;
                } else {
                    return null;
                }
            }

            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    final String id;
                    Cursor cursor = null;
                    try {
                        cursor = context.getContentResolver().query(uri,
                                new String[] { MediaStore.MediaColumns.DISPLAY_NAME }, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            String fileName = cursor.getString(0);
                            String path = Environment.getExternalStorageDirectory().toString() + "/Download/"
                                    + fileName;
                            if (!TextUtils.isEmpty(path)) {
                                return path;
                            }
                        }
                    } finally {
                        if (cursor != null)
                            cursor.close();
                    }
                    id = DocumentsContract.getDocumentId(uri);
                    if (!TextUtils.isEmpty(id)) {
                        if (id.startsWith("raw:")) {
                            return id.replaceFirst("raw:", "");
                        }
                        String[] contentUriPrefixesToTry = new String[] {
                                "content://downloads/public_downloads",
                                "content://downloads/my_downloads"
                        };
                        for (String contentUriPrefix : contentUriPrefixesToTry) {
                            try {
                                final Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix),
                                        Long.valueOf(id));

                                return getDataColumn(context, contentUri, null, null);
                            } catch (NumberFormatException e) {
                                // In Android 8 and Android P the id is not a number
                                return uri.getPath().replaceFirst("^/document/raw:", "").replaceFirst("^raw:", "");
                            }
                        }
                    }

                } else {
                    final String id = DocumentsContract.getDocumentId(uri);
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                    Uri contentUri = null;
                    try {
                        contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    if (contentUri != null) {
                        return getDataColumn(context, contentUri, null, null);
                    }
                }
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            } /*
               * else if (isGoogleDriveUri(uri)) {
               * return getDriveFilePath(uri, context);
               * }
               */
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {

            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            /*
             * if (isGoogleDriveUri(uri)) {
             * return getDriveFilePath(uri, context);
             * }
             */
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
                return getMediaFilePathForN(uri, context);
            } else {
                return getDataColumn(context, uri, null, null);
            }
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static String getPathFromExtSD(String[] pathData) {
        final String type = pathData[0];
        final String relativePath = "/" + pathData[1];
        String fullPath = "";

        if ("primary".equalsIgnoreCase(type)) {
            fullPath = Environment.getExternalStorageDirectory() + relativePath;
            if (fileExists(fullPath)) {
                return fullPath;
            }
        }

        fullPath = System.getenv("SECONDARY_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        fullPath = System.getenv("EXTERNAL_STORAGE") + relativePath;
        if (fileExists(fullPath)) {
            return fullPath;
        }

        return fullPath;
    }

    private static boolean fileExists(String filePath) {
        File file = new File(filePath);

        return file.exists();
    }

    private static String getMediaFilePathForN(Uri uri, Context context) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);

        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        String name = (returnCursor.getString(nameIndex));
        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        File file = new File(context.getFilesDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();

            // int bufferSize = 1024;
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);

            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            Log.e("File Size", "Size " + file.length());
            inputStream.close();
            outputStream.close();
            Log.e("File Path", "Path " + file.getPath());
            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
        return file.getPath();
    }

    private static String getDataColumn(Context context, Uri uri,
            String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);

            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return null;
    }

    private void loadImages() {
        startThread(new ImageLoaderRunnable());
    }

    private class ImageLoaderRunnable implements Runnable {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            /*
             * If the adapter is null, this is first time this activity's view is
             * being shown, hence send FETCH_STARTED message to show progress bar
             * while images are loaded from phone
             */
            if (adapter == null) {
                sendMessage(Constants.FETCH_STARTED);
            }

            File file;
            HashSet<Long> selectedImages = new HashSet<>();
            if (images != null) {
                Image image;
                for (int i = 0, l = images.size(); i < l; i++) {
                    image = images.get(i);
                    file = new File(image.path.getPath());
                    if (file.exists() && image.isSelected) {
                        selectedImages.add(image.id);
                    }
                }
            }
            String selection = "(" + MediaStore.Images.Media.BUCKET_ID + " =?" + ") AND (" +
                    MediaStore.Images.Media.MIME_TYPE + " =? " + " OR " +
                    MediaStore.Images.Media.MIME_TYPE + " =? " + " OR " +
                    MediaStore.Images.Media.MIME_TYPE + " =? )";
            String[] selectionArgs = new String[] { "" + albumId, "image/png",
                    "image/jpeg", "image/bmp" };
            // Log.d(TAG, "ImageLoaderRunnable: " + albumId + ", " + album);

            Cursor cursor;
            cursor = getApplicationContext().getContentResolver()
                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                            selection, selectionArgs, MediaStore.Images.Media.DATE_ADDED);
            if (cursor == null) {
                sendMessage(Constants.ERROR);
                return;
            }

            /*
             * In case this runnable is executed to onChange calling loadImages,
             * using countSelected variable can result in a race condition. To avoid that,
             * tempCountSelected keeps track of number of selected images. On handling
             * FETCH_COMPLETED message, countSelected is assigned value of
             * tempCountSelected.
             */
            int tempCountSelected = 0;
            ArrayList<Image> temp = new ArrayList<>(cursor.getCount());
            if (cursor.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return;
                    }
                    Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    long albumId = cursor.getLong(cursor.getColumnIndex(projection[0]));
                    String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                    Uri uriImage = Uri.withAppendedPath(images, "" + albumId);
                    boolean isSelected = selectedImages.contains(albumId);
                    if (isSelected) {
                        tempCountSelected++;
                    }
                    temp.add(new Image(albumId, name, uriImage, isSelected));

                } while (cursor.moveToPrevious());
            }
            cursor.close();

            if (images == null) {
                images = new ArrayList<>();
            }
            images.clear();
            images.addAll(temp);

            sendMessage(Constants.FETCH_COMPLETED, tempCountSelected);
        }
    }

    private void startThread(Runnable runnable) {
        stopThread();
        thread = new Thread(runnable);
        thread.start();
    }

    private void stopThread() {
        if (thread == null || !thread.isAlive()) {
            return;
        }

        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(int what) {
        sendMessage(what, 0);
    }

    private void sendMessage(int what, int arg1) {
        if (handler == null) {
            return;
        }

        Message message = handler.obtainMessage();
        message.what = what;
        message.arg1 = arg1;
        message.sendToTarget();
    }

    @Override
    protected void permissionGranted() {
        sendMessage(Constants.PERMISSION_GRANTED);
    }

    @Override
    protected void hideViews() {
        progressBar.setVisibility(View.INVISIBLE);
        gridView.setVisibility(View.INVISIBLE);
    }
}
