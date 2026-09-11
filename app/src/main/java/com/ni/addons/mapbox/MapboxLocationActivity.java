package com.ni.addons.mapbox;

import static com.mapbox.common.TileStoreOptions.MAPBOX_ACCESS_TOKEN;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.ni.helper.LocaleManager;
import com.ni.helper.NetworkReceiver;
import com.ni.SnatchItUp.AddProductDetail;
import com.ni.SnatchItUp.BaseActivity;
import com.ni.SnatchItUp.FragmentMainActivity;
import com.ni.SnatchItUp.JoysaleApplication;
import com.ni.SnatchItUp.R;
import com.ni.SnatchItUp.SearchAdvance;
import com.ni.utils.ApiClient;
import com.ni.utils.ApiInterface;
import com.ni.utils.Constants;
import com.ni.utils.GetSet;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.android.gestures.MoveGestureDetector;


import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin;
import com.mapbox.maps.plugin.animation.CameraAnimationsUtils;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.GesturesPlugin;
import com.mapbox.maps.plugin.gestures.GesturesUtils;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;
import com.mapbox.maps.plugin.locationcomponent.generated.LocationComponentSettings;



import com.mapbox.search.ResponseInfo;
import com.mapbox.search.ReverseGeoOptions;

import com.mapbox.search.SearchCallback;
import com.mapbox.search.SearchEngine;
import com.mapbox.search.SearchEngineSettings;
import com.mapbox.search.SearchOptions;

