package com.ni.mapbox

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Insets
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.View.*
import android.view.WindowInsets
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.gms.maps.model.LatLng
import com.ni.helper.Constants
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.observable.eventdata.MapIdleEventData
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.delegates.listeners.OnMapIdleListener
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.search.*
import com.app.mapbox.R
import com.mapbox.search.common.AsyncOperationTask
import com.mapbox.search.common.CompletionCallback
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor
import java.util.concurrent.Executors


@OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
public class MainActivity : AppCompatActivity(), OnMapIdleListener, OnClickListener,
    MapboxAutocompleteAdapter.MapBoxAutoCompleteInterface, AdapterView.OnItemClickListener {
    var mapView: MapView? = null
    var currenLocation: Location? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var lat: Double? = null
    var lng: Double? = null
    var autocompleteLatitude: Double? = null
    var autocompleteLongitude: Double? = null
    open var currentLocation: Location? = null
    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView?.getMapboxMap()?.setCamera(CameraOptions.Builder().bearing(it).build())
    }
    var title: TextView? = null
    var setLoc: TextView? = null
    var remLoc: TextView? = null
    var location: String? = null
    var autocomplete: AutoCompleteTextView? = null
    private lateinit var searchEngine: SearchEngine
    private lateinit var searchRequestTask: AsyncOperationTask
    public var adapter: MapboxAutocompleteAdapter? = null
    val TYPING_DELAY = 300L
    var tempLocation: String? = null
    var mapboxGeocoding: MapboxGeocoding? = null
    var chatId: String? = null
    var from: String? = ""
    var locationRemoved = false
    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView?.getMapboxMap()?.setCamera(CameraOptions.Builder().center(it).build())
        mapView?.gestures?.focalPoint = mapView?.getMapboxMap()?.pixelForCoordinate(it)
    }
    var mylocation: ImageView? = null
    var center: LatLng? = null
    var isLocationSelected: Boolean? = false
    private var searchLay: RelativeLayout? = null
    private var bottomLay: RelativeLayout? = null
    private var markerLay: LinearLayout? = null
    var crossImage: ImageView? = null
    public final var backBtn: ImageView? = null
    public var place: Boolean? = false
    public var isLocationMove: Boolean? = false
    public var editprofileLat: Double? = 0.0
    public var editprofileLng: Double? = 0.0
    private val navigationLocationProvider = NavigationLocationProvider()

    val searchCallback = object : SearchSuggestionsCallback {
        override fun onSuggestions(
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo
        ) {
            val suggestion = suggestions.firstOrNull()
            if (suggestions != null) {
                if (suggestions.size > 0) {
                    searchRequestTask = searchEngine.select(suggestions.get(0), selectCallback);
                    adapter!!.setResultLists(suggestions as ArrayList<SearchSuggestion>)
                    adapter!!.notifyDataSetChanged()
                }
            }
        }

        override fun onError(e: Exception) {
        }
    }
    val selectCallback = object : SearchSelectionCallback {
        override fun onResult(
            suggestion: SearchSuggestion,
            result: SearchResult,
            responseInfo: ResponseInfo
        ) {
        }

        override fun onSuggestions(
            suggestions: List<SearchSuggestion>,
            responseInfo: ResponseInfo
        ) {
            if (suggestions.isEmpty()) {
                Log.e(
                    "TAG",
                    "No suggestions found"
                )
            } else {
                searchRequestTask = searchEngine.select(suggestions[0], this)
                adapter!!.setResultLists(suggestions as ArrayList<SearchSuggestion>)
                adapter!!.notifyDataSetChanged()
            }
        }

        override fun onCategoryResult(
            suggestion: SearchSuggestion,
            results: List<SearchResult>,
            responseInfo: ResponseInfo
        ) {
        }

        override fun onError(e: Exception) {
        }

    }

    /**
     * Gets notified with location updates.
     *
     * Exposes raw updates coming directly from the location services
     * and the updates enhanced by the Navigation SDK (cleaned up and matched to the road).
     */
    private var lastEdited: Long = 0
    private var handler: Handler? = Handler(Looper.getMainLooper())
    var inputFinishChecker = Runnable {
        if (System.currentTimeMillis() > lastEdited + TYPING_DELAY - 500) {
            getAutoComplete();
        }
    }
    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {

        }

        /**
         * Provides the best possible location update, snapped to the route or
         * map-matched to the road if possible.
         */
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(
                enhancedLocation,
                locationMatcherResult.keyPoints,
            )
            // Invoke this method to move the camera to your current location.
            latitude = enhancedLocation.latitude
            longitude = enhancedLocation.longitude
            Log.d("", "onNewLocationMatcherResult: " + lat + lng)
            if (lat == 0.0 && lng == 0.0) {
                if (searchEngine != null) {
                    reverseSearch(lat!!, lng!!)
                }
                updateCamera(enhancedLocation)
                currentLocation = enhancedLocation
            } else {
                if (isLocationMove!!) {
                    reverseSearch(lat!!, lng!!)
                }
                if (!location!!.isEmpty()) {
                    autocomplete!!.setText(location)
                }
                updateCameraonClick(lat!!, lng!!)
            }


        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            SearchEngineSettings(getString(R.string.mapbox_access_token))
        )
        onMapReady()
        chatId = intent.extras!!.getString(Constants.TAG_CHATID)
        from = intent.extras!!.getString(Constants.TAG_FROM)
        location = intent.extras!!.getString(Constants.TAG_LOCATION) as String?
        lat = intent.extras!!.getDouble(Constants.TAG_LAT)
        lng = intent.extras!!.getDouble(Constants.TAG_LON)
        MapboxNavigationApp.setup {
            NavigationOptions.Builder(this)
                .accessToken(resources.getString(R.string.mapbox_access_token))
                // additional options
                .build()
        }
        mapView = findViewById(R.id.mapView)
        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)
        setLoc = findViewById(R.id.apply) as TextView
        remLoc = findViewById(R.id.reset) as TextView
        mylocation = findViewById(R.id.my_location)
        autocomplete = findViewById<AutoCompleteTextView>(R.id.address)
        bottomLay = findViewById(R.id.bottomLay)
        markerLay = findViewById(R.id.markerLay)
        searchLay = findViewById<View>(R.id.searchLayout) as RelativeLayout
        crossImage = findViewById<View>(R.id.cross_icon) as ImageView
        backBtn = findViewById(R.id.back) as ImageView
        backBtn!!.setOnClickListener(this)
        crossImage!!.setOnClickListener(this)
        setLoc!!.setOnClickListener(this)
        mylocation!!.setOnClickListener(this)
        autocomplete!!.setOnItemClickListener(this)
        remLoc!!.setOnClickListener(this)
        adapter =
            MapboxAutocompleteAdapter(this@MainActivity, R.layout.layout_dropdown, "from", this)
        autocomplete!!.setAdapter(adapter)
        autocomplete!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.length > 0) {
                    crossImage!!.setVisibility(View.VISIBLE)
                } else {
                    crossImage!!.setVisibility(View.GONE)
                }
                handler!!.removeCallbacks(inputFinishChecker)
            }

            override fun afterTextChanged(editable: Editable) {
                lastEdited = System.currentTimeMillis()
                handler!!.postDelayed(inputFinishChecker, TYPING_DELAY)
            }
        })

        autocomplete!!.dropDownWidth = getScreenWidth(this) - dpToPx(this, 30)
        if (from == "add") {
            remLoc!!.visibility = View.GONE
        } else if (from == Constants.TAG_CHAT || from == Constants.TAG_EXCHANGE) {
            searchLay!!.setVisibility(View.GONE)
            setLoc!!.visibility = View.GONE
            remLoc!!.text = getString(R.string.share)
            autocomplete!!.visibility = View.VISIBLE
        } else if (from == Constants.TAG_VIEW) {
            bottomLay!!.setVisibility(View.INVISIBLE)
            markerLay!!.setVisibility(View.GONE)
            searchLay!!.setVisibility(View.GONE)
        } else if (from == "locationtxt") {
            remLoc!!.visibility = View.GONE
        } else if (from == Constants.TAG_PROFILE) {
            remLoc!!.visibility = View.GONE
        } else if (from == "detail") {
            searchLay!!.setVisibility(View.GONE)
            setLoc!!.visibility = View.GONE
            remLoc!!.visibility = GONE
            autocomplete!!.visibility = View.VISIBLE
        }
        if (TextUtils.isEmpty(location)) {
            if (Constants.ADDON_SINGLE_COUNTRY) {
                location = getString(R.string.default_country_name)
            } else {
                if (from != "add") {
                    location = getString(R.string.world_wide)
                }

            }
        }
        if (Constants.ADDON_SINGLE_COUNTRY) {
            if (location != getString(R.string.default_country_name)) {
                isLocationSelected = true
                tempLocation = location
            }
        } else {
            if (location != getString(R.string.world_wide)) {
                isLocationSelected = true
                tempLocation = location
            }

        }
        setupGesturesListener()

    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        mapView?.location
            ?.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView?.location
            ?.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView?.gestures?.removeOnMoveListener(onMoveListener)
    }

    private fun onMapReady() {
        mapView?.getMapboxMap()?.setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
        if (from == "home" || from == Constants.TAG_CHAT || from == Constants.TAG_EXCHANGE || from == Constants.TAG_FILTERS) {
            if (Constants.ADDON_SINGLE_COUNTRY) {
                if (location != getString(R.string.default_country_name)) {
                    autocomplete!!.setText(location)
                }
            } else {
                if (location != getString(R.string.world_wide)) {
                    autocomplete!!.setText(location)
                }
            }
            updateCameraonClick(lat!!, lng!!)
        } else if (from == "add") {
            if (lat == 0.0 && lng == 0.0) {
                Log.d("TAG", "onMapReady: " + latitude + longitude)
                updateCameraonClick(latitude!!, longitude!!)
            } else {
                autocomplete!!.setText(location)
                autocomplete!!.dismissDropDown()
                updateCameraonClick(lat!!, lng!!)
            }
        } else if (from == Constants.TAG_PROFILE) {
            isLocationSelected = true
            autocomplete!!.setText(location)
            autocomplete!!.dismissDropDown()
            updateCameraonClick(lat!!, lng!!)

            mapboxNavigation.startTripSession()
        } else if (from == Constants.TAG_VIEW) {

        }
        if (from == "detail") {
            updateCameraonClick(lat!!, lng!!)
            mapboxNavigation.stopTripSession()
        }
        /* initLocationComponent()*/


    }

    /*   private fun initLocationComponent() {
           val locationComponentPlugin = mapView?.location
           if (locationComponentPlugin != null) {
               locationComponentPlugin.updateSettings {
                   this.enabled = true
                   this.locationPuck = LocationPuck2D(
                       bearingImage = AppCompatResources.getDrawable(
                           this@MainActivity,
                           R.drawable.map,
                       ),
                       shadowImage = AppCompatResources.getDrawable(
                           this@MainActivity,
                           R.drawable.map,
                       ),
                       scaleExpression = interpolate {
                           linear()
                           zoom()
                           stop {
                               literal(0.0)
                               literal(0.6)
                           }
                           stop {
                               literal(20.0)
                               literal(1.0)
                           }
                       }.toJson()
                   )
               }
           }
           if (locationComponentPlugin != null) {
               locationComponentPlugin.addOnIndicatorPositionChangedListener(
                   onIndicatorPositionChangedListener
               )
           }
           if (locationComponentPlugin != null) {
               locationComponentPlugin.addOnIndicatorBearingChangedListener(
                   onIndicatorBearingChangedListener
               )
           }

       }*/

    private fun onCameraTrackingDismissed() {
        mapView?.location
            ?.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView?.location
            ?.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView?.gestures?.removeOnMoveListener(onMoveListener)
    }

    private fun setupGesturesListener() {
        mapView?.gestures?.addOnMoveListener(onMoveListener)
    }

    private fun updateCamera(location: Location) {
        Log.d("", "updateCamera: ")
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
        mapView?.camera?.easeTo(
            CameraOptions.Builder()
                // Centers the camera to the lng/lat specified.
                .center(Point.fromLngLat(location.longitude, location.latitude))
                // specifies the zoom value. Increase or decrease to zoom in or zoom out
                .zoom(15.0)
                // specify frame of reference from the center.
                .padding(EdgeInsets(0.0, 0.0, 0.0, 0.0))
                .build(),
            mapAnimationOptions

        )
        latitude = location.latitude
        longitude = location.longitude
        autocomplete!!.setText(tempLocation)


        if (!place!!) {
            reverseSearch(location.latitude, location.longitude)
        }
    }

    private fun updateCameraonClick(latitude: Double, longitude: Double) {
        Log.d("", "updateCameraonClick: " + latitude + longitude)
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
        mapView?.camera?.easeTo(
            CameraOptions.Builder()
                // Centers the camera to the lng/lat specified.
                .center(Point.fromLngLat(longitude, latitude))
                // specifies the zoom value. Increase or decrease to zoom in or zoom out
                .zoom(15.0)
                // specify frame of reference from the center.
                .padding(EdgeInsets(0.0, 0.0, 0.0, 0.0))
                .build(),
            mapAnimationOptions

        )
        if (mapboxNavigation != null) {
            mapboxNavigation.stopTripSession()
        }


    }

    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerLocationObserver(locationObserver)
                mapboxNavigation.startTripSession()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterLocationObserver(locationObserver)
            }
        },
        onInitialize = this::initNavigation
    )

    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        )
        mapView?.getMapboxMap()?.loadStyleUri(
            Style.MAPBOX_STREETS,
            object : Style.OnStyleLoaded {
                override fun onStyleLoaded(style: Style) {
                    Log.d("TAG", "onStyleLoaded: " + latitude + "" + longitude)
                    initMapListerner()
                    if (latitude != null) {
                        addAnnotationToMap(longitude!!, latitude!!)
                    }

                }
            }
        )
