package com.ni.helper.worker;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.CoroutineWorker;
import androidx.work.Data;
import androidx.work.WorkerParameters;

import com.ni.helper.LocaleManager;
import com.ni.helper.location.LocationHelper;
import com.ni.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kotlin.coroutines.Continuation;

public class GetLocationWorker extends CoroutineWorker {
    private static final String TAG = GetLocationWorker.class.getSimpleName();
    private Context mContext;
    private String mCountryCode, from;
    private double lat = 0, lng = 0;

    public GetLocationWorker(@NonNull Context appContext, @NonNull WorkerParameters params) {
        super(appContext, params);
        mContext = appContext;
    }

    @Nullable
    @Override
    public Result doWork(@NonNull Continuation<? super Result> continuation) {
        mCountryCode = getInputData().getString(Constants.TAG_COUNTRY_CODE);
        lat = getInputData().getDouble(Constants.TAG_LAT, 0);
        lng = getInputData().getDouble(Constants.TAG_LON, 0);
        from = getInputData().getString(Constants.TAG_FROM);
        String selectedLocation = getLocation(lat, lng);
        if (!TextUtils.isEmpty(selectedLocation)) {
            return Result.success(createOutputData(selectedLocation));
        } else {
            return Result.failure(Data.EMPTY);
        }
    }

    private String getLocation(double lat, double lng) {
        List<Address> resultList = new ArrayList<>();
        String selectedLocation;
        Locale locale = from.equals("home") || from.equalsIgnoreCase("addProduct") ?
                Locale.ENGLISH : LocaleManager.getLocale(mContext.getResources());
        if (Geocoder.isPresent()) {
            try {
                Geocoder geocoder;
                if (from.equals("home") || from.equalsIgnoreCase("addProduct"))
                    geocoder = new Geocoder(mContext, locale);
                else
                    geocoder = new Geocoder(mContext, locale);
                resultList = geocoder.getFromLocation(lat, lng, 1);
                selectedLocation = parseLocation(resultList);
            } catch (IOException e) {
                resultList = LocationHelper.getLocationFromApi(mContext, lat, lng, locale);
                selectedLocation = parseLocation(resultList);
            }
        } else {
            resultList = LocationHelper.getLocationFromApi(mContext, lat, lng, locale);
            selectedLocation = parseLocation(resultList);
        }
        return selectedLocation;
    }

    private String parseLocation(List<Address> addresses) {
        String selectedLocation = null;
        if (addresses != null && !addresses.isEmpty()) {
            mCountryCode = addresses.get(0).getCountryCode();
            if (addresses.get(0).getAddressLine(1) != null) {
                selectedLocation = (addresses.get(0).getAddressLine(0)
                        .toString() + ", " + addresses.get(0).getAddressLine(1) + ", " +
                        addresses.get(0).getCountryName());
            } else {
                selectedLocation =
                        addresses.get(0).getAddressLine(0).toString() + ", " +
                                addresses.get(0).getCountryName();
            }
        }
        return selectedLocation;
    }

    private Data createOutputData(String selectedLocation) {
        Data.Builder builder = new Data.Builder();
        builder.putString(Constants.TAG_LOCATION, selectedLocation);
        builder.putString(Constants.TAG_COUNTRY_CODE, mCountryCode);
        return builder.build();
    }
}