import com.mapbox.search.SearchSelectionCallback;
import com.mapbox.search.common.AsyncOperationTask;
import com.mapbox.search.result.SearchResult;
import com.mapbox.search.result.SearchSuggestion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapboxLocationActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, TextWatcher, PermissionsListener, MapBoxAutoCompleteAdapter.MapBoxAutoCompleteInterface {

    private static final String TAG = MapboxLocationActivity.class.getSimpleName();
    AutoCompleteTextView autocomplete;
    TextView title, setLoc, remLoc;
    ImageView backbtn, search, crossIcon, myLocation, hoveringMarker;
    private RelativeLayout searchLay, bottomLay;
    private LinearLayout markerLay;
    private MapView mapView;

    public static boolean locationRemoved = false;
    private boolean isLocationApplied = false, isLocationSelected = false;
    LatLng center;
    MapBoxAutoCompleteAdapter adapter;
    ProgressDialog dialog;
    Location mCurrentLocation;
    InputMethodManager imm;
    private PermissionsManager permissionsManager;
    List<SearchSuggestion> suggestionList = new ArrayList<>();
    String chatId, from = "";

    ApiInterface apiInterface;
    public static String location;
    public static double lat, lng;

    Display display;
    public String tempLocation;
    public static SharedPreferences adminPref;
    private static final String DROPPED_MARKER_LAYER_ID = "DROPPED_MARKER_LAYER_ID";

    private long lastEdited = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    Runnable inputFinishChecker = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() > (lastEdited + Constants.TYPING_DELAY) - 500) {
                //getAutoComplete();
            }
        }
    };

    private MapboxMap mapboxMap;
    private SearchEngine searchEngine;
    //private MapboxGeocoding geocoding;
    private AsyncOperationTask searchRequestTask;
    private ReverseGeoOptions reverseGeocoding;
    private AsyncOperationTask reverseSearchRequestTask;
    private CameraAnimationsPlugin camera;
    private LocationComponentPlugin locationPlugin;
    private GesturesPlugin gesturePlugin;

    private OnIndicatorPositionChangedListener onIndicatorPositionChangedListener = new OnIndicatorPositionChangedListener() {
        @Override
        public void onIndicatorPositionChanged(@NonNull Point point) {
            gesturePlugin.setFocalPoint(mapboxMap.pixelForCoordinate(point));
        }
    };

    private final SearchSelectionCallback searchCallback = new SearchSelectionCallback() {

        @Override
        public void onSuggestions(@NonNull List<SearchSuggestion> list, @NonNull ResponseInfo responseInfo) {
            if (list.isEmpty()) {
                Log.e(TAG, "No suggestions found");
            } else {
                searchRequestTask = searchEngine.select(suggestions.get(0), this);
                adapter.setResultLists((ArrayList<SearchSuggestion>) list);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onCategoryResult(@NonNull SearchSuggestion searchSuggestion, @NonNull List<SearchResult> list, @NonNull ResponseInfo responseInfo) {

        }


        @Override
        public void onResult(@NonNull SearchSuggestion suggestion, @NonNull SearchResult result, @NonNull ResponseInfo info) {

        }



        @Override
        public void onError(@NonNull Exception e) {
            Log.i(TAG, "Search error: ", e);
        }
    };
    private SearchCallback reverseSearchCallback = new SearchCallback() {
        @Override
        public void onResults(@NonNull List<SearchResult> list, @NonNull ResponseInfo responseInfo) {

            if (list.isEmpty()) {
                Log.i(TAG, "No reverse geocoding results");
            } else {
                tempLocation = getFormattedAddress(list.get(0));
                autocomplete.setText(tempLocation);
                if (from.equals("home")) {

                } else if (from.equals("add")) {
                    if (AddProductDetail.location != null && AddProductDetail.txtLocation != null) {
                        AddProductDetail.txtLocation.setText(tempLocation);
                        AddProductDetail.txtLocation.setTextColor(getResources().getColor(R.color.primaryText));
                        AddProductDetail.locArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                    }
                } else if (from.equals(Constants.TAG_FILTERS)) {
                    SearchAdvance.location = tempLocation;
                } else if (from.equals(Constants.TAG_PROFILE)) {
                    if (isLocationApplied) {
                        Intent profile = new Intent();
                        String countryName = null, stateName = null, cityName = null;
                        Log.d(TAG, "onResults: " + new Gson().toJson(list.get(0)));
                        if (list.get(0).getAddress() != null) {
                            cityName = list.get(0).getAddress().getDistrict();
                            stateName = list.get(0).getAddress().getRegion();
                            countryName = list.get(0).getAddress().getCountry();
                        }

                        profile.putExtra(Constants.TAG_CITY_NAME, cityName);
                        profile.putExtra(Constants.TAG_STATE_NAME, stateName);
                        profile.putExtra(Constants.TAG_COUNTRY_NAME, countryName);
                        setResult(RESULT_OK, profile);
                        finish();
                    }
                }
                isLocationSelected = false;
            }
        }


        @Override
        public void onError(@NonNull Exception e) {
            e.printStackTrace();
        }
    };
    private OnMoveListener onMoveListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
                new SearchEngineSettings(getString(R.string.mapbox_access_token))
        );
        //reverseGeocoding = MapboxSearchSdk.getReverseGeocodingSearchEngine();

        setContentView(R.layout.activity_mapbox_location);
        title = (TextView) findViewById(R.id.title);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        hoveringMarker = (ImageView) findViewById(R.id.marker);
        mapView = findViewById(R.id.mapView);
        myLocation = (ImageView) findViewById(R.id.my_location);
        title = (TextView) findViewById(R.id.title);
        setLoc = (TextView) findViewById(R.id.apply);
        remLoc = (TextView) findViewById(R.id.reset);
        searchLay = (RelativeLayout) findViewById(R.id.searchLayout);
        search = (ImageView) findViewById(R.id.search);
        crossIcon = (ImageView) findViewById(R.id.cross_icon);
        backbtn = (ImageView) findViewById(R.id.backbtn);
        autocomplete = (AutoCompleteTextView) findViewById(R.id.address);
        bottomLay = findViewById(R.id.bottomLay);
        markerLay = findViewById(R.id.markerLay);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        title.setText(getString(R.string.location));
        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        backbtn.setOnClickListener(this);
        setLoc.setOnClickListener(this);
        remLoc.setOnClickListener(this);
        crossIcon.setOnClickListener(this);
        autocomplete.setOnItemClickListener(this);
        autocomplete.addTextChangedListener(this);
        myLocation.setOnClickListener(this);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        from = (String) getIntent().getExtras().get(Constants.TAG_FROM);
        location = (String) getIntent().getExtras().get(Constants.TAG_LOCATION);
        chatId = getIntent().getExtras().getString(Constants.CHATID);
        lat = getIntent().getExtras().getDouble(Constants.TAG_LAT);
        lng = getIntent().getExtras().getDouble(Constants.TAG_LON);
        if (from.equals(Constants.TAG_FILTERS)) {
            if (Constants.ADDON_SINGLE_COUNTRY) {
                remLoc.setText(getString(R.string.default_country_name));
            } else {
                remLoc.setText(getString(R.string.world_wide));
            }
        }
        if (TextUtils.isEmpty(location)) {
            if (Constants.ADDON_SINGLE_COUNTRY) {
                location = getString(R.string.default_country_name);
            } else {
                location = getString(R.string.world_wide);
            }
        }
        if (Constants.ADDON_SINGLE_COUNTRY) {
            if (!location.equals(getString(R.string.default_country_name))) {
                isLocationSelected = true;
                tempLocation = location;
            }
        } else {
            if (!location.equals(getString(R.string.world_wide))) {
                isLocationSelected = true;
                tempLocation = location;
            }
            Mapbox.getInstance(this, getString(R.string.access_token));
        }

        /** Function for join the user to chat **/
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                JSONObject jobj = new JSONObject();
                try {
                    jobj.put("joinid", GetSet.getUserName());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, 2000);

        adapter = new MapBoxAutoCompleteAdapter(MapboxLocationActivity.this, R.layout.dropdown_layout, from, this);
        autocomplete.setAdapter(adapter);

        autocomplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    crossIcon.setVisibility(View.VISIBLE);
                } else {
                    crossIcon.setVisibility(View.GONE);
                }
                handler.removeCallbacks(inputFinishChecker);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                lastEdited = System.currentTimeMillis();
                handler.postDelayed(inputFinishChecker, Constants.TYPING_DELAY);
            }
        });

        display = this.getWindowManager().getDefaultDisplay();
        autocomplete.setDropDownWidth(display.getWidth() - JoysaleApplication.dpToPx(this, 30));

        if (from.equals("add")) {
            remLoc.setVisibility(View.GONE);
        } else if (from.equals(Constants.TAG_CHAT) || from.equals(Constants.TAG_EXCHANGE)) {
            searchLay.setVisibility(View.GONE);
            setLoc.setVisibility(View.GONE);
            remLoc.setText(getString(R.string.share));
            autocomplete.setVisibility(View.VISIBLE);
        } else if (from.equals(Constants.TAG_VIEW)) {
            bottomLay.setVisibility(View.INVISIBLE);
            markerLay.setVisibility(View.GONE);
            searchLay.setVisibility(View.GONE);
        } else if (from.equals("locationtxt")) {
            remLoc.setVisibility(View.GONE);
        } else if (from.equals(Constants.TAG_PROFILE)) {
            remLoc.setVisibility(View.GONE);
        }
        onMapReady();
    }

    /*private void getAutoComplete() {
        if (!TextUtils.isEmpty(autocomplete.getText()) && autocomplete.getText().length() > 3) {
            SearchOptions options;
            if (Constants.ADDON_SINGLE_COUNTRY) {
                options = new SearchOptions.Builder()
                        .countries(new Country(getResources().getString(R.string.default_country_code)))
                        .build();
            } else {
                options = new SearchOptions.Builder()
                        .limit(5)
                        .build();
            }
            if (searchRequestTask != null) {
                searchRequestTask.cancel();
            }
            searchRequestTask = searchEngine.search(autocomplete.getText().toString(), options, searchCallback);
        }
    }*/

    @Override
    public void onPlaceClick(SearchSuggestion suggestion, int position, String location) {
        isLocationSelected = true;
        autocomplete.setText(location);
        autocomplete.dismissDropDown();
        JoysaleApplication.hideSoftKeyboard(MapboxLocationActivity.this);
        try {
            //geoCodeAddress(location);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void geoCodeAddress(String location) {
        geocoding = MapboxGeocoding.builder()
                .accessToken(getString(R.string.mapbox_access_token))
                .query(location)
                .limit(1)
                .build();
        MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken(MAPBOX_ACCESS_TOKEN)
                .query("1600 Pennsylvania Ave NW")
                .build();
        // Execute the forward geocoding request
        geocoding.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                if (response.isSuccessful() && response.body().features().size() > 0) {
                    CarmenFeature feature = response.body().features().get(0);
                    Geometry geometry = feature.geometry();
                    String geometryString = geometry.toJson();
                    try {
                        JSONObject geometryJson = new JSONObject(geometryString);
                        JSONArray coordinates = geometryJson.getJSONArray("coordinates");
                        lng = coordinates.getDouble(0);
                        lat = coordinates.getDouble(1);
                        setCameraOption(lat, lng);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }


    private void hideOnScreenKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }

    }

    private void updateMap(double latitude, double longitude) {
        // Build marker

        // Animate camera to geocoder result location
        /*CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(15)
                .build();
        mapboxMap1.animateCamera(newCameraPosition(cameraPosition), 500, null);*/

    }

    public void onMapReady() {
        camera = CameraAnimationsUtils.getCamera(mapView);
        mapboxMap = mapView.getMapboxMap();
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                Log.d(TAG, "onStyleLoaded: ");
                initMapListener();
                initLocationPluck();
                initGestureListener();
                if (from.equals("home") || from.equals(Constants.TAG_CHAT) ||
                        from.equals(Constants.TAG_EXCHANGE) || from.equals(Constants.TAG_FILTERS)) {
                    if (Constants.ADDON_SINGLE_COUNTRY) {
                        if (!location.equals(getString(R.string.default_country_name))) {
                            autocomplete.setText(location);
                        }
                    } else {
                        if (!location.equals(getString(R.string.world_wide))) {
                            autocomplete.setText(location);
                        }
                    }
                    setCameraOption(lat, lng);
                } else if (from.equals("add")) {
                    if (AddProductDetail.lat == 0 && AddProductDetail.lng == 0) {
                        getCurrentLocation(true);
                    } else {
                        autocomplete.setText(location);
                        autocomplete.dismissDropDown();
                        setCameraOption(AddProductDetail.lat, AddProductDetail.lng);
                    }
                } else if (from.equals(Constants.TAG_PROFILE)) {
                    isLocationSelected = true;
                    autocomplete.setText(location);
                    autocomplete.dismissDropDown();
                    JoysaleApplication.hideSoftKeyboard(MapboxLocationActivity.this);
                    //geoCodeAddress(location);
                } else if (from.equals(Constants.TAG_VIEW)) {
                    setCameraOption(lat, lng);
                    initDroppedMarker(lat, lng, style);
                }
                loadData();
            }
        });
    }

    private void initMapListener() {
        mapboxMap.addOnMapIdleListener(new OnMapIdleListener() {
            @Override
            public void onMapIdle(@NonNull MapIdleEventData mapIdleEventData) {
                if (!isLocationSelected) {
                    Point point = mapboxMap.getCameraState().getCenter();
                    center = new LatLng(point.latitude(), point.longitude());
                    lat = center.latitude;
                    lng = center.longitude;
                    reverseSearch(lat, lng);
                }
            }
        });
    }

    private void reverseSearch(Double lat, Double lng) {
        final ReverseGeoOptions options = new ReverseGeoOptions.Builder(Point.fromLngLat(lng, lat))
                .limit(1)
                .build();

        if (reverseSearchRequestTask != null) {
            reverseSearchRequestTask.cancel();
        }
        reverseSearchRequestTask = reverseGeocoding.search(options, reverseSearchCallback);
    }

    private void initLocationPluck() {
        locationPlugin = LocationComponentUtils.getLocationComponent(mapView);
        gesturePlugin = GesturesUtils.getGestures(mapView);
        LocationPuck2D puck = new LocationPuck2D();
        puck.setBearingImage(ContextCompat.getDrawable(this, R.drawable.mapbox_user_puck_icon));
        puck.setShadowImage(ContextCompat.getDrawable(this, R.drawable.mapbox_user_icon_shadow));
        locationPlugin.updateSettings(new Function1<LocationComponentSettings, Unit>() {
            @Override
            public Unit invoke(LocationComponentSettings locationComponentSettings) {
                locationComponentSettings.setLocationPuck(puck);
                locationComponentSettings.setEnabled(true);
                return null;
            }
        });
        locationPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
    }

    private void initGestureListener() {
        onMoveListener = new OnMoveListener() {
            @Override
            public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {
                if (!isLocationSelected && !from.equals(Constants.TAG_VIEW)) {
                    Point point = mapboxMap.getCameraState().getCenter();
                    center = new LatLng(point.latitude(), point.longitude());
                    lat = center.latitude;
                    lng = center.longitude;
                    //reverseSearch(lat, lng);
                }
            }

            @Override
            public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
                isLocationSelected = false;
            }

            @Override
            public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
                return false;
            }
        };
        gesturePlugin.addOnMoveListener(onMoveListener);
    }

    private void onCameraTrackingDismissed() {
        locationPlugin
                .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
        gesturePlugin.removeOnMoveListener(onMoveListener);
    }

    private void setCameraOption(double lat, double lon) {
        Log.d(TAG, "setCameraOption: " + lat + ", " + lon);
        CameraOptions cameraOptions = new CameraOptions.Builder()
                .center(Point.fromLngLat(lon, lat))
                .zoom(14.0)
                .build();
        camera.flyTo(
                cameraOptions,
                new MapAnimationOptions.Builder().duration(1500)
                        .animatorListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {
                                setLoc.setEnabled(false);
                                remLoc.setEnabled(false);
                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                setLoc.setEnabled(true);
                                remLoc.setEnabled(true);
                            }

                            @Override
                            public void onAnimationCancel(Animator animator) {
                                setLoc.setEnabled(true);
                                remLoc.setEnabled(true);
                            }

                            @Override
                            public void onAnimationRepeat(Animator animator) {

                            }
                        })
                        .build()
        );
