package project.ssing.myapplication;

import android.Manifest;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.
/**
 * Created by DE on 2017-08-25.
 */

public class MapActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
                    GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String CAMERA_POSITION = "camera_position";
    private static final String LOCATION = "location";
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private CameraPosition mCameraPosition;
    private GoogleMap  map;
    private Location mCurrentLocation;
    private boolean mLocationPermissionGranted;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCurrentLocation = savedInstanceState.getParcelable(LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(CAMERA_POSITION);
        }
        setContentView(R.layout.activity_map);

        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getDeviceLocation();

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        /*LatLng SEOUL = new LatLng(37.56, 126.97);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title("서울");
        markerOptions.snippet("한국의 수도");
        map.addMarker(markerOptions);

        map.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        map.animateCamera(CameraUpdateFactory.zoomTo(10));*/
        map = googleMap;
        updateLocationUI();

        if (mCameraPosition != null) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mCurrentLocation != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mCurrentLocation.getLatitude(),
                            mCurrentLocation.getLongitude()), 16));
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(25.3, 34.3), 16));
            map.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        createLocationRequest();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        if (requestCode == PermissionUtil.REQUEST_LOCATION) {
            if (PermissionUtil.verifyPermission(grantResults)) {
                mLocationPermissionGranted = true;
            } else {
                showRequestAgainDialog();
            }
        }
        updateLocationUI();
    }

    @SuppressWarnings("MissingPermission")
    private void updateLocationUI() {
        if (map == null) return;
        if (mLocationPermissionGranted) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            map.setMyLocationEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    @SuppressWarnings("MissingPermission")
    private void getDeviceLocation() {
        if (PermissionUtil.checkPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            mLocationPermissionGranted = true;
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this
            );
        } else PermissionUtil.requestFineLocationPermissions(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (map != null) {
            outState.putParcelable(CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(LOCATION, mCurrentLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    protected void onResume() {
        if (mGoogleApiClient.isConnected()) {
            getDeviceLocation();
        }
        super.onResume();
    }
    public abstract class PermissionUtil {

        public static boolean verifyPermissions(int[] grantResults) {
            // At least one result must be checked.
            if(grantResults.length < 1){
                return false;
            }

            // Verify that each required permission has been granted, otherwise return false.
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }

    }
}
