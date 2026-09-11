package com.ni.helper;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.ni.SnatchItUp.BuildConfig;
import com.ni.SnatchItUp.R;
import com.ni.utils.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;

/**
 * Created by hitasoft on 7/3/17.
 */

public class ImageStorage {
    private static final String TAG = ImageStorage.class.getSimpleName();
    private Context mContext;
    private String appDirectory;
    public static final String IMG_SENT_PATH = "Images/Sent/";
    public static final String IMG_PROFILE_PATH = "Images/Profile/";
    public static final String IMG_HOME_PATH = "Images/";
    public static final String IMG_BANNER_PATH = "Images/Banner/";
    public static final String IMG_THUMBNAIL_PATH = "Images/.thumbnails/";

    public ImageStorage(Context context) {
        this.mContext = context;
        appDirectory = "/" + context.getString(R.string.app_name);
    }

    public String saveToAppDir(Bitmap bitmap, String from, String filename, String timeStamp) {
        String stored = "";
        File sdcard = getRootDir(mContext);
        String path = getChildDir(from);

        File folder = new File(sdcard.getAbsoluteFile(), path);
        switch (from) {
            case "sent":
            case "profile":
            case "banner":
                if (!folder.exists())
                    folder.mkdirs();
                break;
            default:
                if (!folder.exists())
                    folder.mkdir();
                break;
        }

        File file = new File(folder.getAbsoluteFile(), filename);
        if (file.exists())
            return "success";

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            stored = "success";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stored;
    }

