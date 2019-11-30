package com.a60n1.ejashkojme;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.a60n1.ejashkojme.common.Common;
import com.a60n1.ejashkojme.remote.IGoogleAPI;
import com.a60n1.ejashkojme.utils.PermissionUtils;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings({"deprecation", "FieldCanBeLocal"})
public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "DriverMapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;
    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;
    DatabaseReference mDriversDatabase, mUserPostsDatabase;
    GeoFire geoFire;
    Marker mCurrent;
    SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private String mRiderId;
    private List<LatLng> polyLineList;
    private LatLng currentPosition;
    private Button mGoButton;
    private AutoCompleteTextView mSearchPickText;
    private TextView mPickupName;
    private CircleImageView mRiderAvatar;
    @SuppressWarnings("unused")
    private GeoDataClient mGeoDataClient;

    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyline;
    private IGoogleAPI mService;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        mRiderId = getIntent().getStringExtra("user_id");
        final String name = getIntent().getStringExtra("user_name");
        final String thumb_image = getIntent().getStringExtra("thumb_image");
        String origin = getIntent().getStringExtra("origin");

        mSearchPickText = findViewById(R.id.pick_text);
        mPickupName = findViewById(R.id.driver_pickup_name);
        mRiderAvatar = findViewById(R.id.driver_pickup_photo);

        if (name != null && thumb_image != null) {
            mPickupName.setText("Pickup " + name);
            if (!thumb_image.equals("default")) {
                Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.default_avatar).into(mRiderAvatar, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(mRiderAvatar);
                    }
                });
            } else {
                Picasso.get().load(thumb_image).placeholder(R.drawable.default_avatar).into(mRiderAvatar);
            }
        } else {
            CardView cardView = findViewById(R.id.driver_pickup_view);
            cardView.setVisibility(View.GONE);
        }

        mUserPostsDatabase = FirebaseDatabase.getInstance().getReference().child("user-posts");
        if (mRiderId != null) {
            mSearchPickText.setText(origin);
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mGeoDataClient = Places.getGeoDataClient(this, null);
        // Set up the adapter that will retrieve suggestions from the Places Geo Data Client.
        // mAdapter = new PlaceAutocompleteAdapter(this, mGeoDataClient, null, null);
        // mSearchPickText.setAdapter(mAdapter);
        polyLineList = new ArrayList<>();
        mGoButton = findViewById(R.id.btn_go);

        mGoButton.setOnClickListener(v -> {
            String destination = mSearchPickText.getText().toString();
            Log.d(TAG, destination);
            hideKeyboard();
            getDirection(destination);
        });

        //geo Fire
        mDriversDatabase = FirebaseDatabase.getInstance().getReference("drivers");
        geoFire = new GeoFire(mDriversDatabase);
        setUpLocation();

        mService = Common.getGoogleAPI();
        createLocationCallback();
        startLocationUpdate();
        displayLocation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdate();
        if (mCurrent != null) {
            mCurrent.remove();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdate();
        displayLocation();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DriverMapActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void hideKeyboard() {
        if (getCurrentFocus() != null) {
            // hides the keyboard if applicable
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            Objects.requireNonNull(inputManager).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void getDirection(String destination) {
        if (mLastLocation == null) {
            return;
        }
        currentPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

        String requestApi;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude + "&" +
                    "destination=" + destination + "&" +
                    "key=" + getResources().getString(R.string.google_maps_key);
            Log.d(TAG, requestApi); //print URL for debugging
            Objects.requireNonNull(mService.getPath(requestApi))
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {

                            try {
                                JSONObject jsonObject = new JSONObject(Objects.requireNonNull(response.body()));
                                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    //noinspection unchecked
                                    polyLineList = decodePoly(polyline);
                                }
                                //Adjusting bounds
                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                for (LatLng latLng : polyLineList)
                                    builder.include(latLng);
                                LatLngBounds bounds = builder.build();
                                CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
                                mMap.animateCamera(mCameraUpdate);

                                polylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.GRAY);
                                polylineOptions.width(5);
                                polylineOptions.startCap(new SquareCap());
                                polylineOptions.endCap(new SquareCap());
                                polylineOptions.jointType(JointType.ROUND);
                                polylineOptions.addAll(polyLineList);
                                greyPolyline = mMap.addPolyline(polylineOptions);

                                blackPolylineOptions = new PolylineOptions();
                                blackPolylineOptions.color(Color.BLACK);
                                blackPolylineOptions.width(5);
                                blackPolylineOptions.startCap(new SquareCap());
                                blackPolylineOptions.endCap(new SquareCap());
                                blackPolylineOptions.jointType(JointType.ROUND);
                                blackPolylineOptions.addAll(polyLineList);
                                blackPolyline = mMap.addPolyline(blackPolylineOptions);

                                mMap.addMarker(new MarkerOptions()
                                        .position(polyLineList.get(polyLineList.size() - 1))
                                        .title("Pickup Here"));

                                //Animation
                                ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0, 100);
                                polyLineAnimator.setDuration(2000);
                                polyLineAnimator.setInterpolator(new LinearInterpolator());
                                polyLineAnimator.addUpdateListener(valueAnimator -> {
                                    List<LatLng> pointns = greyPolyline.getPoints();
                                    int percemtValue = (int) valueAnimator.getAnimatedValue();
                                    int size = pointns.size();
                                    int newPoints = (int) (size * (percemtValue / 100.0f));
                                    List<LatLng> p = pointns.subList(0, newPoints);
                                    blackPolyline.setPoints(p);
                                });

                                polyLineAnimator.start();

                                //noinspection unused
                                Marker carMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                                        .flat(true)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            Toast.makeText(DriverMapActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            //noinspection unchecked
            poly.add(p);
        }

        return poly;
    }

    private void setUpLocation() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            if (checkPlayService()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private boolean checkPlayService() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RES_REQUEST).show();
            else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void stopLocationUpdate() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    private void displayLocation() {
        if (mLastLocation != null) {
            final double latitude = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();
            //Update to Firebase
            geoFire.setLocation(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), new GeoLocation(latitude, longitude), (key, error) -> {
                if (mCurrent != null) {
                    mCurrent.remove();
                }
                mCurrent = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .title("Your Location"));
                //Move camera to this position
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));

            });
        } else {
            Log.d("Error", "Cannot get your location");
        }
    }

    private void startLocationUpdate() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }


    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mLastLocation = locationResult.getLastLocation();
                displayLocation();
            }
        };
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }  // Show rationale and request permission.

    }

    @Override
    public boolean onMyLocationButtonClick() {
//        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
//        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdate();
        displayLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }


}