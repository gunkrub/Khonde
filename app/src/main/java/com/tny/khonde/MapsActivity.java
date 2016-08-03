package com.tny.khonde;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback , GoogleMap.OnMapLongClickListener,GoogleApiClient.OnConnectionFailedListener{

    private GoogleMap mMap;
    protected GoogleApiClient mGoogleApiClient;

    private PlaceAutocompleteAdapter mAdapter;

    private AutoCompleteLoading mAutocompleteView;

    private TextView mPlaceDetailsText;

    private TextView mPlaceDetailsAttribution;

    LatLng currentPosition;

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(4.502943, 93.525881), new LatLng(23.466173, 108.027833));
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        int requestCode = intent.getIntExtra("requestCode",0);
        TextView tv1 = (TextView) findViewById(R.id.topbar_label);

        switch(requestCode){
            case 2001:
                tv1.setText("เลือกสถานที่ส่งของ");
                break;
            case 2002:
                tv1.setText("เลือกสถานที่แวะ");
                break;
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.location_map);
        mapFragment.getMapAsync(this);

        View myLocationButton =  ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) myLocationButton.getLayoutParams();
        //params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);

        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        // Update margins, set to 10dp
        final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                getResources().getDisplayMetrics());
        params.setMargins(margin, margin, margin, margin);

        myLocationButton.setLayoutParams(params);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();
        mAutocompleteView = (AutoCompleteLoading)
                findViewById(R.id.autocomplete_places);
        mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);


        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.
            mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY,
                null);
        mAutocompleteView.setAdapter(mAdapter);
        mAutocompleteView.setLoadingIndicator((ProgressBar)findViewById(R.id.locationLoading));

    }


    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            findViewById(R.id.locationLoading).setVisibility(View.VISIBLE);

            AutoCompleteTextView typingView = (AutoCompleteTextView)findViewById(R.id.autocomplete_places);
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(typingView.getWindowToken(), 0);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

        }
    };

    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e("MAPS", "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);
            LatLng point =  place.getLatLng();

            // Format details of the place for display and show it in a TextView.
            //((TextView)findViewById(R.id.textView)).setText(point.latitude + ","+ point.longitude);
            moveCameraTo(point.latitude,point.longitude);

            Log.i("MAPS", "Place details received: " + place.getName());
            places.release();
        }
    };

    public void moveCameraTo(double lat1, double long1){
        findViewById(R.id.locationLoading).setVisibility(View.GONE);
        CameraPosition position = new CameraPosition.Builder().target(new LatLng(lat1, long1)).zoom((float) 16).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e("MAPS", "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.location_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 1337);
            return;
        }
        mMap.setMyLocationEnabled(true);

        LocationManager a = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location a1 = a.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Intent intent = getIntent();
        double last_lat = intent.getDoubleExtra("lat",a1.getLatitude());
        double last_long = intent.getDoubleExtra("long",a1.getLongitude());

        CameraPosition position = new CameraPosition.Builder().target(new LatLng(last_lat,last_long)).zoom((float) 16).build();
        this.mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
        //mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.setOnMapLongClickListener(this);

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                Log.e("CURRENT POSITION",position.target.latitude + "," + position.target.longitude);
                currentPosition = position.target;
                //((TextView)findViewById(R.id.textView)).setText(position.target.latitude + "," + position.target.longitude);
                ((ImageView)findViewById(R.id.marker_icon)).setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public void onMapLongClick(LatLng latLng) {
/*
        ((TextView)findViewById(R.id.textView)).setText(latLng.latitude + "," + latLng.longitude);

        LatLng point = new LatLng(latLng.latitude,latLng.longitude);
        mMap.clear();
        Marker current_marker = mMap.addMarker(new MarkerOptions()
                                .position(point));
*/
    }

    public void clearSearch(View v){
        ((AutoCompleteTextView)findViewById(R.id.autocomplete_places)).setText("");
    }

    public void selectLocation(View v){
        Intent res = new Intent();
        Tab2.startPoint = currentPosition;
        Intent i1 = getIntent();
        int index1 = i1.getIntExtra("index1",0);
        res.putExtra("index1",index1);
        setResult(RESULT_OK,res);
        finish();
    }

}
