package com.ni.external.multipleimageselect.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.ni.external.multipleimageselect.adapters.CustomAlbumSelectAdapter;
import com.ni.external.multipleimageselect.helpers.Constants;
import com.ni.external.multipleimageselect.models.Album;
import com.ni.SnatchItUp.R;

import java.util.ArrayList;
import java.util.HashSet;

public class AlbumSelectActivity extends HelperActivity {
    private static final String TAG = AlbumSelectActivity.class.getSimpleName();
    private ArrayList<Album> albums;

    private TextView errorDisplay;

    private ProgressBar progressBar;
    private GridView gridView;
    private CustomAlbumSelectAdapter adapter;

    private ActionBar actionBar;

    private ContentObserver observer;
    private Handler handler;
    private Thread thread;

    private String[] projection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            ? new String[] { MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.MIME_TYPE
            }
            : new String[] {
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.MIME_TYPE,
                    MediaStore.Images.ImageColumns.DATA
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_select);
        setView(findViewById(R.id.layout_album_select));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        Constants.limit = intent.getIntExtra(Constants.INTENT_EXTRA_LIMIT, Constants.DEFAULT_LIMIT);

        boolean fromEditProfile = intent.getBooleanExtra("fromEditProfile", false);

        errorDisplay = (TextView) findViewById(R.id.text_view_error);
        errorDisplay.setVisibility(View.INVISIBLE);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar_album_select);
        gridView = (GridView) findViewById(R.id.grid_view_album_select);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ImageSelectActivity.class);
                intent.putExtra(Constants.INTENT_EXTRA_ALBUM_ID, albums.get(position).bucketId);
                intent.putExtra(Constants.INTENT_EXTRA_ALBUM, albums.get(position).bucketName);
                intent.putExtra("fromEditProfile", fromEditProfile);
                startActivityForResult(intent, Constants.REQUEST_CODE);
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
                        loadAlbums();
                        break;
                    }

                    case Constants.FETCH_STARTED: {
                        progressBar.setVisibility(View.VISIBLE);
                        gridView.setVisibility(View.INVISIBLE);
                        break;
                    }

                    case Constants.FETCH_COMPLETED: {
                        if (adapter == null) {
                            adapter = new CustomAlbumSelectAdapter(getApplicationContext(), albums);
                            gridView.setAdapter(adapter);

                            progressBar.setVisibility(View.INVISIBLE);
                            gridView.setVisibility(View.VISIBLE);
                            orientationBasedUI(getResources().getConfiguration().orientation);

                        } else {
                            adapter.notifyDataSetChanged();
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
            public void onChange(boolean selfChange, Uri uri) {
                loadAlbums();
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
        albums = null;
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
            int size = orientation == Configuration.ORIENTATION_PORTRAIT ? metrics.widthPixels / 2
                    : metrics.widthPixels / 4;
            adapter.setLayoutParams(size);
        }
        // gridView.setNumColumns(orientation == Configuration.ORIENTATION_PORTRAIT ? 2
        // : 4);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE
                && resultCode == RESULT_OK) {
            data.putExtra("fromGallery", data.getStringExtra("fromGallery"));
            setResult(RESULT_OK, data);
            finish();
        }
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

    private void loadAlbums() {
        startThread(new AlbumLoaderRunnable());
    }

    private class AlbumLoaderRunnable implements Runnable {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (adapter == null) {
                sendMessage(Constants.FETCH_STARTED);
            }
            Uri collection = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            } else {
                collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            }

            Cursor bucketCursor = getApplicationContext().getContentResolver()
                    .query(collection, projection, null, null,
                            MediaStore.Images.Media.DATE_ADDED);

            if (bucketCursor == null) {
                sendMessage(Constants.ERROR);
                return;
            }

            ArrayList<Album> temp = new ArrayList<>();
            HashSet<Long> albumSet = new HashSet<>();

            if (bucketCursor != null && bucketCursor.getCount() > 0) {
                if (bucketCursor.moveToLast()) {
                    do {
                        if (Thread.interrupted()) {
                            return;
                        }
                        long bucketId = -1, mediaId = -1;
                        String bucketName = null;
                        String mimeType = null;
                        Uri bucketUri = null;
                        String bucketPath = null;
                        bucketId = bucketCursor
                                .getLong(bucketCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            bucketName = bucketCursor.getString(
                                    bucketCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                        } else {
                            bucketName = bucketCursor.getString(
                                    bucketCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                        }
                        mimeType = bucketCursor.getString(
                                bucketCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.MIME_TYPE));

                        if (!albumSet.contains(bucketId)) {
                            mediaId = bucketCursor
                                    .getLong(bucketCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                bucketUri = Uri.withAppendedPath(collection, "" + mediaId);
                            } else {
                                bucketPath = bucketCursor
                                        .getString(bucketCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                                // Log.d(TAG, "AlbumLoaderRunnable: " + bucketPath);
                            }
                            /*
                             * It may happen that some image file paths are still present in cache,
                             * though image file does not exist. These last as long as media
                             * scanner is not run again. To avoid get such image file paths, check
                             * if image file exists.
                             */
                            temp.add(new Album(bucketId, bucketName, bucketPath, bucketUri));
                            albumSet.add(bucketId);
                            /*
                             * file = new File(image);
                             * if (file.exists()) {
                             * temp.add(new Album(album, image,uriImage,picturePath));
                             * albumSet.add(albumId);
                             * }
                             */
                        }

                    } while (bucketCursor.moveToPrevious());
                }
            }
            bucketCursor.close();

            if (albums == null) {
                albums = new ArrayList<>();
            }
            albums.clear();
            albums.addAll(temp);

            sendMessage(Constants.FETCH_COMPLETED);
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
        if (handler == null) {
            return;
        }

        Message message = handler.obtainMessage();
        message.what = what;
        message.sendToTarget();
    }

    @Override
    protected void permissionGranted() {
        Message message = handler.obtainMessage();
        message.what = Constants.PERMISSION_GRANTED;
        message.sendToTarget();
    }

    @Override
    protected void hideViews() {
        progressBar.setVisibility(View.INVISIBLE);
        gridView.setVisibility(View.INVISIBLE);
    }
}