// Instantiate the location component which is the key component to fetch location updates.
        mapView?.location?.apply {
            setLocationProvider(navigationLocationProvider)
            // Uncomment this block of code if you want to see a circular puck with arrow.
            /*locationPuck = LocationPuck2D(
            bearingImage = ContextCompat.getDrawable(
            this@ShowCurrentLocationActivity,
            R.drawable.mapbox_navigation_puck_icon
            )
            )*/
            // When true, the blue circular puck is shown on the map. If set to false, user
            // location in the form of puck will not be shown on the map.
            enabled = true
        }
    }

    private fun addAnnotationToMap(latitude: Double, longitude: Double) {
// Create an instance of the Annotation API and get the PointAnnotationManager.
        bitmapFromDrawableRes(
            this@MainActivity,
            R.drawable.map
        )?.let {
            val annotationApi = mapView?.annotations
            val pointAnnotationManager = annotationApi?.createPointAnnotationManager(mapView!!)
// Set options for the resulting symbol layer.
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
// Define a geographic coordinate.
                .withPoint(Point.fromLngLat(longitude, latitude))
// Specify the bitmap you assigned to the point annotation
// The bitmap will be added to map style automatically.
                .withIconImage(it)
// Add the resulting pointAnnotation to the map.
            pointAnnotationManager?.create(pointAnnotationOptions)
            mapboxNavigation.stopTripSession()
        }
    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
// copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                120, 120,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    public fun initMapListerner() {
        mapView!!.getMapboxMap().addOnMapIdleListener(this@MainActivity)
    }

    override fun onMapIdle(eventData: MapIdleEventData) {
        mapView!!.getMapboxMap().addOnMapIdleListener(OnMapIdleListener { (begin, end) ->

            val point: Point = mapView!!.getMapboxMap().cameraState.getCenter()
            var center = LatLng(point.latitude(), point.longitude())
            var latitude = center.latitude
            var longitude = center.longitude
            if (!isLocationSelected!! && isLocationMove!!) {
                reverseSearch(
                    latitude,
                    longitude
                )


            } else {

            }

            if (autocomplete!!.isShown) {
                autocomplete!!.dismissDropDown()
            }

        })
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.my_location -> {
                isLocationSelected = false
                isLocationMove = true
                if (currentLocation == null && lat == 0.0 && lng == 0.0) {
                    mapboxNavigation.startTripSession()
                } else {
                    if (mapboxNavigation != null) {
                        mapboxNavigation.stopTripSession()
                    }
                    updateCameraonClick(latitude!!, longitude!!)
                    if (autocomplete!!.isShown) {
                        autocomplete!!.dismissDropDown()
                    }
                }
            }
            R.id.apply -> {
                if (!autocomplete!!.text.toString().isEmpty()) {
                    val point: Point = mapView!!.getMapboxMap().cameraState.center
                    center = LatLng(point.latitude(), point.longitude())
                    if (from == "home") {
                        if (autocomplete!!.text.toString().length == 0) {
//                            new GetLocationAsync(center.getLatitude(), center.getLongitude()).execute().get();
//                            makeGeocodeSearch(new LatLng(center.latitude, center.longitude), "");
                            lat = center!!.latitude
                            lng = center!!.longitude
                            locationRemoved = false
                            finish()
                        } else {
                            val latn = arrayOfNulls<Double>(2)
                            lat = center!!.latitude
                            lng = center!!.longitude
                            location =
                                autocomplete!!.getText().toString().trim { it <= ' ' }
                            locationRemoved = false
                            val filter = Intent()
                            filter.putExtra(Constants.TAG_LAT, lat)
                            filter.putExtra(Constants.TAG_LOCATION_REMOVED, true)
                            filter.putExtra(Constants.TAG_LON, lng)
                            Log.d("TAG", "onClick: " + lat + lng)
                            filter.putExtra(Constants.TAG_LOCATION, location)
                            filter.putExtra(
                                Constants.TAG_LOCATION_REMOVED,
                                false
                            )
                            setResult(RESULT_OK, filter)
                            finish()

                        }
                    } else if (from == Constants.TAG_FILTERS) {
                        lat = center!!.latitude
                        lng = center!!.longitude
                        val filter = Intent()
                        filter.putExtra(Constants.TAG_LAT, lat)
                        filter.putExtra(Constants.TAG_LOCATION_REMOVED, true)
                        filter.putExtra(Constants.TAG_LON, lng)
                        filter.putExtra(Constants.TAG_LOCATION, autocomplete!!.text.toString())
                        filter.putExtra(
                            Constants.TAG_LOCATION_REMOVED,
                            false
                        )
                        setResult(RESULT_OK, filter)
                        finish()
                    } else if (from == Constants.TAG_PROFILE) {
                        lat = center!!.latitude
                        lng = center!!.longitude
                        editprofileLat = lat
                        editprofileLng = lng
                        isLocationSelected = true
                        isLocationMove = true
                        reverseSearch(editprofileLat!!, editprofileLng!!)
                        Log.d("", "onClick: " + editprofileLat + editprofileLng)
                    } else if (from.equals("add")) {
                        lat = center!!.latitude
                        lng = center!!.longitude
                        val filter = Intent()
                        filter.putExtra(
                            Constants.TAG_LAT,
                            lat
                        )
                        filter.putExtra(
                            Constants.TAG_LON,
                            lng
                        )
                        filter.putExtra(Constants.TAG_LOCATION, autocomplete!!.text.toString())
                        filter.putExtra(
                            Constants.TAG_LOCATION_REMOVED,
                            true
                        )
                        setResult(RESULT_OK, filter)
                        finish()
                    }
                } else {
                    Toast.makeText(applicationContext,"Enter the location", Toast.LENGTH_SHORT).show()
            }

            }
            R.id.reset -> {
                val point: Point = mapView!!.getMapboxMap().cameraState.center
                center = LatLng(point.latitude(), point.longitude())
                when (from) {
                    "home" -> {
                        if (Constants.ADDON_SINGLE_COUNTRY) {
                            location =
                                getString(R.string.default_country_name)
                        } else {
                            location = getString(R.string.world_wide)
                        }
                        val filter = Intent()
                        filter.putExtra(Constants.TAG_LAT, 0)
                        filter.putExtra(Constants.TAG_LON, 0)
                        filter.putExtra(Constants.TAG_LOCATION, location)
                        filter.putExtra(
                            Constants.TAG_LOCATION_REMOVED,
                            false
                        )
                        setResult(RESULT_OK, filter)
                        finish()
                    }
                    "add" -> {
                        lat = 0.0
                        lng = 0.0
                        location = ""
                        finish()

                    }

                    "filters" -> {
                        lat = 0.0
                        lng = 0.0
                        if (Constants.ADDON_SINGLE_COUNTRY) {
                            location =
                                getString(R.string.default_country_name)
                        } else {
                            location = getString(R.string.world_wide)
                        }
                        locationRemoved = true
                        val filter = Intent()
                        filter.putExtra(
                            Constants.TAG_LAT,
                            lat
                        )
                        filter.putExtra(
                            Constants.TAG_LON,
                            lng
                        )
                        filter.putExtra(
                            Constants.TAG_LOCATION,
                            location
                        )
                        filter.putExtra(
                            Constants.TAG_LOCATION_REMOVED,
                            locationRemoved
                        )
                        setResult(RESULT_OK, filter)
                        finish()
                    }
                    "profile" -> {

                    }
                    "chat" -> {
                        lat = center!!.latitude
                        lng = center!!.longitude
                        remLoc!!.isEnabled = false
                        val filter = Intent()
                        filter.putExtra(Constants.TAG_LAT, lat)
                        filter.putExtra(Constants.TAG_FROM, Constants.TAG_CHAT)
                        filter.putExtra(Constants.TAG_LON, lng)
                        setResult(RESULT_OK, filter)
                        finish()
                    }
                    Constants.TAG_EXCHANGE -> {
                        lat = center!!.latitude
                        lng = center!!.longitude
                        remLoc!!.isEnabled = false
                        val filter = Intent()
                        filter.putExtra(Constants.TAG_LAT, lat)
                        filter.putExtra(Constants.TAG_FROM, Constants.TAG_EXCHANGE)
                        filter.putExtra(Constants.TAG_LON, lng)
                        setResult(RESULT_OK, filter)
                        finish()
                    }

                }


            }
            R.id.back -> {
                finish()
            }
            R.id.cross_icon -> {
                autocomplete!!.text.clear()
                if (mapboxNavigation != null) {
                    mapboxNavigation.stopTripSession()
                }
            }
        }
    }


    override fun onPlaceClick(
        placeAutocomplete: SearchSuggestion?,
        position: Int,
        location: String?
    ) {
        isLocationSelected = true
        Log.d("TAG", "onPlaceClick: " + location.toString())
        place = true
        geocodeAddress(location!!)
        autocomplete!!.setText(location.toString())
        autocomplete!!.dismissDropDown()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        TODO("Not yet implemented")
    }

    private fun getAutoComplete() {
        if (!TextUtils.isEmpty(autocomplete!!.text) && autocomplete!!.text.length > 3) {
            if (lat == 0.0 && lng == 0.0) {
                val options = ReverseGeoOptions(
                    center = Point.fromLngLat(longitude!!, latitude!!),
                    limit = 1
                )
            } else {
                val options = ReverseGeoOptions(
                    center = Point.fromLngLat(lat!!, lng!!),
                    limit = 1
                )
            }

            searchRequestTask = searchEngine.search(
                autocomplete!!.text.toString(),
                SearchOptions(limit = 5),
                searchCallback
            )

        }
    }

    fun dpToPx(context: Context, dp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    fun getScreenWidth(activity: Activity): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            activity.display!!.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    fun getFormattedAddress(item: SearchResult): String? {
        val builder = StringBuilder()
        builder.append(item.name)
        builder.append(", ")
        if (item.address != null && item.address!!.locality != null) {
            builder.append(item.address!!.locality)
            builder.append(", ")
        }
        if (item.address != null && item.address!!.district != null) {
            if (item.address != null && item.address!!.place != null) {
                if (item.address!!.place.equals(item.address!!.district, ignoreCase = true)) {
                    builder.append(item.address!!.place)
                    builder.append(", ")
                } else {
                    builder.append(item.address!!.place)
                    builder.append(", ")
                    builder.append(item.address!!.district)
                    builder.append(", ")
                }
            } else {
                if (!item.name.equals(item.address!!.district, ignoreCase = true)) {
                    builder.append(item.address!!.district)
                    builder.append(", ")
                }
            }
        }
        if (item.address != null && item.address!!.region != null) {
            builder.append(item.address!!.region)
            builder.append(", ")
        }
        if (item.address != null && item.address!!.country != null) {
            builder.append(item.address!!.country)
        }
        return builder.toString()
    }

    public fun reverseSearch(latitude: Double, longitude: Double) {

            var executor:Executor=Executors.newSingleThreadExecutor()
            searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(ApiType.GEOCODING,
                SearchEngineSettings("pk.eyJ1IjoicGhvbmZhbmF0aXgiLCJhIjoiY2w2ejNsZTEwMDBsbzNxcWdmMTJ4djYzMiJ9.T2-gSQyVMTGZiNJppZTQcg"),
                executor, object : CompletionCallback<Unit>{
                    override fun onComplete(result: Unit) {
                        reverseEngine(latitude, longitude)
                    }

                    override fun onError(e: Exception) {

                    }

                }
            )

        //place = false

    }
    public fun reverseSearchforAddproduct(latitude: Double, longitude: Double,executor:Executor) {

        // var executor:Executor=Executors.newSingleThreadExecutor()
        searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(ApiType.GEOCODING,
            SearchEngineSettings("pk.eyJ1IjoicGhvbmZhbmF0aXgiLCJhIjoiY2w2ejNsZTEwMDBsbzNxcWdmMTJ4djYzMiJ9.T2-gSQyVMTGZiNJppZTQcg"),
            executor, object : CompletionCallback<Unit>{
                override fun onComplete(result: Unit) {
                    reverseEngineforAddproduct(latitude, longitude)
                }

                override fun onError(e: Exception) {

                }

            }
        )

        //place = false

    }

    val reverseSearchCallback = object : SearchCallback{
        override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
            if (results.isEmpty()) {
                Log.i(
                    "TAG",
                    "No reverse geocoding results"
                )
            } else {
                tempLocation = getFormattedAddress(results.get(0))
                if (tempLocation !=null) {
                    autocomplete!!.setText(tempLocation)
                }

                if (autocomplete!!.isShown) {
                    autocomplete!!.dismissDropDown()
                }
                if (from.equals(Constants.TAG_PROFILE)) {
                    if (isLocationSelected!!.equals(true)) {
                        val profile = Intent()
                        var countryName: String? = null
                        var stateName: String? = null
                        var cityName: String? = null
                        if (results.get(0).address != null) {
                            cityName = results.get(0).address!!.district
                            stateName = results.get(0).address!!.region
                            countryName = results.get(0).address!!.country
                            Log.d(
                                "TAG",
                                "onResults: " + results.get(0).address!!.district + results.get(0).address!!.region + results.get(
                                    0
                                ).address!!.country
                            )
                        }
                        profile.putExtra(
                            Constants.TAG_CITY_NAME,
                            cityName
                        )
                        profile.putExtra(
                            Constants.TAG_STATE_NAME,
                            stateName
                        )
                        profile.putExtra(
                            Constants.TAG_COUNTRY_NAME,
                            countryName
                        )
                        profile.putExtra(
                            Constants.TAG_LOCATION,
                            "" + cityName + ", " + stateName + ", " + countryName
                        )
                        profile.putExtra(Constants.TAG_LAT, editprofileLat)
                        profile.putExtra(Constants.TAG_LON, editprofileLng)
                        Log.d("", "onResults: " + editprofileLat)
                        setResult(RESULT_OK, profile)
                        finish()
                    }

                }
            }
            if (mapboxNavigation != null && !mapboxNavigation.isDestroyed) {
                mapboxNavigation.stopTripSession()
            }
            isLocationSelected = false
        }

        override fun onError(e: Exception) {
        }

    }

    fun geocodeAddress(location: String) {
        /*mapboxGeocoding= MapboxGeocoding.builder()
       .accessToken(getString(R.string.mapbox_access_token))
       .query(location!!)
       .limit(1)
       .build()*/
        var mapboxGeocoding: MapboxGeocoding = MapboxGeocoding.builder()
            .accessToken(getString(R.string.mapbox_access_token))
            .query(location)
            .build()
        // Execute the forward geocoding request
        // Execute the forward geocoding request
        mapboxGeocoding.enqueueCall(
            object : Callback<GeocodingResponse> {
                override fun onResponse(
                    call: Call<GeocodingResponse>,
                    response: Response<GeocodingResponse>
                ) {
                    if (response.isSuccessful() && response.body()!!.features().size > 0) {
                        val feature: CarmenFeature = response.body()!!.features().get(0)
                        val geometry: Geometry = feature.geometry()!!
                        val geometryString = geometry.toJson()
                        try {
                            val geometryJson = JSONObject(geometryString)
                            val coordinates = geometryJson.getJSONArray("coordinates")
                            autocompleteLongitude = coordinates.getDouble(0)
                            autocompleteLatitude = coordinates.getDouble(1)
                            if (from == Constants.TAG_PROFILE) {
                                editprofileLat = autocompleteLatitude
                                editprofileLng = autocompleteLongitude
                            }
                            updateCameraonClick(
                                autocompleteLatitude!!,
                                autocompleteLongitude!!
                            )

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            Log.d("", "onMoveBegin: ")
            isLocationMove = true
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            Log.d("", "onMove: ")
            isLocationMove = true
            place = false
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {
            reverseSearch(lat!!, lng!!)
            isLocationSelected = false
            //onCameraTrackingDismissed()
        }
    }
    /*public fun searchEngine(latitude: Double, longitude: Double){
        var executor:Executor=Executors.newSingleThreadExecutor()
        searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(ApiType.GEOCODING,
            SearchEngineSettings("pk.eyJ1IjoicGhvbmZhbmF0aXgiLCJhIjoiY2w2ejNsZTEwMDBsbzNxcWdmMTJ4djYzMiJ9.T2-gSQyVMTGZiNJppZTQcg"),
            executor, object : CompletionCallback<Unit>{
                override fun onComplete(result: Unit) {
                    reverseEngine(latitude, longitude)
                }

                override fun onError(e: Exception) {

                }

            }
        )
    }
    public fun searchEngineforAddproduct(latitude: Double, longitude: Double){
        var executor:Executor=Executors.newSingleThreadExecutor()
        searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(ApiType.GEOCODING,
            SearchEngineSettings("pk.eyJ1IjoicGhvbmZhbmF0aXgiLCJhIjoiY2w2ejNsZTEwMDBsbzNxcWdmMTJ4djYzMiJ9.T2-gSQyVMTGZiNJppZTQcg"),
            executor, object : CompletionCallback<Unit>{
                override fun onComplete(result: Unit) {
                    reverseEngineforAddproduct(latitude, longitude)
                }

                override fun onError(e: Exception) {

                }

            }
        )
    }*/
    public fun reverseEngine(latitude: Double, longitude: Double){
        val task = searchEngine.search(
            ReverseGeoOptions(
                center = Point.fromLngLat(longitude, latitude)
            ),
            reverseSearchCallback
        )
    }
    public fun reverseEngineforAddproduct(latitude: Double, longitude: Double){
        val task = searchEngine.search(
            ReverseGeoOptions(
                center = Point.fromLngLat(longitude, latitude)
            ),
            reverseSearchCallbackforAddproduct
        )
    }
    val reverseSearchCallbackforAddproduct = object : SearchCallback{
        override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
            if (results.isEmpty()) {
                Log.i(
                    "TAG",
                    "No reverse geocoding results"
                )
            } else {
                tempLocation = getFormattedAddress(results.get(0))
                Log.d("TAG", "onResults: "+tempLocation)
            }
        }

        override fun onError(e: Exception) {
        }

    }
}
