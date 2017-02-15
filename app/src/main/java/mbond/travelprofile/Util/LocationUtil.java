package mbond.travelprofile.Util;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mbond.travelprofile.AppController;
import mbond.travelprofile.R;

import static android.app.Activity.RESULT_CANCELED;
import static android.view.View.GONE;

/**
 * Created by AMAN on 23/11/16.
 */

public class LocationUtil implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final int REQUEST_CHECK_SETTINGS = 0;
    public static final int REQUEST_ID_PERMISSION = 1;
    private static final String TAG = "LocationUtil";
    private static final int TIME_INTERVAL = 3000;
    private static final int FIVE_SECONDS = 1000 * 5;
    public GoogleApiClient mGoogleApiClient;
    public boolean isLocationEnabled;
    public boolean logoutRequest;
    RelativeLayout enableLocationLayout;
    Button enableLocationButton;
    LocationRequest locationRequest;
    TextView enableLocationDescription;
    boolean requestToStopLocationUpdate = false;
    Location currentBestLocation;
    boolean firstTime = true;
    private long time = 0;
    private Activity mActivity;
    private OnLocationUtilListener onLocationUtilListener;
    private ProgressDialog progressDialog;
    private boolean showDialogs;

    public LocationUtil(Activity mActivity, RelativeLayout enableLocationLayout, Button enableLocationButton, TextView enableLocationDescription) {
        this.enableLocationDescription = enableLocationDescription;
        this.mActivity = mActivity;
        this.enableLocationButton = enableLocationButton;
        this.enableLocationLayout = enableLocationLayout;
        CheckAndRequestPermissions();
    }

    public void removeLocationUtilListener() {
        this.onLocationUtilListener = null;
    }

    public void setOnLocationUtilListener(OnLocationUtilListener onLocationUtilListener) {
        this.onLocationUtilListener = onLocationUtilListener;
    }

    public synchronized void buildGoogleApiClient(boolean showDialogs) {
        this.showDialogs = showDialogs;
        //mCircularProgressBar.setVisibility(View.VISIBLE);
        if (AppController.isOnline()) {
            if (mGoogleApiClient == null) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(new Scope(Scopes.PROFILE))
                        .requestScopes(new Scope(Scopes.PLUS_ME))
                        .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                        .requestEmail()
                        .build();
                mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API).build();
                mGoogleApiClient.connect();
            } else {
                mGoogleApiClient.connect();
            }
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        if (requestToStopLocationUpdate) {
            stopLocationUpdates();
            return;
        }
        GetCurrentLocation(TIME_INTERVAL, showDialogs);
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mGoogleApiClient == null) {
            buildGoogleApiClient(showDialogs);
            return;
        } else if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            return;
        }
        Log.d(TAG, "onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: ");
    }

    public void GetCurrentLocation() {
        locationSettingsRequest(5000, showDialogs);
    }

    public void GetCurrentLocation(int interval, boolean showDialogs) {
        if (mGoogleApiClient == null) {
            buildGoogleApiClient(showDialogs);
            return;
        } else if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            return;
        }
        this.showDialogs = showDialogs;
        locationSettingsRequest(interval, showDialogs);


    }

    public void locationSettingsRequest(int interval, boolean showDialogs) {
        if (!showDialogs)
            initializeProgressDialog();
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(5000);
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
                        startLocationUpdates();
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
                            status.startResolutionForResult(mActivity, REQUEST_CHECK_SETTINGS);
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
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            CheckAndRequestPermissions();
            return;
        }
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(enableLocationLayout, "alpha", 1.0f, 0);
        objectAnimator.setDuration(500);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.start();
        enableLocationLayout.setVisibility(GONE);
        enableLocationButton.setVisibility(GONE);

        if (mGoogleApiClient == null) {
            buildGoogleApiClient(showDialogs);
            return;
        } else if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            return;
        }
        if (onLocationUtilListener != null) {
            onLocationUtilListener.onLocationEnabled();
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }

    public void stopLocationUpdates() {
        requestToStopLocationUpdate = true;
        if (mGoogleApiClient == null) {
            buildGoogleApiClient(showDialogs);
            return;
        } else if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        requestToStopLocationUpdate = false;
    }

    private void initializeProgressDialog() {
        progressDialog = new ProgressDialog(mActivity) {
            @Override
            public void onBackPressed() {
                if (progressDialog.isShowing()) {
                    if (time + TIME_INTERVAL > System.currentTimeMillis()) {
                        progressDialog.dismiss();
                        super.onBackPressed();
                        return;
                    } else {
                        AppController.showToast("Please wait.. or Press again to cancel the ongoing request.");
                    }
                    time = System.currentTimeMillis();

                }
            }
        };
        progressDialog.setMessage("Fetching Location ...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.onBackPressed();
        progressDialog.show();

    }


    public void onRequestPermissionsResult(String[] permissions, int[] grantResults) {
        Map<String, Integer> perms = new HashMap<>();
        // Initialize the map with both permissions
        perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
        // Fill with actual results from user
        if (grantResults.length > 0) {
            for (int i = 0; i < permissions.length; i++)
                perms.put(permissions[i], grantResults[i]);
            // Check for both permissions


            if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "location services permission granted");

                buildGoogleApiClient(showDialogs);
                // process the normal flow
                //else any one or both the permissions are not granted
            } else {
                Log.d(TAG, "Some permissions are not granted ask again ");
                //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showDialogOK();
                }
                //permission is denied (and never ask again is  checked)
                //shouldShowRequestPermissionRationale will return false
                else {
                    enableLocationDescription.setText("This feature requires your location.\nPlease enable location services.");
                    enableLocationLayout.setVisibility(View.VISIBLE);
                    enableLocationButton.setVisibility(View.VISIBLE);
                    enableLocationButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDialogforSettings();
                        }
                    });
                    //proceed with logic by disabling the related features or quit the app.
                }
            }
        }
    }

    private void showDialogforSettings() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        AlertDialog alert;
        builder.setMessage("App needs location permission to access this feature. It can be enabled under Phone Settings > LogiTaxi > Permissions");
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                enableLocationDescription.setText("This feature requires your location.\nPlease enable location services.");
                enableLocationLayout.setVisibility(View.VISIBLE);
                enableLocationButton.setVisibility(View.VISIBLE);
                enableLocationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialogforSettings();
                    }
                });

            }
        });

        builder.setPositiveButton("Open Setting", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                final Intent i = new Intent();
                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                i.setData(Uri.parse("package:" + mActivity.getPackageName()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                mActivity.startActivity(i);
                mActivity.overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_top);
            }
        });
        alert = builder.create();
        alert.show();
    }


    private void showDialogOK() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        builder.setMessage("Location Services Permission is required for this App.\n" +
                "Do you want to try again ?");
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                enableLocationDescription.setText("This feature requires your location.\nPlease enable location services.");
                enableLocationLayout.setVisibility(View.VISIBLE);
                enableLocationButton.setVisibility(View.VISIBLE);
                enableLocationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckAndRequestPermissions();
                    }

                });

            }
        });

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog
                CheckAndRequestPermissions();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                enableLocationDescription.setText("This feature requires your location.\nPlease enable location services.");
                enableLocationLayout.setVisibility(View.VISIBLE);
                enableLocationButton.setVisibility(View.VISIBLE);
                enableLocationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckAndRequestPermissions();
                    }

                });
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        isLocationEnabled = true;
        if (!firstTime) {
            firstTime = true;
            if (isBetterLocation(location, currentBestLocation)) {
                currentBestLocation = location;
            }
        } else {
            currentBestLocation = location;
        }


        if (onLocationUtilListener != null) {
            onLocationUtilListener.onLocationChanged(currentBestLocation);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cancelDialog();
            }
        }, 2000);

    }

    public void onActivityResult(int resultCode) {
        switch ((resultCode)) {
            case Activity.RESULT_OK:
                startLocationUpdates();
                Log.d(TAG, "User enabled location");
                break;
            case RESULT_CANCELED:
                // The user was asked to change settings, but chose not to
                cancelDialog();
                Log.d(TAG, "User Cancelled enabling location");
                enableLocationDescription.setText("This feature requires your location.\nPlease enable location services.");
                enableLocationLayout.setVisibility(View.VISIBLE);
                enableLocationButton.setVisibility(View.VISIBLE);
                enableLocationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GetCurrentLocation();
                    }
                });
                break;
            default:
                break;
        }
    }

    public void cancelDialog() {
        if (progressDialog != null) {
            progressDialog.cancel();
        }
    }

    public boolean CheckAndRequestPermissions() {
        int locationPermission = ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(mActivity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_PERMISSION);
            return false;
        }
        if (mGoogleApiClient == null) {
            buildGoogleApiClient(showDialogs);
            return true;
        } else if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            return true;
        } else if (mGoogleApiClient.isConnected()) {
            GetCurrentLocation();
        }

        return true;
    }


    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {

            return true;
        }


        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > FIVE_SECONDS;
        boolean isSignificantlyOlder = timeDelta < -FIVE_SECONDS;
        boolean isNewer = timeDelta > 0;


        if (isSignificantlyNewer) {
            return true;

        } else if (isSignificantlyOlder) {
            return false;
        }


        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;


        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());


        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }


    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public interface OnLocationUtilListener {
        void onLocationEnabled();

        void onLocationChanged(Location location);
    }

}
