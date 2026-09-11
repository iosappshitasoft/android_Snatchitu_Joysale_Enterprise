package com.ni.helper;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.google.android.datatransport.runtime.logging.Logging;
import com.ni.SnatchItUp.R;
import com.ni.utils.Constants;
import com.ni.utils.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class StorageUtils {
    private static final String TAG = StorageUtils.class.getSimpleName();
    private static StorageUtils mInstance;
    private final Context context;
    private String appDirectory;
    public static final String IMG_SENT_PATH = "Images/Sent/";
    public static final String IMG_PROFILE_PATH = "Images/Profile/";
    public static final String IMG_HOME_PATH = "Images/";
    public static final String IMG_BANNER_PATH = "Images/Banner/";
    public static final String IMG_THUMBNAIL_PATH = "Images/.thumbnails/";
    public static final String VIDEO_CACHE_DIR = "video-cache";

    public StorageUtils(Context context) {
        this.context = context;
        appDirectory = "/" + context.getString(R.string.app_name);
    }

    public static StorageUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new StorageUtils(context);
        }
        return mInstance;
    }
    public String getPath(String from) {
        String path = "";
        switch (from) {
            case "sent":
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent";
                break;
            case "received":
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images";
                break;
            case "profile":
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Profile";
                break;
            case "TAG_THUMBNAIL":
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails/";
                break;
            default:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images";
                break;
        }

        return path;
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void makeNoMedia(@NonNull final String dir) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();

        contentValues.put(MediaStore.MediaColumns.TITLE, MediaStore.MEDIA_IGNORE_FILENAME);
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MEDIA_IGNORE_FILENAME);
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, dir);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");

        final Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

        final Uri target = resolver.insert(collection, contentValues);

        try (ParcelFileDescriptor pfd = new ParcelFileDescriptor(resolver.openFileDescriptor(target, "w"));
             FileOutputStream outputStream = new FileOutputStream(pfd.getFileDescriptor())) {
            outputStream.flush();
            //   Timber.d("Gallery: .nomedia created at " + dir);
            Log.d(TAG, "makeNoMedia: "+dir);
        } catch (IOException e) {
            // Timber.e(e);
            Log.d(TAG, "makeNoMedia: "+e.toString());

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private  boolean containsNoMedia(@NonNull final String dir) {
        ContentResolver resolver = context.getContentResolver();
        final String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=? AND " +
                MediaStore.MediaColumns.TITLE + "=?";
        final String[] selectionArgs = new String[]{dir, MediaStore.MEDIA_IGNORE_FILENAME};
        final Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        try (Cursor c = resolver.query(collection,
                null, selection, selectionArgs,     null)) {
            if (c.moveToFirst()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            //   Timber.e(e);
            return false;
        }
    }

    public File getTempAudioCacheDir() {
        File[] mDataDir = ContextCompat.getExternalCacheDirs(context);
        File cacheDir = null;
        if (mDataDir.length > 0) {
            if (!mDataDir[0].exists()) {
                mDataDir[0].mkdirs();
            }
            cacheDir = new File(mDataDir[0] + File.separator + "TempAudio");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
        }

        if (cacheDir != null && cacheDir.exists()) return cacheDir;
        else return null;
    }



    public Uri saveToSDCard(Bitmap bitmap, String from, String fileName) {
        fileName = getFileName(fileName);

        String path = getPath(from);

        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        Uri outUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.TITLE, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + path);
            contentValues.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
            contentValues.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis());
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q)
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);
/*

            if (Objects.equals(from, Constants.TAG_SENT) && !containsNoMedia(Environment.DIRECTORY_PICTURES + path)) {
                makeNoMedia(Environment.DIRECTORY_PICTURES + path);
            }
*/

            Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

            outUri = resolver.insert(collection, contentValues);

            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                InputStream inputStream = new ByteArrayInputStream(stream.toByteArray());

                ParcelFileDescriptor pfd = resolver.openFileDescriptor(outUri, "w");
                boolean saved = FileUtil.copyFile(inputStream, new FileOutputStream(pfd.getFileDescriptor()));
                Log.d("Save: %s", outUri.getPath());
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                Log.d("Save: failed %s", e.getMessage());
            }

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                contentValues.clear();
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                resolver.update(outUri, contentValues, null, null);
            }
            return outUri;
        } else {
            // for API 28 and below (in progress)
           /* final String directory = Environment.getExternalStorageDirectory().getAbsolutePath();
            File targetDir = new File(directory);

            File folder = new File(targetDir, path);
            if (from.equals(Constants.TAG_SENT)) {
                if (!folder.exists()) {
                    if (folder.mkdirs()) {
                        Timber.i("Directory created: %s", targetDir.getAbsolutePath());
                    }
                    File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
                    try {
                        if (!noMediaFile.exists()) {
                            noMediaFile.createNewFile();
                        }
                    } catch (IOException e) {
                        Logging.e(TAG, "saveToSDCard: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                folder.mkdirs();
            }

            File createdMedia = new File(folder.getAbsoluteFile(), fileName);
            if (createdMedia.exists()) createdMedia.delete();*/

            File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            File folder = new File(sdcard.getAbsoluteFile(), path);
            if (from.equals(Constants.TAG_SENT)) {
                if (!folder.exists()) folder.mkdirs();
                File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
                try {
                    if (!noMediaFile.exists()) {
                        noMediaFile.createNewFile();
                    }
                } catch (IOException e) {
                    //   Logging.e(TAG, "saveToSDCard: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                folder.mkdirs();
            }

            File file = new File(folder.getAbsoluteFile(), fileName);
            if (file.exists())
                file.delete();

            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
                refreshGallery(file);
                return createImageUri(from, file.getAbsolutePath());
            } catch (Exception e) {
                //     Logging.e(TAG, "saveToSDCard: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    public Uri getImageUri(String from, String fileName) {
        fileName = getFileName(fileName);
        Uri imageUri = null;
        String path = getPath(from);

        ContentResolver resolver = context.getContentResolver();

        Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        final String selection;
        final String[] selectionArgs;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = MediaStore.MediaColumns.DISPLAY_NAME + "=? AND " +
                    MediaStore.MediaColumns.RELATIVE_PATH + "=?";
            selectionArgs = new String[]{fileName, Environment.DIRECTORY_PICTURES + path};
        } else {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + path;
            selection = MediaStore.MediaColumns.DATA + "=?";
            selectionArgs = new String[]{path + fileName};
        }
        try (Cursor c = resolver.query(collection, null, selection, selectionArgs, null)) {
            if (c.moveToFirst()) {
                imageUri = ContentUris.withAppendedId(collection, c.getLong(c.getColumnIndex(MediaStore.MediaColumns._ID)));
                //  Timber.d("Check file: exists %s %s", fileName, imageUri);
            } else {
                //   Timber.d("Check file: not found %s", (Object) selectionArgs);
            }
        } catch (Exception e) {
            //  Timber.e(e);
        }
        return imageUri;
    }

    private String getFileName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1);
        }
        return imgSplit;
    }

    public Uri createImageUri(String from, String filePath) {
        Uri imageUri = getImageUri(from, getFileName(filePath));
        if (imageUri == null) {
            // create a new uri
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            } else {
                ContentResolver resolver = context.getContentResolver();
                ContentValues values = new ContentValues();

                final String fileName = getFileName(filePath);
                final String path = getPath(from);
                Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                values.put(MediaStore.MediaColumns.TITLE, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
                values.put(MediaStore.MediaColumns.DATA, filePath);

                imageUri = resolver.insert(collection, values);
            }
        }
        return imageUri;
    }