//        mapboxMap.setCamera(cameraOptions);
    }

    private void initDroppedMarker(Double lat, Double lng, @NonNull Style loadedMapStyle) {
        // Add the marker image to map
        Bitmap marker = BitmapFactory.decodeResource(getResources(), R.drawable.map);
        int width = 120;
        int height = 120;
        Bitmap resizedbitmap = Bitmap.createScaledBitmap(marker, width, height, true);
        AnnotationPlugin annotationAPI = AnnotationPluginImplKt.getAnnotations(mapView);
        PointAnnotationManager pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationAPI, mapView);
        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                .withPoint(com.mapbox.geojson.Point.fromLngLat(lng, lat))
                .withIconImage(resizedbitmap);
        pointAnnotationManager.create(pointAnnotationOptions);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        crossIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.length() > 0) {
            crossIcon.setVisibility(View.VISIBLE);

        } else {
            crossIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadData() {
        if (from.equals("home") || from.equals(Constants.TAG_CHAT) ||
                from.equals(Constants.TAG_EXCHANGE) || from.equals(Constants.TAG_FILTERS)) {
            if (lat == 0.0 && lng == 0.0) {
                if (mCurrentLocation == null) {
                    getCurrentLocation(true);
                } else {
                    lat = mCurrentLocation.getLatitude();
                    lng = mCurrentLocation.getLongitude();
                }
            } else {
                setCameraOption(lat, lng);
            }
        } else if (from.equals("add")) {
            // if (AddProductDetail.l == 0 && AddProductDetail.lon == 0) {

            // }
        }

    }

    @SuppressWarnings({"MissingPermission"})
    private void getCurrentLocation(boolean moveCamera) {
        // Check if permissions are enabled and if not request
        try {
            if (PermissionsManager.areLocationPermissionsGranted(this)) {
                JoysaleApplication.getLocationEngine().getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
                    @Override
                    public void onSuccess(LocationEngineResult result) {
                        if (result != null && result.getLastLocation() != null) {
                            lat = result.getLastLocation().getLatitude();
                            lng = result.getLastLocation().getLongitude();
                            mCurrentLocation = result.getLastLocation();
                            if (moveCamera) {
                                setCameraOption(lat, lng);
                                //reverseSearch(lat, lng);
d
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        exception.printStackTrace();
                    }
                });
            } else {
                permissionsManager = new PermissionsManager(MapboxLocationActivity.this);
                permissionsManager.requestLocationPermissions(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {/*
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

// Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

// Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    */
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    /**
     * Function for send the location to server
     **/

    private void sendLocationToServer(final String chat_type, final String type, final String params, final double latitude, final double longitude) {
        if (NetworkReceiver.isConnected()) {
            Map<String, String> map = new HashMap<String, String>();
            long unixTime = System.currentTimeMillis() / 1000L;
            double lat = latitude;
            double lon = longitude;
            map.put(Constants.SOAP_USERNAME, Constants.SOAP_USERNAME_VALUE);
            map.put(Constants.SOAP_PASSWORD, Constants.SOAP_PASSWORD_VALUE);
            map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
            map.put(Constants.TAG_CHAT_ID, chatId);
            if (getIntent().getStringExtra(Constants.TAG_SOURCE_ID) != null)
                map.put(Constants.TAG_SOURCE_ID, getIntent().getStringExtra(Constants.TAG_SOURCE_ID));
            else
                map.put(Constants.TAG_SOURCE_ID, "0");
            map.put(Constants.TAG_IMAGE_URL, "");
            map.put(Constants.TAG_TYPE, type);
            map.put(Constants.TAG_CREATED_DATE, Long.toString(unixTime));
            map.put(Constants.TAG_MESSAGE, params);
            if (chat_type != null && chat_type.length() != 0) {
                map.put(Constants.TAG_CHAT_TYPE, chat_type);
            } else {
                map.put(Constants.TAG_CHAT_TYPE, "normal");
            }

            if (lat != 0.0 && longitude != 0.0) {
                try {
                    NumberFormat nf = NumberFormat.getInstance(new Locale("en", "US"));
                    DecimalFormat dFormat = new DecimalFormat("#.######");
                    if (LocaleManager.isRTL(getApplicationContext())) {
                        lat = Double.valueOf(nf.format(lat));
                        lon = Double.valueOf(nf.format(lon));
                    } else {

                        lat = Double.valueOf(dFormat.format(lat));
                        lon = Double.valueOf(dFormat.format(lon));
                    }
                    map.put(Constants.TAG_CURRENT_LATITUDE, String.valueOf(lat));
                    map.put(Constants.TAG_CURRENT_LONGITUDE, String.valueOf(lon));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else {
                map.put(Constants.TAG_CURRENT_LATITUDE, "");
                map.put(Constants.TAG_CURRENT_LONGITUDE, "");
            }

            Call<HashMap<String, String>> call = apiInterface.sendChat(map);
            call.enqueue(new Callback<HashMap<String, String>>() {
                @Override
                public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                    if (response.isSuccessful()) {
                        long unixTime = System.currentTimeMillis() / 1000L;
                        JSONObject jsonObject = callSocket(unixTime, "share_location", latitude, longitude);
                        Intent i = new Intent();
                        i.putExtra(Constants.TAG_CURRENT_LATITUDE, "" + latitude);
                        i.putExtra(Constants.TAG_CURRENT_LONGITUDE, "" + longitude);
                        i.putExtra("jsonObject", String.valueOf(jsonObject));
                        setResult(RESULT_OK, i);
                        finish();
                    } else {
                        remLoc.setEnabled(true);
                    }
                }

                @Override
                public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                    remLoc.setEnabled(true);
                    call.cancel();
                    if (dialog.isShowing() && dialog != null) {
                        dialog.dismiss();
                    }
                    JoysaleApplication.dialog(MapboxLocationActivity.this, getResources().getString(R.string.alert), getString(R.string.somethingwrong));
                }
            });
        } else {
            remLoc.setEnabled(true);
        }
    }


    /**
     * Function to send a Location to Socket
     **/
    private JSONObject callSocket(long time, String type, double lat, double lon) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject message = new JSONObject();
            message.put(Constants.TAG_CHATTIME, Long.toString(time));
            message.put(Constants.SOCK_USERIMAGE, GetSet.getImageUrl().replace("/150/", "/40/"));
            message.put(Constants.SOCK_USERNAME, GetSet.getUserName());
            message.put(Constants.TAG_MESSAGE, "");
            message.put(Constants.SOCK_VIEW_URL, "");
            message.put(Constants.TAG_TYPE, "share_location");
            message.put(Constants.TAG_LAT, lat);
            message.put(Constants.TAG_LON, lon);
            message.put(Constants.SOCK_MESSAGE_CONTENT, "3");
            jobj.put(Constants.TAG_MESSAGE, message);
            jobj.put(Constants.SOCK_RECEIVERID, GetSet.getUserName());
            jobj.put(Constants.SOCK_SENDERID, getIntent().getStringExtra(Constants.TAG_USERNAME));
            jobj.put("offerId", "0");
            if (getIntent().getStringExtra(Constants.TAG_SOURCE_ID) != null) {
                jobj.put(Constants.SOCK_SOURCE_ID, getIntent().getStringExtra(Constants.TAG_SOURCE_ID));
            } else {
                jobj.put(Constants.SOCK_SOURCE_ID, "0");
            }
            Log.v(TAG, "sendLocationJSON=" + jobj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jobj;
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.apply: {
                Point point = mapboxMap.getCameraState().getCenter();
                center = new LatLng(point.latitude(), point.longitude());
                if (from.equals("home")) {
                    try {
                        if (autocomplete.getText().toString().length() == 0) {
//                            new GetLocationAsync(center.getLatitude(), center.getLongitude()).execute().get();
//                            makeGeocodeSearch(new LatLng(center.latitude, center.longitude), "");
                            lat = center.latitude;
                            lng = center.longitude;
                            locationRemoved = false;
                            Constants.fileditor.putString(Constants.TAG_LAT, String.valueOf(lat));
                            Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(lng));
                            Constants.fileditor.putBoolean("locationRemoved", locationRemoved);
                            Constants.fileditor.commit();

                            Constants.isLocationChanged = true;
                            FragmentMainActivity.currentPage = 0;
                            if (FragmentMainActivity.mScrollListener != null) {
                                FragmentMainActivity.mScrollListener.resetpagecount();
                            }
                            FragmentMainActivity.homeItemList.clear();
                            if (FragmentMainActivity.itemAdapter != null) {
                                FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                            }
                            finish();
                            Intent j = new Intent(MapboxLocationActivity.this, FragmentMainActivity.class);
                            startActivity(j);

                        } else {
                            Double latn[] = new Double[2];
//                            latn = new GetLocationFromString().execute(address.getText().toString().trim()).get();
                            lat = center.latitude;
                            lng = center.longitude;
                            location = autocomplete.getText().toString().trim();
                            locationRemoved = false;
                            if (FragmentMainActivity.locationTxt != null) {
                                FragmentMainActivity.locationTxt.setText(location);
                            }
                            Constants.fileditor.putString("location", location);
                            Constants.fileditor.putString(Constants.TAG_LAT, String.valueOf(lat));
                            Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(lng));
                            Constants.fileditor.putBoolean("locationRemoved", locationRemoved);
                            Constants.fileditor.commit();

                            Constants.isLocationChanged = true;
                            FragmentMainActivity.currentPage = 0;
                            if (FragmentMainActivity.mScrollListener != null) {
                                FragmentMainActivity.mScrollListener.resetpagecount();
                            }
                            FragmentMainActivity.homeItemList.clear();
                            if (FragmentMainActivity.itemAdapter != null) {
                                FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                            }
                            finish();
                            Intent j = new Intent(MapboxLocationActivity.this, FragmentMainActivity.class);
                            startActivity(j);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (from.equals(Constants.TAG_PROFILE)) {
                    isLocationApplied = true;
                    final ReverseGeoOptions options = new ReverseGeoOptions.Builder(Point.fromLngLat(center.longitude, center.latitude))
                            .limit(1)
                            .build();

                    if (reverseSearchRequestTask != null) {
                        reverseSearchRequestTask.cancel();
                    }
                    reverseSearchRequestTask = reverseGeocoding.search(options, reverseSearchCallback);
                } else if (from.equals(Constants.TAG_FILTERS)) {
                    locationRemoved = false;
                    if (TextUtils.isEmpty(autocomplete.getText())) {
//                        makeGeocodeSearch(new LatLng(center.latitude, center.longitude), "");
                        lat = center.latitude;
                        lng = center.longitude;
                        location = tempLocation;
                        Constants.fileditor.putString(Constants.TAG_LOCATION, location);
                        Constants.fileditor.putString(Constants.TAG_LAT, String.valueOf(lat));
                        Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(lng));
                        Constants.fileditor.putBoolean(Constants.TAG_LOCATION_REMOVED, locationRemoved);
                        Constants.fileditor.commit();
                        Intent filter = new Intent();
                        filter.putExtra(Constants.TAG_LAT, lat);
                        filter.putExtra(Constants.TAG_LON, lng);
                        filter.putExtra(Constants.TAG_LOCATION, location);
                        filter.putExtra(Constants.TAG_LOCATION_REMOVED, locationRemoved);
                        setResult(RESULT_OK, filter);
                        finish();
                    } else {
                        lat = center.latitude;
                        lng = center.longitude;
                        location = autocomplete.getText().toString().trim();
                        locationRemoved = false;
                        Constants.fileditor.putString(Constants.TAG_LOCATION, location);
                        Constants.fileditor.putString(Constants.TAG_LAT, String.valueOf(lat));
                        Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(lng));
                        Constants.fileditor.putBoolean(Constants.TAG_LOCATION_REMOVED, locationRemoved);
                        Constants.fileditor.commit();
                        Intent filter = new Intent();
                        filter.putExtra(Constants.TAG_LAT, lat);
                        filter.putExtra(Constants.TAG_LON, lng);
                        filter.putExtra(Constants.TAG_LOCATION, tempLocation);
                        filter.putExtra(Constants.TAG_LOCATION_REMOVED, locationRemoved);
                        setResult(RESULT_OK, filter);
                        finish();
                    }
                    break;
                } else {
                    try {
                        if (TextUtils.isEmpty(autocomplete.getText())) {
//                            makeGeocodeSearch(new LatLng(center.latitude, center.longitude), "");
                            AddProductDetail.lat = center.latitude;
                            AddProductDetail.lng = center.longitude;

                        } else {
                            AddProductDetail.lat = center.latitude;
                            AddProductDetail.lng = center.longitude;
                            AddProductDetail.location = autocomplete.getText().toString().trim();

                            if (AddProductDetail.location != null) {
                                AddProductDetail.txtLocation.setText(AddProductDetail.location);
                                AddProductDetail.txtLocation.setTextColor(getResources().getColor(R.color.primaryText));
                                AddProductDetail.locArrow.setColorFilter(getResources().getColor(R.color.primaryText));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finish();
                }
            }
            break;
            case R.id.reset:
                switch (from) {
                    case "home": {
                        lat = 0;
                        lng = 0;
                        if (Constants.ADDON_SINGLE_COUNTRY) {
                            location = getString(R.string.default_country_name);
                        } else {
                            location = getString(R.string.world_wide);
                        }
                        locationRemoved = true;
                        if (FragmentMainActivity.locationTxt != null) {
                            FragmentMainActivity.locationTxt.setText(location);
                        }
                        Constants.fileditor.putString("location", location);
                        Constants.fileditor.putString("lat", String.valueOf(lat));
                        Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(lng));
                        Constants.fileditor.putBoolean("locationRemoved", locationRemoved);
                        Constants.fileditor.commit();

                        FragmentMainActivity.currentPage = 0;
                        FragmentMainActivity.homeItemList.clear();
                        if (FragmentMainActivity.itemAdapter != null) {
                            FragmentMainActivity.itemAdapter.notifyDataSetChanged();
                        }
                        finish();
                        Intent i = new Intent(MapboxLocationActivity.this, FragmentMainActivity.class);
                        startActivity(i);
                    }
                    break;
                    case "add": {
                        AddProductDetail.lat = 0;
                        AddProductDetail.lng = 0;
                        AddProductDetail.location = "";
                        if (AddProductDetail.location != null) {
                            if (Constants.ADDON_SINGLE_COUNTRY) {
                                AddProductDetail.txtLocation.setText(getString(R.string.default_country_name));
                            } else {
                                AddProductDetail.txtLocation.setText(getString(R.string.world_wide));
                            }
                            AddProductDetail.txtLocation.setTextColor(getResources().getColor(R.color.secondaryText));
                            AddProductDetail.locArrow.setColorFilter(getResources().getColor(R.color.secondaryText));
                        }
                        finish();
                    }
                    break;
                    case Constants.TAG_CHAT:
                    case Constants.TAG_EXCHANGE: {
                        remLoc.setEnabled(false);
                        sendLocationToServer(getIntent().getStringExtra("chat_type"), "share_location", "", lat, lng);
                    }
                    break;
                    case Constants.TAG_FILTERS:
                        lat = 0;
                        lng = 0;
                        if (Constants.ADDON_SINGLE_COUNTRY) {
                            location = getString(R.string.default_country_name);
                        } else {
                            location = getString(R.string.world_wide);
                        }
                        locationRemoved = true;
                        Constants.fileditor.putString("location", location);
                        Constants.fileditor.putString("lat", String.valueOf(lat));
                        Constants.fileditor.putString(Constants.TAG_LON, String.valueOf(lng));
                        Constants.fileditor.putBoolean(Constants.TAG_LOCATION_REMOVED, locationRemoved);
                        Constants.fileditor.commit();

                        Intent filter = new Intent();
                        filter.putExtra(Constants.TAG_LAT, lat);
                        filter.putExtra(Constants.TAG_LON, lng);
                        filter.putExtra(Constants.TAG_LOCATION, location);
                        filter.putExtra(Constants.TAG_LOCATION_REMOVED, locationRemoved);
                        setResult(RESULT_OK, filter);
                        finish();
                        break;
                }
                break;

            case R.id.backbtn:
                finish();
                break;

            case R.id.cross_icon:
                autocomplete.getText().clear();
                break;

            case R.id.my_location:
                if (mCurrentLocation == null) {
                    getCurrentLocation(true);
                } else {
                    setCameraOption(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                }

                break;

        }
    }

    public static String getFormattedAddress(SearchResult item) {
        StringBuilder builder = new StringBuilder();
        builder.append(item.getName());
        builder.append(", ");
        if (item.getAddress() != null && item.getAddress().getLocality() != null) {
            builder.append(item.getAddress().getLocality());
            builder.append(", ");
        }

        if (item.getAddress() != null && item.getAddress().getDistrict() != null) {
            if (item.getAddress() != null && item.getAddress().getPlace() != null) {
                if (item.getAddress().getPlace().equalsIgnoreCase(item.getAddress().getDistrict())) {
                    builder.append(item.getAddress().getPlace());
                    builder.append(", ");
                } else {
                    builder.append(item.getAddress().getPlace());
                    builder.append(", ");
                    builder.append(item.getAddress().getDistrict());
                    builder.append(", ");
                }
            } else {
                if (!item.getName().equalsIgnoreCase(item.getAddress().getDistrict())) {
                    builder.append(item.getAddress().getDistrict());
                    builder.append(", ");
                }
            }
        }

        if (item.getAddress() != null && item.getAddress().getRegion() != null) {
            builder.append(item.getAddress().getRegion());
            builder.append(", ");
        }
        if (item.getAddress() != null && item.getAddress().getCountry() != null) {
            builder.append(item.getAddress().getCountry());
        }
        return builder.toString();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        mapboxMap.addOnCameraChangeListener(cameraListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        if (searchRequestTask != null) {
            searchRequestTask.cancel();
        }
        if (reverseSearchRequestTask != null) {
            reverseSearchRequestTask.cancel();
        }
        if (locationPlugin != null) {
            locationPlugin.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
        }
        if (gesturePlugin != null) {
            gesturePlugin.removeOnMoveListener(onMoveListener);
        }
        super.onDestroy();
//        mapboxMap.removeOnCameraChangeListener(cameraListener);
    }

}
