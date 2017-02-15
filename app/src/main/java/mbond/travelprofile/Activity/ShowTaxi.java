package mbond.travelprofile.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.uber.sdk.android.core.auth.AccessTokenManager;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.ServerTokenSession;
import com.uber.sdk.rides.client.SessionConfiguration;
import com.uber.sdk.rides.client.UberRidesApi;
import com.uber.sdk.rides.client.model.PriceEstimatesResponse;
import com.uber.sdk.rides.client.model.TimeEstimatesResponse;
import com.uber.sdk.rides.client.services.RidesService;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mbond.travelprofile.AppController;
import mbond.travelprofile.R;
import mbond.travelprofile.Util.AppConstant;
import mbond.travelprofile.Util.LocationUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static mbond.travelprofile.Util.AppConstant.DROP;
import static mbond.travelprofile.Util.AppConstant.PICK_UP;

/**
 * Created by AMAN on 24/12/16.
 */

public class ShowTaxi extends AppCompatActivity implements OnMapReadyCallback, LocationUtil.OnLocationUtilListener {
    private static final String TAG = "SHOWTAXI";
    @BindView(R.id.enable_location_button)
    public Button enableLocationButton;
    @BindView(R.id.enable_location_layout)
    public RelativeLayout enableLocationLayout;
    @BindView(R.id.enable_location_description)
    public TextView enableLocationDescription;
    @BindView(R.id.pickup)
    TextView pickup;
    @BindView(R.id.drop)
    TextView drop;
    @BindView(R.id.book)
    Button book;
    @BindView(R.id.circle_progress)
    CircleProgressBar circleProgressBar;
    @BindView(R.id.my_location)
    FloatingActionButton myLocation;
    @BindView(R.id.pickup_layout)
    CardView pickupLayout;
    @BindView(R.id.drop_layout)
    CardView dropLayout;
    @BindView(R.id.detail)
    LinearLayout detail;
    @BindView(R.id.estimate_time)
    TextView estimateTime;
    @BindView(R.id.estimate_price)
    TextView estimatePrice;
    SupportMapFragment mapView;
    UiSettings mUiSettings;
    GoogleMap mGoogleMap;
    Location currentLocation;
    boolean isPickUPLayout = true;
    Marker marker;
    LocationUtil locationUtil;
    boolean gettingLocation;
    SessionConfiguration config;
    ServerTokenSession session;
    RidesService service;
    String cabID;
    Call<PriceEstimatesResponse> priceEstimatesResponse = null;
    Call<TimeEstimatesResponse> eta = null;
    String productName, productCat, productPrice, productWeight;
    Integer price;
    private Location pickupLocation;
    private Location dropLocation;
    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.showtaxi);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        ButterKnife.bind(this);
        locationUtil = new LocationUtil(this, enableLocationLayout, enableLocationButton, enableLocationDescription);
        locationUtil.setOnLocationUtilListener(this);
        productName = getIntent().getStringExtra("product_name");
        productCat = getIntent().getStringExtra("product_cat");
        productPrice = getIntent().getStringExtra("product_price");
        productWeight = getIntent().getStringExtra("product_weight");
        mapView = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view);
        mapView.getMapAsync(this);
        dropLayout.setEnabled(false);
        config = new SessionConfiguration.Builder()
                .setClientId("e5fF_R1q1fp4bbzqDfO8xPT1HFbVHU6E")
                .setServerToken("lkqjhE4YG7037hRDTns5ryqPaYTSuMJ3JWrTjv6E")
                .setClientSecret("YC6o07NZgUd1r70fG6xnmhJbcm3Bj3Fd3s3m0DU_")
                .setEnvironment(SessionConfiguration.Environment.SANDBOX)
                .build();

        session = new ServerTokenSession(config);
        service = UberRidesApi.with(session).build().createService();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: ");
        mGoogleMap = googleMap;
        mUiSettings = googleMap.getUiSettings();
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setCompassEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gettingLocation) {
                    updateCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                    gettingLocation = true;
                    locationUtil.GetCurrentLocation(2000, true);
                }
            }
        });


        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                Runnable run = new Runnable() {

                    @Override
                    public void run() {

                        // cancel all the previous requests in the queue to optimise your network calls during autocomplete search
                        String address = getAddress(googleMap.getCameraPosition().target.latitude, googleMap.getCameraPosition().target.longitude);
                        LatLng latlng = googleMap.getCameraPosition().target;
                        if (isPickUPLayout) {
                            pickup.setText(address);
                            pickupLocation = new Location("");
                            pickupLocation.setLatitude(latlng.latitude);
                            pickupLocation.setLongitude(latlng.longitude);
                        } else {
                            drop.setText(address);
                            dropLocation = new Location("");
                            dropLocation.setLatitude(latlng.latitude);
                            dropLocation.setLongitude(latlng.longitude);
                        }
                        if (pickupLocation != null && dropLocation != null) {
                            requestRides();
                        }

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
        });

    }


    @OnClick(R.id.pickup)
    public void setPickup() {
        Intent i = new Intent(ShowTaxi.this, PickLocationActivity.class);
        i.putExtra("STATUS", PICK_UP);
        startActivityForResult(i, PICK_UP);
        overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_top);
        dropLayout.setForeground(new ColorDrawable(getResources().getColor(R.color.inactive)));
        pickupLayout.setForeground(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        isPickUPLayout = true;
        pickupLayout.setEnabled(true);
    }

    @OnClick(R.id.drop)
    public void setDrop() {
        Intent i = new Intent(ShowTaxi.this, PickLocationActivity.class);
        i.putExtra("STATUS", DROP);
        startActivityForResult(i, AppConstant.DROP);
        overridePendingTransition(R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_top);
        pickupLayout.setForeground(new ColorDrawable(getResources().getColor(R.color.inactive)));
        dropLayout.setForeground(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        isPickUPLayout = false;
        pickupLayout.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_UP) {
            if (data != null) {

                String address = data.getStringExtra("Location Address");
                double lat = data.getDoubleExtra("lat", 0.000000);
                double lon = data.getDoubleExtra("lng", 0.000000);
                pickup.setText(address);
                updateCamera(new LatLng(lat, lon));

            }
        } else if (requestCode == DROP) {
            if (data != null) {

                String address = data.getStringExtra("Location Address");
                double lat = data.getDoubleExtra("lat", 0.000000);
                double lon = data.getDoubleExtra("lng", 0.000000);
                drop.setText(address);
                updateCamera(new LatLng(lat, lon));
                //  requestRides();
            }
        } else {
            switch (requestCode) {
                case LocationUtil.REQUEST_CHECK_SETTINGS:
                    locationUtil.onActivityResult(resultCode);
                    break;
            }
        }

    }

    private String getAddress(double latitude, double longitude) {
        StringBuilder result = new StringBuilder();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
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
            }
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        return result.toString();
    }

    @Override
    public void onLocationEnabled() {

    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
       /* if (marker == null)
            marker = mGoogleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_32)).position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));*/
        String address = getAddress(currentLocation.getLatitude(), currentLocation.getLongitude());
        updateCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        if (isPickUPLayout) {
            pickup.setText(address);
        } else {
            drop.setText(address);

        }
        locationUtil.stopLocationUpdates();
        gettingLocation = false;
    }

    public void updateMarker(LatLng latLng) {
        if (marker != null)
            marker.setPosition(latLng);
    }

    public void updateCamera(LatLng latLng) {
        if (isPickUPLayout) {
            pickupLocation = new Location("");
            pickupLocation.setLatitude(latLng.latitude);
            pickupLocation.setLongitude(latLng.longitude);

        } else {
            dropLocation = new Location("");
            dropLocation.setLatitude(latLng.latitude);
            dropLocation.setLongitude(latLng.longitude);
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(16f)
                .build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void initiateUber() {
        AccessTokenManager accessTokenManager = new AccessTokenManager(this);
        int expirationTime = 2592000;
        List<Scope> scopes = Arrays.asList(Scope.RIDE_WIDGETS);
        String token = "obtainedAccessToken";
        String refreshToken = "obtainedRefreshToken";
        String tokenType = "obtainedTokenType";
        AccessToken accessToken = new AccessToken(expirationTime, scopes, token, refreshToken, tokenType);
        accessTokenManager.setAccessToken(accessToken);
    }

    private void requestRides() {
        AppController.showToast("Fetching Fares...");
        if (priceEstimatesResponse != null) {
            priceEstimatesResponse.cancel();
        }
        if (eta != null) {
            eta.cancel();
        }
        showCircleProgress();
        book.setEnabled(false);
        priceEstimatesResponse = service.getPriceEstimates((float) pickupLocation.getLatitude(), (float) pickupLocation.getLongitude(), (float) dropLocation.getLatitude(), (float) dropLocation.getLongitude());
        priceEstimatesResponse.enqueue(new Callback<PriceEstimatesResponse>() {
            @Override
            public void onResponse(Call<PriceEstimatesResponse> call, Response<PriceEstimatesResponse> response) {
                PriceEstimatesResponse products = response.body();
                if (products != null) {
                    if (products.getPrices().size() > 0) {
                        cabID = products.getPrices().get(0).getProductId();
                        estimatePrice.setText(products.getPrices().get(0).getEstimate());
                        price = products.getPrices().get(0).getHighEstimate();
                        eta = service.getPickupTimeEstimate((float) pickupLocation.getLatitude(), (float) pickupLocation.getLongitude(), products.getPrices().get(0).getProductId());
                        eta.enqueue(new Callback<TimeEstimatesResponse>() {
                            @Override
                            public void onResponse(Call<TimeEstimatesResponse> call, Response<TimeEstimatesResponse> etaResponse) {
                                TimeEstimatesResponse etaBody = etaResponse.body();
                                if (etaBody != null) {
                                    if (etaBody.getTimes().size() > 0) {
                                        String time = String.valueOf(etaBody.getTimes().get(0).getEstimate() > 60 ? (Math.ceil(etaBody.getTimes().get(0).getEstimate() / 60) + " mins ETA") : etaBody.getTimes().get(0).getEstimate() + " secs ETA");
                                        estimateTime.setText(time);
                                        detail.setVisibility(View.VISIBLE);
                                        book.setEnabled(true);
                                    } else {
                                        Toast.makeText(ShowTaxi.this, "No Near by LogiTaxi Available.", Toast.LENGTH_LONG).show();
                                        detail.setVisibility(View.GONE);
                                        hideCircleProgress();
                                    }
                                }
                                hideCircleProgress();
                            }

                            @Override
                            public void onFailure(Call<TimeEstimatesResponse> call, Throwable t) {
                                hideCircleProgress();
                            }
                        });

                    } else {
                        Toast.makeText(ShowTaxi.this, "No Near by LogiTaxi Available.", Toast.LENGTH_LONG).show();
                        detail.setVisibility(View.GONE);
                        hideCircleProgress();
                    }
                }
            }

            @Override
            public void onFailure(Call<PriceEstimatesResponse> call, Throwable t) {
                hideCircleProgress();
            }
        });

    }

    @OnClick(R.id.book)
    public void bookTaxi() {
        if (dropLocation == null) {
            Toast.makeText(ShowTaxi.this, "Enter Drop Location", Toast.LENGTH_LONG).show();
        } else if (pickupLocation == null) {
            Toast.makeText(ShowTaxi.this, "Enter Pick Up Location", Toast.LENGTH_LONG).show();
        } else if (!book.isEnabled()) {
            AppController.showToast("Fetching Fare....");
        } else {
            showCircleProgress();
           /* RideRequestParameters.Builder rideParameter = new RideRequestParameters.Builder(cabID, (float) pickupLocation.getLatitude(), (float) pickupLocation.getLongitude(), null, null, null, (float) dropLocation.getLatitude(), (float) dropLocation.getLongitude(), null, null, null, null, null, null, null);
            Call<Ride> requestingRide = service.requestRide(rideParameter.build());
            requestingRide.enqueue(new Callback<Ride>() {
                @Override
                public void onResponse(Call<Ride> call, Response<Ride> response) {
                    //  sendOrderData(response.body());
                    if (response != null)
                        Log.d(TAG, "onResponse: " + response.body().getRideId());
                    hideCircleProgress();
                }

                @Override
                public void onFailure(Call<Ride> call, Throwable t) {
                    hideCircleProgress();
                }
            });*/
            sendOrderData();
           /* new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideCircleProgress();
                    Toast.makeText(ShowTaxi.this, "Successfully Booked", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(ShowTaxi.this, ProductOnWay.class));
                }
            }, 2000);*/


        }
    }

    private void sendOrderData() {

        StringRequest stringRequest = new StringRequest(StringRequest.Method.POST, AppConstant.ORDER, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: " + response);
                if (response != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("success").equalsIgnoreCase("1")) {
                            Toast.makeText(ShowTaxi.this, "Successfully Booked", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(ShowTaxi.this, ProductOnWay.class).putExtra("orderID", jsonObject.getString("order_id")));
                            finish();
                        } else {
                            AppController.showToast(jsonObject.getString("message"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                circleProgressBar.setVisibility(View.INVISIBLE);
            }

        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                circleProgressBar.setVisibility(View.INVISIBLE);
                AppController.errorToast();
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("product_name", productName);
                params.put("product_cat", productCat);
                params.put("product_price", productPrice);
                params.put("product_weight", productWeight);
                SharedPreferences sharedPreferences = getSharedPreferences(AppConstant.APP_DATA, Context.MODE_PRIVATE);
                String userID = sharedPreferences.getString("userID", "");
                params.put("user_id", userID);
                params.put("cost", String.valueOf(price));
                params.put("src1", String.valueOf(pickupLocation.getLatitude()));
                params.put("src2", String.valueOf(pickupLocation.getLongitude()));
                params.put("dest1", String.valueOf(dropLocation.getLatitude()));
                params.put("dest2", String.valueOf(dropLocation.getLongitude()));

                Log.d(TAG, "from params : " + params.toString());
                return AppConstant.checkParams(params);
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest);
    }


    public void hideCircleProgress() {
        circleProgressBar.setVisibility(View.INVISIBLE);
    }

    public void showCircleProgress() {
        circleProgressBar.setVisibility(View.VISIBLE);
    }

}
