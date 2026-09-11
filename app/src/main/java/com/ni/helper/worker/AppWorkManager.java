package com.ni.helper.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class AppWorkManager extends Worker {
    private static final String TAG = AppWorkManager.class.getSimpleName();
    private Context mContext;

    public AppWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: WakeupDevice");
        return Result.success();
    }
}
