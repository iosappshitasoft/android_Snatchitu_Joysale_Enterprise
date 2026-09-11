package com.ni.helper.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ni.helper.LocaleManager;
import com.ni.SnatchItUp.BuildConfig;
import com.ni.SnatchItUp.R;
import com.ni.utils.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationHelper {

    private static final String TAG = LocationHelper.class.getSimpleName();

    public static List<Address> getLocationFromLatLng(Context context, String from, double lat, double lng) {
        List<Address> resultList = new ArrayList<>();
        Locale locale;
        if (from.equals("home") || from.equalsIgnoreCase("addProduct")) {
            locale = Locale.ENGLISH;
        } else {
            locale = LocaleManager.getLocale(context.getResources());
        }

        if (Geocoder.isPresent()) {
            try {
                Geocoder geocoder;
                if (from.equals("home") || from.equalsIgnoreCase("addProduct"))
                    geocoder = new Geocoder(context, Locale.ENGLISH);
                else
                    geocoder = new Geocoder(context, LocaleManager.getLocale(context.getResources()));
                resultList = geocoder.getFromLocation(lat, lng, 1);
            } catch (IOException e) {
                resultList = LocationHelper.getLocationFromApi(context, lat, lng, locale);
            }
        } else {
            resultList = LocationHelper.getLocationFromApi(context, lat, lng, locale);
        }
        return resultList;
    }

    public static List<Address> getLocationFromApi(Context mContext, double lat, double lng, Locale locale) {
        String selectedLocation;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        List<Address> resultList = new ArrayList<>();
        Address addressData = new Address(LocaleManager.getLocale(mContext.getResources()));
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            String sb = Constants.GOOGLE_MAPS_API + "geocode/json" + "?latlng=" + lat + "," + lng +
                    "&ka&sensor=true" + "&language=" + locale +
                    "&key=" + Constants.API_KEY;
            URL url = new URL(sb);
//            Log.i(TAG, "getLocation URL: " + url);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            JSONObject jsonObject = new JSONObject(jsonResults.toString());
            Log.i(TAG, "getLocationResults: " + jsonObject);

            JSONArray resultsArray = new JSONArray(jsonObject.optString("results"));
            JSONObject result = new JSONObject("" + resultsArray.get(0));
            addressData.setLatitude((Double) result.getJSONObject("geometry").getJSONObject("location").get("lat"));
            addressData.setLongitude((Double) result.getJSONObject("geometry").getJSONObject("location").get("lng"));
            JSONArray addressArray = result.getJSONArray("address_components");
            int size = addressArray.length();
            for (int i = 0; i < size; i++) {
                JSONArray typeArray = addressArray.getJSONObject(i)
                        .getJSONArray("types");
                if (typeArray.getString(0).equals("administrative_area_level_2")) {
                    addressData.setAddressLine(0, addressArray.getJSONObject(i).getString(
                            "long_name"));
                } else if (typeArray.getString(0).equals("locality")) {
                    addressData.setAddressLine(0, addressArray.getJSONObject(i).getString(
                            "long_name"));
                }

                if (typeArray.getString(0).equals("administrative_area_level_1")) {
                    addressData.setAddressLine(1, addressArray.getJSONObject(i).getString(
                            "long_name"));
                }
                if (typeArray.getString(0).equals("country")) {
                    addressData.setCountryName(addressArray.getJSONObject(i).getString(
                            "long_name"));
                }

                resultList.add(addressData);
            }

        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "withoutGeoCoderLocation: " + resultList);
        return resultList;
    }

    //Addon Single Country
    public static boolean isUserCountry(Context mContext, @NonNull String location) {
        String[] values = location.trim().split(",");
        String locText = values[values.length - 1].trim();
        return locText.trim().equalsIgnoreCase(mContext.getString(R.string.default_country_name));
    }

    public static String getStaticMapUrl(Context mContext, String latitude, String longitude, int screenWidth) {
        String url = Constants.GOOGLE_MAPS_API + "staticmap?center=" + latitude + "," + longitude +
                "&zoom=15&size=" + screenWidth + "x" + screenWidth / 2 + "&sensor=false&key=" + Constants.API_KEY;
        Log.d(TAG, "getStaticMapUrl: " + url);
        return url;
    }

    public static String getStaticMapUrl(Context mContext, String latitude, String longitude, int width, int height) {
        String url = Constants.GOOGLE_MAPS_API + "staticmap?center=" + latitude + "," + longitude +
                "&zoom=15&size=" + width + "x" + height + "&sensor=false&markers=" + latitude + "," + longitude +
                "|color:red" + "&key=" + Constants.API_KEY;
        Log.d(TAG, "getStaticMapUrl: " + url);
        return url;
    }
}