    public boolean saveToGallery(Bitmap bitmap, @NonNull String name) throws IOException {
        boolean saved;
        OutputStream fos;
        File image = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = mContext.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.TITLE, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES +
                    File.separator + mContext.getString(R.string.app_name) + File.separator);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(imageUri);
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES).toString() + File.separator + mContext.getString(R.string.app_name);
            File file = new File(imagesDir);

            if (!file.exists()) {
                file.mkdir();
            }
            image = new File(imagesDir, name);
            fos = new FileOutputStream(image);
        }
        saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
        if (saved && image != null) {
            refreshGallery(image);
        }
        return saved;
    }

    public void refreshGallery(File file) {

        try {
            final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            final Uri contentUri = Uri.fromFile(file);
            scanIntent.setData(contentUri);
            mContext.sendBroadcast(scanIntent);
            MediaScannerConnection.scanFile(mContext, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    Log.e(TAG, "Finished scanning " + file.getAbsolutePath());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String saveImageInStorage(Bitmap bitmap, String from, String filename) {
        String stored = "";
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            ContentValues contentValues = getContentValues();
            ContentResolver resolver = mContext.getContentResolver();
            Uri uri = getPublicImageUri(contentValues, resolver);
            try {
                saveBitmap(bitmap, resolver.openOutputStream(uri));
                contentValues.put(MediaStore.Images.Media.IS_PENDING, false);
                mContext.getContentResolver().update(uri, contentValues, null, null);
                stored = getPath(mContext, uri);
            } catch (FileNotFoundException | URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            //File sdcard = Environment.getExternalStorageDirectory();
            File sdcard = mContext.getExternalCacheDir();
            String path = "";
            if (from.equals("sent")) {
                path = "/" + mContext.getString(R.string.app_name) + "Images/Sent";
            } else if (from.equals("profile")) {
                path = "/" + mContext.getString(R.string.app_name) + "Images/Profile";
            } else {
                path = "/" + mContext.getString(R.string.app_name) + "Images";
            }
            File folder = new File(sdcard.getAbsoluteFile(), path);
            if (from.equals("sent")) {
                folder.mkdirs();
                File nomediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
                try {
                    if (!nomediaFile.exists()) {
                        nomediaFile.createNewFile();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (from.equals("profile")) {
                if (!folder.exists())
                    folder.mkdirs();
            } else {
                if (!folder.exists())
                    folder.mkdir();
            }

            File file = new File(folder.getAbsoluteFile(), filename);
            if (file.exists())
                return folder.getAbsolutePath();

            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
                stored = file.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return stored;
    }

    public Uri getPublicImageUri(ContentValues values, ContentResolver resolver) {
        Uri uri = null;
        try {

            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values);

        } catch (Exception e) {
            Log.e("ExternalContentUri ", "-" + e.toString());
        }

        return uri;
    }

    public void saveBitmap(Bitmap bitmap, OutputStream outputStream) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public ContentValues getContentValues() {
        ContentValues valuesvideos;
        valuesvideos = new ContentValues();
        valuesvideos.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + mContext.getString(R.string.app_name) + "images");
        valuesvideos.put(MediaStore.Images.Media.TITLE, "TEMP_" + String.valueOf(System.currentTimeMillis()));
        valuesvideos.put(MediaStore.Images.Media.DISPLAY_NAME, "TEMP_" + String.valueOf(System.currentTimeMillis()));
        valuesvideos.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        valuesvideos.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        valuesvideos.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        valuesvideos.put(MediaStore.Images.Media.IS_PENDING, 1);
        return valuesvideos;
    }


    public File getFileSavePath(String from) {
        return new File(getRootDir(mContext) + getChildDir(from));
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

    public File getImage(String from, String imagename) {

        File mediaImage = null;
        try {
            File myDir = getRootDir(mContext);
            if (!myDir.exists())
                return null;
            String path = getChildDir(from);
            mediaImage = new File(myDir.getPath() + path + imagename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaImage;
    }

    public boolean checkIfImageExists(String from, String imageName) {
        File file = getImage(from, imageName);
        return file.exists();
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
        Log.i(TAG, "getTempFile: " + mDataDir);
        return new File(mDataDir.getPath() + File.separator + fileName);
    }
    public File getTempFileNew(Context context, String fileName) {
        File mDataDir;
// Use app-specific cache directory
        mDataDir = context.getCacheDir(); // This is app-specific and should be accessible
// Create the directory if it doesn't exist
        if (!mDataDir.exists()) {
            if (!mDataDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory: " + mDataDir.getAbsolutePath());
                return null; // Return null if directory creation fails
            }
        }
        File tempFile = new File(mDataDir, fileName);
        try {
            if (tempFile.createNewFile()) {
                Log.i(TAG, "Temp file created: " + tempFile.getAbsolutePath());
            } else {
                Log.i(TAG, "Temp file already exists: " + tempFile.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error creating temp file", e);
            return null; // Return null if file creation fails
        }
        return tempFile;
    }
    public void deleteCacheDir() {
        File mDataDir = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mDataDir = ContextCompat.getExternalCacheDirs(mContext)[0];
            if (mDataDir.exists() && mDataDir.listFiles() != null)
                for (File file : mDataDir.listFiles()) {
                    file.delete();
                }
        } else {
            mDataDir = mContext.getCacheDir();
            if (mDataDir.exists() && mDataDir.listFiles() != null)
                for (File file : mDataDir.listFiles()) {
                    file.delete();
                }
        }
    }

    /**
     * Method for Download Images from Given Url
     */

    public Bitmap downloadImage(String src) {
//        Log.v(TAG, "downloadImgUrl=" + src);
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
            return myBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @SuppressLint("NewApi")
    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{split[1]};
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    public File getExtCacheDir() {
        File mDataDir = ContextCompat.getExternalCacheDirs(mContext)[0];
        if (!mDataDir.exists()) {
            mDataDir.mkdirs();
        }
        if (mDataDir.exists()) return mDataDir;
        else return null;
    }

    public File getExtFilesDir() {
        File mDataDir = ContextCompat.getExternalFilesDirs(mContext, null)[0];
        if (!mDataDir.exists()) {
            mDataDir.mkdirs();
        }
        if (mDataDir.exists()) return mDataDir;
        else return null;
    }

    public File createDirectory(String from, String fileName) {
        File folder = new File(mContext.getExternalCacheDir() + getFolderPath(from));
        if (!folder.exists()) {
            folder.mkdirs();
        }


        File noMediaFile = new File(folder.getAbsoluteFile(), fileName);
        try {
            if (!noMediaFile.exists()) {
                noMediaFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return folder;
    }

    public String getPath(String from) {
        File sdcard = null;
        sdcard = mContext.getExternalCacheDir();
        File folder = new File(sdcard.getAbsoluteFile(), getFolderPath(from));
        Log.d(TAG, "getPath: " + folder.getAbsolutePath());
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File noMediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
        Log.d(TAG, "getPath: " + noMediaFile.getAbsolutePath());

        try {
            if (!noMediaFile.exists()) {
                noMediaFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return folder.getPath();
    }

    public String getFolderPaths(String from) {
        String path = "";
        switch (from) {
            case "sent":
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Sent/";
                break;
            case "profile":
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Profile/";
                break;
            case "received":
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/";
                break;
            case "audio_sent":
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Audio/Sent/";
                break;
            case "audio_receive":
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Audio/";
                break;
            default:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/";
                break;
        }
        return path;
    }

    public String getFolderPath(String from) {
        String path = "";
        switch (from) {
            case "sent":
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Sent/";
                break;
            case "profile":
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Profile/";
                break;
            case "received":
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/";
                break;
            case "audio_sent":
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Audio/Sent/";
                break;
            case "audio_receive":
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Audio/";
                break;
            default:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/";
                break;
        }
        return path;
    }

    public boolean checkIfFileExists(String from, String fileName) {
        File file = getImages(from, fileName);
        return file != null && file.exists();
    }

    public File getImages(String from, String imagename) {

        File mediaImage = null;
        try {
            String root = mContext.getExternalCacheDir().toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            mediaImage = new File(myDir.getPath() + getFolderPath(from) + imagename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaImage;
    }

    public File getFile(String from, String fileName) {
        File mediaImage = null;
        String root = getRootDirectory(from).toString();
        try {
            File myDir = new File(root);
            if (!myDir.exists())
                return null;
            String path = getDirectoryPath(from);

            mediaImage = new File(myDir.getPath() + path + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (mediaImage != null && mediaImage.exists()) ? mediaImage : null;
    }

    public File saveFileToSent(File srcFile, String from, String fileName) {
        File root = getRootDirectory(from);
        String path = getDirectoryPath(from);

        File folder = new File(root.getAbsoluteFile(), path);
        if (!folder.exists()) folder.mkdirs();

        File newFile = new File(folder.getAbsoluteFile(), fileName);
        if (newFile.exists())
            return newFile;

        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(srcFile).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
//            srcFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputChannel != null) inputChannel.close();
                if (outputChannel != null) outputChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return newFile.exists() ? newFile : null;
    }

    private File getRootDirectory(String from) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            File root;
            switch (from) {
                case Constants.TAG_SENT:
                case Constants.TAG_PROFILE:
                case Constants.TAG_THUMB:
                case Constants.TAG_RECEIVED:
                case Constants.TAG_RECEIVE:
                default:
                    root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    break;
                case Constants.TAG_AUDIO_SENT:
                case Constants.TAG_AUDIO_RECEIVE:
                case Constants.TAG_AUDIO:
                    root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                    break;
            }
            return root;
        } else {
            return getExtFilesDir();
        }
    }

    public String getDirectoryPath(String from) {
        String path = "";
        switch (from) {
            case Constants.TAG_SENT:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Sent/";
                break;
            case Constants.TAG_PROFILE:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/Profile/";
                break;
            case Constants.TAG_THUMB:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/.thumbnails/";
            case Constants.TAG_AUDIO_SENT:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Audio/Sent/";
                break;
            case Constants.TAG_AUDIO_RECEIVE:
            case Constants.TAG_AUDIO:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Audio/";
                break;
            case Constants.TAG_RECEIVED:
            default:
                path = "/" + mContext.getString(R.string.app_name) + "/" + mContext.getString(R.string.app_name) + "Images/";
                break;
        }
        return path;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Uri getAudioUri(String from, String fileName) {
        fileName = getFileName(fileName);
        Uri returnUri = null;
        String path = getDirectoryPath(from);
        Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                : MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // which Audio properties are we querying
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID
        };
        final String selection;
        final String[] selectionArgs;
        selection = MediaStore.MediaColumns.DISPLAY_NAME + "=? AND " +
                MediaStore.MediaColumns.RELATIVE_PATH + "=?";
        selectionArgs = new String[]{fileName, Environment.DIRECTORY_MUSIC + path};
        ContentResolver resolver = mContext.getContentResolver();
        Cursor audioCursor = resolver.query(collection, projection, selection, selectionArgs, null);
        int columnIndexID;
        if (audioCursor != null) {
            columnIndexID = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
            if (audioCursor.moveToFirst()) {
                long audioId = audioCursor.getLong(columnIndexID);
                returnUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioId);
            }
            audioCursor.close();
        }
        return checkValidUri(returnUri) ? returnUri : null;
    }

    private boolean checkValidUri(Uri contentUri) {
        try {
            InputStream inputStream =
                    mContext.getContentResolver().openInputStream(contentUri);
            inputStream.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "checkValidUri: " + e.getMessage());
        }
        return false;
    }

    public String getFileName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1);
        }
        return imgSplit;
    }

    public Uri getUriFromFile(File dir) {
        if (dir != null && dir.exists()) {
            return FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", dir);
        } else {
            return null;
        }
    }

    public String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public String getMimeTypeOfUri(Uri uri) {
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = mContext.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public int getMediaDuration(Context context, Uri fileUri) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context, fileUri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        try {
            int duration = Integer.parseInt(durationStr);
            return duration;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getMediaDuration(File srcFile) {
        int duration = 0;
        if (srcFile != null && srcFile.length() > 0) {
            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(srcFile.getAbsolutePath());
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                duration = Integer.parseInt(durationStr);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "getMediaDuration: " + duration);
        return duration;
    }

    public File saveToSDCardV9(File srcFile, String from, String filename, String fileType) {
        File sdcard = getRootDirectory(from);
        String path = getDirectoryPath(from);

        File folder = new File(sdcard.getAbsoluteFile(), path);
        if (from.equals("sent") || from.equals(Constants.TAG_AUDIO_SENT)) {
            folder.mkdirs();
            File nomediaFile = new File(folder.getAbsoluteFile(), ".nomedia");
            try {
                if (!nomediaFile.exists()) {
                    nomediaFile.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (from.equals("profile")) {
            folder.mkdirs();
        } else {
            folder.mkdirs();
        }

        File newFile = new File(folder.getAbsoluteFile(), filename);
        if (newFile.exists())
            return newFile;

        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(srcFile).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputChannel != null) inputChannel.close();
                if (outputChannel != null) outputChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return newFile.exists() ? newFile : null;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Uri saveToSDCardV10(File srcFile, String from, String fileName, String fileType, String mimeType) {
        Uri savedUri = null;
        Uri mediaContentUri = null;
        ContentValues values = null;
        switch (fileType) {
            case Constants.TAG_IMAGE:
                mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                values = getContentValues(mContext, from, fileName, mimeType);
                break;
            case Constants.TAG_AUDIO:
                mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                values = getContentValues(mContext, from, fileName, "audio/*");
                break;
            case Constants.TAG_VIDEO:
                mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                values = getContentValues(mContext, from, fileName, "video/mp4");
                break;
            case Constants.TAG_DOCUMENT:
                mediaContentUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
                values = getContentValues(mContext, from, fileName, getMimeType(srcFile.getAbsolutePath()));
                break;
        }
        savedUri = mContext.getContentResolver().insert(mediaContentUri, values);
        if (savedUri != null) {
            ParcelFileDescriptor pfd;
            try {
                pfd = mContext.getContentResolver().openFileDescriptor(savedUri, "w");
                FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());
                InputStream in = new FileInputStream(srcFile);
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
                pfd.close();
                switch (fileType) {
                    case Constants.TAG_IMAGE:
                        values.put(MediaStore.Images.Media.IS_PENDING, 0);
                        break;
                    case Constants.TAG_AUDIO:
                        values.put(MediaStore.Audio.Media.IS_PENDING, 0);
                        break;
                    case Constants.TAG_VIDEO:
                        values.put(MediaStore.Video.Media.IS_PENDING, 0);
                        break;
                    case Constants.TAG_DOCUMENT:
                        values.put(MediaStore.Files.FileColumns.IS_PENDING, 0);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mContext.getContentResolver().update(savedUri, values, null, null);
            Log.d(TAG, "saveToSDCardV10: " + savedUri);
        }
        return savedUri;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public ContentValues getContentValues(Context mContext, String from, String
            fileName, String mimeType) {
        String directory = "";
        ContentValues values = new ContentValues();
        switch (from) {
            case Constants.TAG_IMAGE: {
                directory = Environment.DIRECTORY_PICTURES + getDirectoryPath(from);
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.TITLE, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
                values.put(MediaStore.Images.Media.RELATIVE_PATH, directory);
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
            }
            break;
            case Constants.TAG_VIDEO: {
                directory = Environment.DIRECTORY_MOVIES + getDirectoryPath(from);
                values.put(MediaStore.Video.Media.TITLE, fileName);
                values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Video.Media.MIME_TYPE, mimeType);
                values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                values.put(MediaStore.Video.Media.RELATIVE_PATH, directory);
                values.put(MediaStore.Video.Media.IS_PENDING, 1);
            }
            break;
            case Constants.TAG_AUDIO_RECEIVE:
            case Constants.TAG_AUDIO: {
                directory = Environment.DIRECTORY_MUSIC + getDirectoryPath(from);
                values.put(MediaStore.Audio.Media.TITLE, fileName);
                values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Audio.Media.MIME_TYPE, mimeType);
                values.put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                values.put(MediaStore.Audio.Media.RELATIVE_PATH, directory);
                values.put(MediaStore.Audio.Media.IS_PENDING, 1);
            }
            break;
            case Constants.TAG_DOCUMENT: {
                directory = Environment.DIRECTORY_DOCUMENTS + getDirectoryPath(from);
                values.put(MediaStore.MediaColumns.TITLE, fileName);
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, directory);
                values.put(MediaStore.MediaColumns.IS_PENDING, 1);
            }
            break;
            default:
                directory = Environment.DIRECTORY_PICTURES + getDirectoryPath(from);
                values.put(MediaStore.Files.FileColumns.TITLE, fileName);
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
                values.put(MediaStore.Files.FileColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
                values.put(MediaStore.Images.Media.RELATIVE_PATH, directory);
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
                break;
        }
        return values;
    }
}
