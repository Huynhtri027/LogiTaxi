package mbond.travelprofile.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import mbond.travelprofile.Adapter.AutoCompleteAdapter;
import mbond.travelprofile.AppController;
import mbond.travelprofile.DataModel.PlacePredictions;
import mbond.travelprofile.R;
import mbond.travelprofile.Util.VolleyJSONRequest;


public class PickLocationActivity extends AppCompatActivity implements Response.Listener<String>, Response.ErrorListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    protected static final int REQUEST_CHECK_SETTINGS = 0x5;
    private static final int MY_PERMISSIONS_REQUEST_LOC = 30;
    private static final int TIME_INTERVAL = 3000;
    public static ProgressDialog progressDialog;
    double latitude;
    double longitude;
    TextView backButton;
    boolean check = true;
    Button myLocation;
    TextView orText;
    LocationRequest locationRequest;
    String city = "";
    CircleProgressBar circleProgress;
    private ListView mAutoCompleteList;
    private EditText Address;
    private String GETPLACESHIT = "places_hit";
    private PlacePredictions predictions;
    private Location mLastLocation;
    private AutoCompleteAdapter mAutoCompleteAdapter;
    private int CUSTOM_AUTOCOMPLETE_REQUEST_CODE = 20;
    private ImageView searchBtn;
    private FragmentManager fragmentManager;
    private String preFilledText;
    private Handler handler;
    private VolleyJSONRequest request;
    private GoogleApiClient mGoogleApiClient;
    private ProgressBar progressBar;
    private int USER_LOCATION = 15;
    private long time = 0;
    private String TAG = "PICKLOCATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick_location);
        if (getIntent().hasExtra("Search Text")) {
            preFilledText = getIntent().getStringExtra("Search Text");
        }

        USER_LOCATION = getIntent().getIntExtra("STATUS", 15);
        fragmentManager = getSupportFragmentManager();
        Address = (EditText) findViewById(R.id.adressText);
        mAutoCompleteList = (ListView) findViewById(R.id.searchResultLV);
        searchBtn = (ImageView) findViewById(R.id.search);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        orText = (TextView) findViewById(R.id.or_text);
        myLocation = (Button) findViewById(R.id.my_location);
        circleProgress = (CircleProgressBar) findViewById(R.id.circle_progress);
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                settingsrequest();
            }
        });

        progressDialog = new ProgressDialog(this) {
            @Override
            public void onBackPressed() {
                if (progressDialog.isShowing()) {
                    if (time + TIME_INTERVAL > System.currentTimeMillis()) {
                        progressDialog.dismiss();
                        super.onBackPressed();
                        return;
                    } else {
                        Toast.makeText(getBaseContext(), "Please wait.. or Press again to cancel the ongoing request.", Toast.LENGTH_SHORT).show();
                    }
                    time = System.currentTimeMillis();
                }
            }
        };
        progressDialog.setMessage("Getting Location ...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        backButton = (TextView) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if (check) {
            orText.setVisibility(View.VISIBLE);
            myLocation.setVisibility(View.VISIBLE);
            backButton.setText("Update Current Location");
        }

        //get permission for Android M
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            fetchLocation();
        } else {

            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOC);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            } else {
                fetchLocation();
            }
        }


        //Add a text change listener to implement autocomplete functionality
        Address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // optimised way is to start searching for laction after user has typed minimum 3 chars
                if (Address.getText().length() > 3) {
                    searchBtn.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    Runnable run = new Runnable() {

                        @Override
                        public void run() {

                            // cancel all the previous requests in the queue to optimise your network calls during autocomplete search
                            AppController.volleyQueueInstance.cancelRequestInQueue(GETPLACESHIT);

                            //build Get url of Place Autocomplete and hit the url to fetch result.
                            request = new VolleyJSONRequest(Request.Method.GET, getPlaceAutoCompleteUrl(Address.getText().toString()), null, null, PickLocationActivity.this, PickLocationActivity.this);

                            //Give a tag to your request so that you can use this tag to cancle request later.
                            request.setTag(GETPLACESHIT);

                            AppController.volleyQueueInstance.addToRequestQueue(request);

                        }

                    };

                    // only canceling the network calls will not help, you need to remove all callbacks as well
                    // otherwise the pending callbacks and messages will again invoke the handler and will send the request
                    if (handler != null) {
                        handler.removeCallbacksAndMessages(null);
                    } else {
                        handler = new Handler();
                    }
                    handler.postDelayed(run, 1000);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });

        Address.setText(preFilledText);
        Address.setSelection(Address.getText().length());

        mAutoCompleteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                circleProgress.setVisibility(View.VISIBLE);
                hideKeyboard(view);
                // pass the result to the calling activity
                // pass the result to the calling activity
                Places.GeoDataApi.getPlaceById(mGoogleApiClient, predictions.getPlaces().get(0).getPlaceID())
                        .setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(PlaceBuffer places) {
                                if (places.getStatus().isSuccess()) {
                                    final Place myPlace = places.get(0);
                                    LatLng queriedLocation = myPlace.getLatLng();
                                    Log.d(TAG, "onResult: lat : " + queriedLocation.latitude + "  lng: " + queriedLocation.longitude);
                                    Intent intent = new Intent();
                                    intent.putExtra("Location Address", predictions.getPlaces().get(position).getPlaceDesc());
                                    intent.putExtra("lat", queriedLocation.latitude);
                                    intent.putExtra("lng", queriedLocation.longitude);
                                    if (check) {
                                        int size = predictions.getPlaces().get(0).getTerms().size();
                                        String cityName = predictions.getPlaces().get(0).getTerms().get(size - 3).getValue();
                                        Log.d(TAG, "onResult City Name: " + cityName);
                                        intent.putExtra("city", cityName);
                                        setResult(USER_LOCATION, intent);
                                    } else {
                                        setResult(CUSTOM_AUTOCOMPLETE_REQUEST_CODE, intent);
                                    }
                                    circleProgress.setVisibility(View.INVISIBLE);
                                    finish();
                                }
                                places.release();
                            }
                        });
            }
        });

    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /*
        * Create a get url to fetch results from google place autocomplete api.
        * Append the input received from autocomplete edittext
        * Append your current location
        * Append radius you want to search results within
        * Choose a language you want to fetch data in
        * Append your google API Browser key
     */
    public String getPlaceAutoCompleteUrl(String input) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/place/autocomplete/json");
        urlString.append("?input=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&location=");
        urlString.append(latitude + "," + longitude); // append lat long of current location to show nearby results.
        urlString.append("&radius=1000&language=en");
        urlString.append("&key=" + "AIzaSyCX15n39sOpK9-bvLfwfYieFQnJSkbfFzI");

        Log.d("FINAL URL:::   ", urlString.toString());
        return urlString.toString();
    }

    @Override
    public void onErrorResponse(VolleyError error) {

        searchBtn.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

    }

    @Override
    public void onResponse(String response) {

        searchBtn.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        Log.d("PLACES RESULT:::", response);
        Gson gson = new Gson();
        predictions = gson.fromJson(response, PlacePredictions.class);
        if (predictions.getPlaces().size() > 0) {
            mAutoCompleteList.setVisibility(View.VISIBLE);
            if (check) {
                orText.setVisibility(View.GONE);
                myLocation.setVisibility(View.GONE);
            }
        } else {
            mAutoCompleteList.setVisibility(View.GONE);
            if (check) {
                orText.setVisibility(View.VISIBLE);
                myLocation.setVisibility(View.VISIBLE);
            }
        }
        if (mAutoCompleteAdapter == null) {
            mAutoCompleteAdapter = new AutoCompleteAdapter(this, predictions.getPlaces(), PickLocationActivity.this);
            mAutoCompleteList.setAdapter(mAutoCompleteAdapter);
        } else {
            mAutoCompleteAdapter.clear();
            mAutoCompleteAdapter.addAll(predictions.getPlaces());
            mAutoCompleteAdapter.notifyDataSetChanged();
            mAutoCompleteList.invalidate();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void fetchLocation() {
        //Build google API client to use fused location
        buildGoogleApiClient();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission granted!
                    fetchLocation();

                } else {
                    // permission denied!

                    Toast.makeText(this, "Please grant permission for using this app!", Toast.LENGTH_LONG).show();
                }
                return;
            }


        }
    }

    private String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<android.location.Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                android.location.Address address = addresses.get(0);
                int max = address.getMaxAddressLineIndex();
                for (int i = 0; i < max; i++) {
                    if (i == 0)
                        result.append(address.getAddressLine(i));
                    else if (i == max - 1)
                        result.append(", " + address.getAddressLine(i).substring(0, address.getAddressLine(i).length() - 7));
                    else
                        result.append(", " + address.getAddressLine(i));

                }
                Log.d(TAG, "getAddress: " + address.toString() + "\n");
                if (address.getLocality() != null) {
                    city = address.getLocality();

                }


            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }

    public void settingsrequest() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

            locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setNumUpdates(1);
            locationRequest.setExpirationDuration(20000);
            locationRequest.setFastestInterval(500);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            builder.setAlwaysShow(true); //this is the key ingredient
            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS: {
                            setLocation();
                        }
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(PickLocationActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        } else {
            if (mGoogleApiClient == null)
                buildGoogleApiClient();
            else if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
// Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        setLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        if (progressDialog != null) {
                            progressDialog.cancel();
                        }

                        //settingsrequest();//keep asking if imp or do whatever
                        break;
                }
                break;
        }
    }

    public void setLocation() {
        SmartLocation.with(this).location().oneFix().start(new OnLocationUpdatedListener() {
            @Override
            public void onLocationUpdated(Location location) {
                if (location == null) {
                    settingsrequest();
                    return;
                }
                if (progressDialog != null) {
                    progressDialog.cancel();
                }
                String address = getAddress(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                Intent intent = new Intent();
                intent.putExtra("Location Address", address);
                intent.putExtra("lat", mLastLocation.getLatitude());
                intent.putExtra("lng", mLastLocation.getLongitude());
                intent.putExtra("city", city);
                setResult(USER_LOCATION, intent);
                Log.d(TAG, "onLocationUpdated: address ; " + address + "   city: " + city + "   lat : " + latitude + "   long: " + longitude);
                finish();
                //  overridePendingTransition(R.anim.abc_slide_in_top, R.anim.abc_slide_out_bottom);
                SmartLocation.with(PickLocationActivity.this).location().stop();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.abc_slide_in_top, R.anim.abc_slide_out_bottom);
    }
}

