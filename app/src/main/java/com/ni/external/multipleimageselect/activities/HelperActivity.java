package com.ni.external.multipleimageselect.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.ni.external.multipleimageselect.helpers.Constants;
import com.ni.SnatchItUp.BaseActivity;
import com.ni.SnatchItUp.R;

public class HelperActivity extends BaseActivity {
    protected View view;

    private final int maxLines = 4;
    private final String[] permissions = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private final String[] storagereadmediapermission = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE };
    protected void checkPermission() {
        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                permissionGranted();
            } else {
                ActivityCompat.requestPermissions(this, storagereadmediapermission, Constants.PERMISSION_REQUEST_CODE);
            }
        }
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                permissionGranted();
            } else {
                ActivityCompat.requestPermissions(this, permissions, Constants.PERMISSION_REQUEST_CODE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                permissionGranted();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Constants.PERMISSION_REQUEST_CODE);
            }
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    openManageStoragePermissionAccess();
                } else {
                    permissionGranted();
                }
            }
        }
    }

    private void openManageStoragePermissionAccess(){
        View contentView = getLayoutInflater().inflate(R.layout.bottom_sheet_scoped_storage_permission, findViewById(R.id.parentLay), false);
        BottomSheetDialog pickerOptionsSheet = new BottomSheetDialog(this, R.style.SimpleBottomDialog);
        pickerOptionsSheet.setCanceledOnTouchOutside(false);
        pickerOptionsSheet.setContentView(contentView);
        TextView des = contentView.findViewById(R.id.bottom_sheet_des);
        View layoutCancel = contentView.findViewById(R.id.layoutCancel);
        View layoutAllow = contentView.findViewById(R.id.layoutAllow);

        des.setText("The "+ getString(R.string.app_name)+" "+ getString(R.string.storage_permission_des));
        layoutCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pickerOptionsSheet.isShowing()) {
                    pickerOptionsSheet.dismiss();
                    finish();
                }

            }
        });
        layoutAllow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pickerOptionsSheet.isShowing()){
                    pickerOptionsSheet.dismiss();
                }
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                    startActivityForResult(intent, 120);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, 102);
                }
            }
        });
        pickerOptionsSheet.show();
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showRequestPermissionRationale();

        } else {
            showAppPermissionSettings();
        }
    }

    private void showRequestPermissionRationale() {
        Snackbar snackbar = Snackbar.make(
                view,
                getString(R.string.permission_info),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.permission_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(
                                HelperActivity.this,
                                permissions,
                                Constants.PERMISSION_REQUEST_CODE);
                    }
                });

        /*((TextView) snackbar.getView()
                .findViewById(android.support.design.R.id.snackbar_text)).setMaxLines(maxLines);*/
        snackbar.show();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }

    private void showAppPermissionSettings() {
        Snackbar snackbar = Snackbar.make(
                view,
                getString(R.string.permission_force),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.permission_settings), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.fromParts(
                                getString(R.string.permission_package),
                                HelperActivity.this.getPackageName(),
                                null);

                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.setData(uri);
                        startActivityForResult(intent, Constants.PERMISSION_REQUEST_CODE);
                    }
                });

        /*((TextView) snackbar.getView()
                .findViewById(android.support.design.R.id.snackbar_text)).setMaxLines(maxLines);*/
        snackbar.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != Constants.PERMISSION_REQUEST_CODE
                || grantResults.length == 0
                || grantResults[0] == PackageManager.PERMISSION_DENIED) {
            permissionDenied();

        } else {
            permissionGranted();
        }
    }

    protected void permissionGranted() {}

    private void permissionDenied() {
        hideViews();
        requestPermission();
    }

    protected void hideViews() {}

    protected void setView(View view) {
        this.view = view;
    }
}
