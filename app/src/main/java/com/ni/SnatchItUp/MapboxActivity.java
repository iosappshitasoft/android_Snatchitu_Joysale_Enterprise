package com.ni.SnatchItUp;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ni.utils.Constants;
//import com.mapbox.android.core.location.LocationEngineCallback;
//import com.mapbox.android.core.location.LocationEngineResult;
//import com.mapbox.android.core.permissions.PermissionsManager;
//import com.mapbox.geojson.Point;
//import com.mapbox.maps.CameraOptions;
//import com.mapbox.maps.EdgeInsets;
//import com.mapbox.maps.MapView;
//import com.mapbox.maps.extension.observable.eventdata.MapLoadedEventData;
//import com.mapbox.maps.plugin.animation.CameraAnimationsPlugin;
//import com.mapbox.maps.plugin.animation.CameraAnimationsUtils;
//import com.mapbox.maps.plugin.animation.MapAnimationOptions;
//import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadedListener;
//import com.mapbox.navigation.base.options.NavigationOptions;
//import com.mapbox.navigation.core.MapboxNavigation;
//import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp;
//import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver;
//import com.mapbox.navigation.core.trip.session.LocationMatcherResult;
//import com.mapbox.navigation.core.trip.session.LocationObserver;
//import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider;

public class MapboxActivity extends BaseActivity {
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }
   /* private static final String TAG = MapboxActivity.class.getSimpleName();
    private MapView mapView;
    public static double lat, lng;
    Location mCurrentLocation;
    NavigationLocationProvider navigationLocationProvider = new NavigationLocationProvider();
    private CameraAnimationsPlugin camera;
    TextView title, setLoc, remLoc;
    Location enhancedLocation;
    LocationObserver locationObserver = null;
    MapboxNavigation mapboxNavigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lat = getIntent().getExtras().getDouble(Constants.TAG_LAT);
        lng = getIntent().getExtras().getDouble(Constants.TAG_LON);
        remLoc = (TextView) findViewById(R.id.reset);
        //mapView.getMapboxMap().loadStyleUri(NavigationStyles.NAVIGATION_DAY_STYLE);
        MapboxNavigationApp.setup(
                new NavigationOptions.Builder(this)
                        .accessToken(getString(R.string.mapbox_access_token))
                        .build()
        );
        locationObserver = new LocationObserver() {
            @Override
            public void onNewRawLocation(@NonNull Location location) {

            }

            @Override
            public void onNewLocationMatcherResult(@NonNull LocationMatcherResult locationMatcherResult) {
                enhancedLocation = locationMatcherResult.getEnhancedLocation();
                navigationLocationProvider.changePosition(enhancedLocation, locationMatcherResult.getKeyPoints(), null, null);
                Log.d(TAG, "onNewLocationMatcherResult: "+ enhancedLocation.getLatitude());
                updateCamera(enhancedLocation);

            }
        };

        setMapboxNavigation();
        *//*ResourceOptions resourceOptions = new ResourceOptions.Builder().accessToken(getResources().getString(R.string.mapbox_access_token)).build();
        CameraOptions cameraOptions = new CameraOptions.Builder().center(Point.fromLngLat(lng,lat)).zoom(9.0).bearing(120.0).build();
        //Plugin.Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        MapOptions mapOptions = new MapOptions.Builder().constrainMode(ConstrainMode.HEIGHT_ONLY).glyphsRasterizationOptions(new GlyphsRasterizationOptions.Builder().rasterizationMode(GlyphsRasterizationMode.IDEOGRAPHS_RASTERIZED_LOCALLY).fontFamily("sans-serif").build()).build();

MapInitOptions mapInitOptions= new MapInitOptions(this,resourceOptions,mapOptions,null,cameraOptions,true);*//*


        setContentView(R.layout.activity_mapbox_location);

        mapView = findViewById(R.id.mapView);

        mapView.getMapboxMap().addOnMapLoadedListener(new OnMapLoadedListener() {
            @Override
            public void onMapLoaded(@NonNull MapLoadedEventData mapLoadedEventData) {
                camera = CameraAnimationsUtils.getCamera(mapView);
                Log.d(TAG, "onMapLoaded: " + mapLoadedEventData.getBegin() + ", " + mapLoadedEventData.getEnd());
                //getCurrentLocation(true);
            }
        });
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
    }



    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private void getCurrentLocation(boolean moveCamera) {
        // Check if permissions are enabled and if not request
        try {
            if (PermissionsManager.areLocationPermissionsGranted(this)) {
                JoysaleApplication.getLocationEngine().getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
                    @Override
                    public void onSuccess(LocationEngineResult result) {
                        Log.d("TAG", "onSuccess: ");
                        if (result != null && result.getLastLocation() != null) {
                            lat = result.getLastLocation().getLatitude();
                            lng = result.getLastLocation().getLongitude();
                            mCurrentLocation = result.getLastLocation();

                            if (moveCamera) {
                                //setCameraOption(lat, lng);
                                //reverseSearch(lat, lng);
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        exception.printStackTrace();
                        Log.d("TAG", "onFailure: ");
                    }
                });
            } else {
                //permissionsManager = new PermissionsManager(MapboxLocationActivity.this);
                //permissionsManager.requestLocationPermissions(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    *//*public void updateCamera(Location location) {
        MapAnimationOptions mapAnimationOptions = new MapAnimationOptions.Builder().duration(1500L).build();
        mapView.getMapboxMap().addOnCameraChangeListener(new OnCameraChangeListener() {
            @Override
            public void onCameraChanged(@NonNull CameraChangedEventData cameraChangedEventData) {

            }
        });
                new CameraOptions.Builder()
// Centers the camera to the lng/lat specified.
                        .center(Point.fromLngLat(location.getLatitude(), location.getLongitude()))
// specifies the zoom value. Increase or decrease to zoom in or zoom out
                        .zoom(12.0)
// specify frame of reference from the center.

                        .build(),
                mapAnimationOptions )
    }*//*
    private void updateCamera(Location location) {
        MapAnimationOptions mapAnimationOptions = new MapAnimationOptions.Builder().duration(1500L).build();
        CameraOptions cameraOptions = new CameraOptions.Builder()
                .center(Point.fromLngLat(location.getLongitude(), location.getLatitude()))
                .zoom(14.0)
                .build();
        camera.easeTo(
                new CameraOptions.Builder()
// Centers the camera to the lng/lat specified.
                        .center(Point.fromLngLat(location.getLongitude(), location.getLatitude()))
// specifies the zoom value. Increase or decrease to zoom in or zoom out
                        .zoom(12.0)
// specify frame of reference from the center.
                        .padding(new EdgeInsets(500.0, 0.0, 0.0, 0.0))
                        .build(),
                mapAnimationOptions
        );
       *//* camera.flyTo(
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
        );*//*
    }

    public void setMapboxNavigation() {
        //camera.initialize();
        MapboxNavigationObserver mapboxNavigationObserver = new MapboxNavigationObserver() {
            @Override
            public void onAttached(@NonNull MapboxNavigation mapboxNavigation) {
                Log.d(TAG, "onAttached: ");
                mapboxNavigation.registerLocationObserver(locationObserver);
                mapboxNavigation.startTripSession();
            }

            @Override
            public void onDetached(@NonNull MapboxNavigation mapboxNavigation) {
                mapboxNavigation.registerLocationObserver(locationObserver);
                mapboxNavigation.stopTripSession();
            }
        };
    }*/
}
