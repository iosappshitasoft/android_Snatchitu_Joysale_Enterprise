package com.ni.helper;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.ni.helper.callback.OkayCancelCallback;
import com.ni.SnatchItUp.R;


public class PermissionsUtils {

    private static final String TAG = PermissionsUtils.class.getSimpleName();
    public static final int CALL_PERMISSION_REQUEST_CODE = 101;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 106;
    public static final int CONTACT_PERMISSION_REQUEST_CODE = 110;
    public static final int RECORD_PERMISSION_REQUEST_CODE = 111;

    public static final String[] HOMEPAGE_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    public static final String[] HOMEPAGE_V11_PERMISSION = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    public static final String[] HOMEPAGE_V10_PERMISSION = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    public static final String[] CAMERA_STORAGE_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    public static final String[] CAMERA_PERMISSION = {
            Manifest.permission.CAMERA
    };

    public static final String[] LOCATION_PERMISSION = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    public static final String[] CALL_PERMISSION = {
            Manifest.permission.CALL_PHONE
    };

    public static final String[] GETLOCATION_PERMISSION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public static final String[] AUDIO_RECORD_PERMISSION = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
public static final String[] LOCATIONS = {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION};
public static final String[] LOCATIONS_ACCESS_COARSE_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION};
public static final String[] LOCATIONS_ACCESS_FINE_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION};
    public static final String[] READ_MEDIA_IMAGESPERMISSION = {
            Manifest.permission.READ_MEDIA_IMAGES
    };
    public static final String[] READ_MEDIA_IMAGESPERMISSION14 = {
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,Manifest.permission.READ_MEDIA_IMAGES
    };

    public static final String[] NOTIFICATION_PERMISSION = {
            Manifest.permission.POST_NOTIFICATIONS/*,Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION*/
    };
    public static final String[] READ_PHONE_STATEPERMISSION = {
            Manifest.permission.READ_PHONE_STATE
    };

    public static final String[] AUDIO_RECORD_V11_PERMISSION = {
            Manifest.permission.RECORD_AUDIO
    };

    public static final String[] READ_STORAGE_PERMISSION = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static final String[] WRITE_STORAGE_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static final String[] READ_WRITE_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public static final String[] CHAT_READ_MEDIA_IMAGE = {
            Manifest.permission.CAMERA,Manifest.permission.READ_MEDIA_IMAGES
    };

    //
    public static boolean checkPermissionsArray(String[] permissions, Context mContext) {
        for (int i = 0; i < permissions.length; i++) {
            String check = permissions[i];
            if (!checkPermissions(check, mContext)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check a single permission is it has been verified
     *
     * @param permission
     * @return
     */
    public static boolean checkPermissions(String permission, Context mContext) {
        Log.d(TAG, "checkPermissions: " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(mContext, permission);
        if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "checkPermissions: \n Permission was not granted for: " + permission);
            return false;
        } else {
            Log.d(TAG, "checkPermissions: \n Permission was granted for: " + permission);
            return true;
        }
    }

    public static boolean checkStoragePermission(Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            boolean granted = false;
            for (String permission : PermissionsUtils.READ_WRITE_PERMISSIONS) {
                int permissionRequest = ActivityCompat.checkSelfPermission(mContext, permission);
                if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                } else {
                    granted = true;
                }
            }
            return granted;
        }
    }

    public static boolean checkCameraPermission(Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else {
            boolean granted = false;
            for (String permission : PermissionsUtils.CAMERA_STORAGE_PERMISSION) {
                int permissionRequest = ActivityCompat.checkSelfPermission(mContext, permission);
                if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                } else {
                    granted = true;
                }
            }
            return granted;
        }
    }

    public static boolean checkRecordAudioPermission(Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            boolean granted = false;
            for (String permission : PermissionsUtils.AUDIO_RECORD_PERMISSION) {
                int permissionRequest = ActivityCompat.checkSelfPermission(mContext, permission);
                if (permissionRequest != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                } else {
                    granted = true;
                }
            }
            return granted;
        }
    }

    public static void openStorageDialogV11(Context mContext, String description, OkayCancelCallback callback) {
        final Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_popup);
        dialog.getWindow().setLayout(mContext.getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView title = dialog.findViewById(R.id.title);
        TextView yes = dialog.findViewById(R.id.yes);
        TextView no = dialog.findViewById(R.id.no);
        yes.setText(mContext.getString(R.string.allow));
        no.setText(mContext.getString(R.string.nope));
        title.setText(description);
        no.setVisibility(View.VISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                callback.onOkayClicked("");
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                callback.onCancelClicked("");
            }
        });

        dialog.show();
    }

    public static void openPermissionDialog(Context mContext, OkayCancelCallback callback, String description) {
        new AlertDialog.Builder(mContext)
                .setMessage(description)
                .setPositiveButton(mContext.getString(R.string.okay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onOkayClicked("");
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onCancelClicked("");
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }
}