/*
    public File saveToSDCard(Bitmap bitmap, String from, String fileName) {
        File sdcard = Environment.getExternalStorageDirectory();

        String path = "";
        switch (from) {
            case "sent":
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent";
                break;
            case "received":
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images";
                break;
            case "profile":
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Profile";
                break;
            case "TAG_THUMBNAIL":
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails/";
                break;
            default:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images";
                break;
        }

        File folder = new File(sdcard.getAbsoluteFile(), path);
        if (from.equals("sent")) {
            if (!folder.exists()) folder.mkdirs();
            File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
            try {
                if (!noMediaFile.exists()) {
                    noMediaFile.createNewFile();
                }
            } catch (IOException e) {
                //Logging.e(TAG, "saveToSDCard: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            folder.mkdirs();
        }

        File file = new File(folder.getAbsoluteFile(), fileName);
        if (file.exists())
            file.delete();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            refreshGallery(file);
            return file;
        } catch (Exception e) {
          //  Logging.e(TAG, "saveToSDCard: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
*/

    public void refreshGallery(File file) {

        try {
            final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            final Uri contentUri = Uri.fromFile(file);
            scanIntent.setData(contentUri);
            context.sendBroadcast(scanIntent);
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Logging.i(TAG, "Finished scanning " + file.getAbsolutePath(),"");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String saveThumbNail(Bitmap bitmap, String filename) {
        String stored = "";

        String path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails";
        File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File folder = new File(sdcard.getAbsoluteFile(), path);
        folder.mkdirs();
        Log.d(TAG, "saveThumbNailFolder: "+folder.getAbsolutePath());

        File nomediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
        Log.d(TAG, "saveThumbNailFile: "+nomediaFile);
        try {
            if (!nomediaFile.exists()) {
                nomediaFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File(folder.getAbsoluteFile(), filename);
        Log.d(TAG, "saveThumbNail: "+file);
        if (file.exists())
            return file.getPath();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            stored = "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getPath();
    }


    public boolean checkIfImageExists(String from, String imageName) {
        File file = getImage(from, imageName);
        return file != null && file.exists();
    }

    // url = file path or whatever suitable URL you want.
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public File getImage(String from, String imageName) {
        File mediaImage = null;
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            String path = "";
            switch (from) {
                case "sent":
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent/";
                    break;
                case "profile":
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Profile/";
                    break;
                case "TAG_THUMBNAIL":
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails/";
                    break;
                default:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/";
                    break;
            }
            mediaImage = new File(myDir.getPath() + path + imageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaImage;
    }

    public File getFileSavePath(String from) {
        return new File(getRootDir(context) + getChildDir(from));
    }

    public File getRootDir(Context context) {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalFilesDirs(context, null)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getFilesDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
//        Log.i(TAG, "getAppDataDir: " + mDataDir);
        return new File(mDataDir.getAbsolutePath());
    }

    public String getChildDir(String from) {
        String path;
        switch (from) {
            case "sent":
                path = appDirectory + IMG_SENT_PATH;
                break;
            case "profile":
                path = appDirectory + IMG_PROFILE_PATH;
                break;
            case "banner":
                path = appDirectory + IMG_BANNER_PATH;
                break;
            default:
                path = appDirectory + IMG_HOME_PATH;
                break;
        }
        return path;
    }

    public File getTempFile(Context context, String fileName) {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(context)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getCacheDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
        Logging.i(TAG, "getTempFile: " + mDataDir,"");
        return new File(mDataDir.getPath() + File.separator + fileName);
    }

    public File saveToAppDir(Bitmap bitmap, String from, String filename) {
        boolean stored = false;
        File sdcard = getRootDir(context);
        String path = getChildDir(from);

        File folder = new File(sdcard.getAbsoluteFile(), path);
        if (!folder.exists())
            folder.mkdir();

        File file = new File(folder.getAbsoluteFile(), filename);
        if (file.exists())
            stored = true;

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            stored = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stored ? file : null;
    }

    public File saveToCacheDir(Bitmap bitmap, String filename) {
        boolean stored = false;
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(context)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getCacheDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }

        File file = new File(mDataDir.getAbsoluteFile(), filename);
        if (file.exists())
            stored = true;

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            stored = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stored ? file : null;
    }

    public void deleteCacheDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            for (File externalCacheDir : ContextCompat.getExternalCacheDirs(context)) {
                if (externalCacheDir.listFiles() != null) {
                    for (File file : externalCacheDir.listFiles()) {
                        if (!file.getName().equals(VIDEO_CACHE_DIR)) {
                            file.delete();
                        }
                    }
                }
            }
        } else {
            File mDataDir = null;
            mDataDir = context.getCacheDir();
            if (mDataDir.exists() && mDataDir.listFiles() != null)
                for (File file : mDataDir.listFiles()) {
                    if (!file.getName().equals(VIDEO_CACHE_DIR)) {
                        file.delete();
                    }
                }
        }
    }

    public void clearVideoCache() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            for (File externalCacheDir : ContextCompat.getExternalCacheDirs(context)) {
                if (externalCacheDir.listFiles() != null) {
                    for (File file : externalCacheDir.listFiles()) {
                        if (file.getName().equals(VIDEO_CACHE_DIR) && file.isDirectory()) {
                            for (File listFile : file.listFiles()) {
                                listFile.delete();
                            }
                            break;
                        }
                    }
                }
            }
        } else {
            File mDataDir = null;
            mDataDir = context.getCacheDir();
            if (mDataDir.exists() && mDataDir.listFiles() != null)
                for (File file : mDataDir.listFiles()) {
                    if (file.getName().equals(VIDEO_CACHE_DIR) && file.isDirectory()) {
                        for (File listFile : file.listFiles()) {
                            listFile.delete();
                        }
                        break;
                    }
                }
        }
    }
   /* private static final String TAG = StorageUtils.class.getSimpleName();
    private static StorageUtils mInstance;
    private final Context context;
    private String appDirectory;
    public static final String IMG_SENT_PATH = "Images/Sent/";
    public static final String IMG_PROFILE_PATH = "Images/Profile/";
    public static final String IMG_HOME_PATH = "Images/";
    public static final String IMG_BANNER_PATH = "Images/Banner/";
    public static final String IMG_THUMBNAIL_PATH = "Images/.thumbnails/";
    public static final String VIDEO_CACHE_DIR = "video-cache";

    public StorageUtils(Context context) {
        this.context = context;
        appDirectory = "/" + context.getString(R.string.app_name);
    }

    public static StorageUtils getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new StorageUtils(context);
        }
        return mInstance;
    }
    public String getPath(String from) {
        String path = "";
        switch (from) {
                case "sent":
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent";
                    break;
                case "received":
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images";
                    break;
                case "profile":
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Profile";
                    break;
                case "TAG_THUMBNAIL":
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails/";
                    break;
                default:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images";
                    break;
            }

        return path;
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void makeNoMedia(@NonNull final String dir) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();

        contentValues.put(MediaStore.MediaColumns.TITLE, MediaStore.MEDIA_IGNORE_FILENAME);
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MEDIA_IGNORE_FILENAME);
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, dir);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");

        final Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

        final Uri target = resolver.insert(collection, contentValues);

        try (ParcelFileDescriptor pfd = new ParcelFileDescriptor(resolver.openFileDescriptor(target, "w"));
             FileOutputStream outputStream = new FileOutputStream(pfd.getFileDescriptor())) {
            outputStream.flush();
            //   Timber.d("Gallery: .nomedia created at " + dir);
            Log.d(TAG, "makeNoMedia: "+dir);
        } catch (IOException e) {
            // Timber.e(e);
            Log.d(TAG, "makeNoMedia: "+e.toString());

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private  boolean containsNoMedia(@NonNull final String dir) {
        ContentResolver resolver = context.getContentResolver();
        final String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=? AND " +
                MediaStore.MediaColumns.TITLE + "=?";
        final String[] selectionArgs = new String[]{dir, MediaStore.MEDIA_IGNORE_FILENAME};
        final Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        try (Cursor c = resolver.query(collection,
                null, selection, selectionArgs,     null)) {
            if (c.moveToFirst()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            //   Timber.e(e);
            return false;
        }
    }


    public Uri saveToSDCard(Bitmap bitmap, String from, String fileName) {
        fileName = getFileName(fileName);

        String path = getPath(from);

        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        Uri outUri;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.TITLE, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + path);
            contentValues.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
            contentValues.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis());
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q)
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);

            if (Objects.equals(from, Constants.TAG_SENT) && !containsNoMedia(Environment.DIRECTORY_PICTURES + path)) {
                makeNoMedia(Environment.DIRECTORY_PICTURES + path);
            }

            Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);

            outUri = resolver.insert(collection, contentValues);

            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                InputStream inputStream = new ByteArrayInputStream(stream.toByteArray());

                ParcelFileDescriptor pfd = resolver.openFileDescriptor(outUri, "w");
                boolean saved = FileUtil.copyFile(inputStream, new FileOutputStream(pfd.getFileDescriptor()));
                Log.d("Save: %s", outUri.getPath());
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                Log.d("Save: failed %s", e.getMessage());
            }

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                contentValues.clear();
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                resolver.update(outUri, contentValues, null, null);
            }
            return outUri;
        } else {
            // for API 28 and below (in progress)
           *//* final String directory = Environment.getExternalStorageDirectory().getAbsolutePath();
            File targetDir = new File(directory);

            File folder = new File(targetDir, path);
            if (from.equals(Constants.TAG_SENT)) {
                if (!folder.exists()) {
                    if (folder.mkdirs()) {
                        Timber.i("Directory created: %s", targetDir.getAbsolutePath());
                    }
                    File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
                    try {
                        if (!noMediaFile.exists()) {
                            noMediaFile.createNewFile();
                        }
                    } catch (IOException e) {
                        Logging.e(TAG, "saveToSDCard: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                folder.mkdirs();
            }

            File createdMedia = new File(folder.getAbsoluteFile(), fileName);
            if (createdMedia.exists()) createdMedia.delete();*//*

            File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            File folder = new File(sdcard.getAbsoluteFile(), path);
            if (from.equals(Constants.TAG_SENT)) {
                if (!folder.exists()) folder.mkdirs();
                File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
                try {
                    if (!noMediaFile.exists()) {
                        noMediaFile.createNewFile();
                    }
                } catch (IOException e) {
                    //   Logging.e(TAG, "saveToSDCard: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                folder.mkdirs();
            }

            File file = new File(folder.getAbsoluteFile(), fileName);
            if (file.exists())
                file.delete();

            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
                refreshGallery(file);
                return createImageUri(from, file.getAbsolutePath());
            } catch (Exception e) {
                //     Logging.e(TAG, "saveToSDCard: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    public Uri getImageUri(String from, String fileName) {
        fileName = getFileName(fileName);
        Uri imageUri = null;
        String path = getPath(from);

        ContentResolver resolver = context.getContentResolver();

        Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        final String selection;
        final String[] selectionArgs;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = MediaStore.MediaColumns.DISPLAY_NAME + "=? AND " +
                    MediaStore.MediaColumns.RELATIVE_PATH + "=?";
            selectionArgs = new String[]{fileName, Environment.DIRECTORY_PICTURES + path};
        } else {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + path;
            selection = MediaStore.MediaColumns.DATA + "=?";
            selectionArgs = new String[]{path + fileName};
        }
        try (Cursor c = resolver.query(collection, null, selection, selectionArgs, null)) {
            if (c.moveToFirst()) {
                imageUri = ContentUris.withAppendedId(collection, c.getLong(c.getColumnIndex(MediaStore.MediaColumns._ID)));
                //  Timber.d("Check file: exists %s %s", fileName, imageUri);
            } else {
                //   Timber.d("Check file: not found %s", (Object) selectionArgs);
            }
        } catch (Exception e) {
            //  Timber.e(e);
        }
        return imageUri;
    }

    private String getFileName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1);
        }
        return imgSplit;
    }

    public Uri createImageUri(String from, String filePath) {
        Uri imageUri = getImageUri(from, getFileName(filePath));
        if (imageUri == null) {
            // create a new uri
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            } else {
                ContentResolver resolver = context.getContentResolver();
                ContentValues values = new ContentValues();

                final String fileName = getFileName(filePath);
                final String path = getPath(from);
                Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                values.put(MediaStore.MediaColumns.TITLE, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
                values.put(MediaStore.MediaColumns.DATA, filePath);

                imageUri = resolver.insert(collection, values);
            }
        }
        return imageUri;
    }


*//*
    public File saveToSDCard(Bitmap bitmap, String from, String fileName) {
        File sdcard = Environment.getExternalStorageDirectory();

        String path = "";
        switch (from) {
            case "sent":
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent";
                break;
            case "received":
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images";
                break;
            case "profile":
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Profile";
                break;
            case "TAG_THUMBNAIL":
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails/";
                break;
            default:
                path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images";
                break;
        }

        File folder = new File(sdcard.getAbsoluteFile(), path);
        if (from.equals("sent")) {
            if (!folder.exists()) folder.mkdirs();
            File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
            try {
                if (!noMediaFile.exists()) {
                    noMediaFile.createNewFile();
                }
            } catch (IOException e) {
                //Logging.e(TAG, "saveToSDCard: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            folder.mkdirs();
        }

        File file = new File(folder.getAbsoluteFile(), fileName);
        if (file.exists())
            file.delete();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            refreshGallery(file);
            return file;
        } catch (Exception e) {
          //  Logging.e(TAG, "saveToSDCard: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
*//*

    public void refreshGallery(File file) {

        try {
            final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            final Uri contentUri = Uri.fromFile(file);
            scanIntent.setData(contentUri);
            context.sendBroadcast(scanIntent);
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Logging.i(TAG, "Finished scanning " + file.getAbsolutePath());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String saveThumbNail(Bitmap bitmap, String filename) {
        String stored = "";

        String path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails";
        File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File folder = new File(sdcard.getAbsoluteFile(), path);
        folder.mkdirs();

        File nomediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
        try {
            if (!nomediaFile.exists()) {
                nomediaFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File(folder.getAbsoluteFile(), filename);
        if (file.exists())
            return file.getPath();

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            stored = "success";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getPath();
    }


    public boolean checkIfImageExists(String from, String imageName) {
        File file = getImage(from, imageName);
        return file != null && file.exists();
    }

    // url = file path or whatever suitable URL you want.
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public File getImage(String from, String imageName) {
        File mediaImage = null;
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            String path = "";
            switch (from) {
                case "sent":
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Sent/";
                    break;
                case "profile":
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/Profile/";
                    break;
                case "TAG_THUMBNAIL":
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/.thumbnails/";
                    break;
                default:
                    path = "/" + context.getString(R.string.app_name) + "/" + context.getString(R.string.app_name) + "Images/";
                    break;
            }
            mediaImage = new File(myDir.getPath() + path + imageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaImage;
    }

    public File getFileSavePath(String from) {
        return new File(getRootDir(context) + getChildDir(from));
    }

    public File getRootDir(Context context) {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalFilesDirs(context, null)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getFilesDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
//        Log.i(TAG, "getAppDataDir: " + mDataDir);
        return new File(mDataDir.getAbsolutePath());
    }

    public String getChildDir(String from) {
        String path;
        switch (from) {
            case "sent":
                path = appDirectory + IMG_SENT_PATH;
                break;
            case "profile":
                path = appDirectory + IMG_PROFILE_PATH;
                break;
            case "banner":
                path = appDirectory + IMG_BANNER_PATH;
                break;
            default:
                path = appDirectory + IMG_HOME_PATH;
                break;
        }
        return path;
    }

    public File getTempFile(Context context, String fileName) {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(context)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getCacheDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }
        Logging.i(TAG, "getTempFile: " + mDataDir);
        return new File(mDataDir.getPath() + File.separator + fileName);
    }

    public File saveToAppDir(Bitmap bitmap, String from, String filename) {
        boolean stored = false;
        File sdcard = getRootDir(context);
        String path = getChildDir(from);

        File folder = new File(sdcard.getAbsoluteFile(), path);
        if (!folder.exists())
            folder.mkdir();

        File file = new File(folder.getAbsoluteFile(), filename);
        if (file.exists())
            stored = true;

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            stored = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stored ? file : null;
    }

    public File saveToCacheDir(Bitmap bitmap, String filename) {
        boolean stored = false;
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(context)[0];
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        } else {
            mDataDir = context.getCacheDir();
            if (!mDataDir.exists()) {
                mDataDir.mkdirs();
            }
        }

        File file = new File(mDataDir.getAbsoluteFile(), filename);
        if (file.exists())
            stored = true;

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            stored = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stored ? file : null;
    }

    public void deleteCacheDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            for (File externalCacheDir : ContextCompat.getExternalCacheDirs(context)) {
                if (externalCacheDir.listFiles() != null) {
                    for (File file : externalCacheDir.listFiles()) {
                        if (!file.getName().equals(VIDEO_CACHE_DIR)) {
                            file.delete();
                        }
                    }
                }
            }
        } else {
            File mDataDir = null;
            mDataDir = context.getCacheDir();
            if (mDataDir.exists() && mDataDir.listFiles() != null)
                for (File file : mDataDir.listFiles()) {
                    if (!file.getName().equals(VIDEO_CACHE_DIR)) {
                        file.delete();
                    }
                }
        }
    }

    public void clearVideoCache() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            for (File externalCacheDir : ContextCompat.getExternalCacheDirs(context)) {
                if (externalCacheDir.listFiles() != null) {
                    for (File file : externalCacheDir.listFiles()) {
                        if (file.getName().equals(VIDEO_CACHE_DIR) && file.isDirectory()) {
                            for (File listFile : file.listFiles()) {
                                listFile.delete();
                            }
                            break;
                        }
                    }
                }
            }
        } else {
            File mDataDir = null;
            mDataDir = context.getCacheDir();
            if (mDataDir.exists() && mDataDir.listFiles() != null)
                for (File file : mDataDir.listFiles()) {
                    if (file.getName().equals(VIDEO_CACHE_DIR) && file.isDirectory()) {
                        for (File listFile : file.listFiles()) {
                            listFile.delete();
                        }
                        break;
                    }
                }
        }
    }*/
}
