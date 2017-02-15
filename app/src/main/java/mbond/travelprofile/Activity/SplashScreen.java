package mbond.travelprofile.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.GsonBuilder;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mbond.travelprofile.Util.AppConstant;
import mbond.travelprofile.AppController;
import mbond.travelprofile.DataModel.OrderDetail;
import mbond.travelprofile.DataModel.PendingOrderDataModel;
import mbond.travelprofile.R;

public class SplashScreen extends AppCompatActivity {

    private static final String TAG = "SPLASHSCREEN";
    public static boolean isGCMRegistered = false;
    public static boolean GCMRegistering = false;
    TextView loginView, registerView, book_now;
    LinearLayout signUpLayout;
    Activity activity;
    SharedPreferences preferences;
    boolean loggedIn = false;
    CircleProgressBar circleProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash);
        circleProgressBar = (CircleProgressBar) findViewById(R.id.circle_progress);
        activity = this;
        initializeViews();
        setupSplash();
    }


    void initializeViews() {
        preferences = activity.getSharedPreferences("loginMeta", MODE_PRIVATE);
        loginView = (TextView) activity.findViewById(R.id.login);
        registerView = (TextView) activity.findViewById(R.id.register);
        signUpLayout = (LinearLayout) activity.findViewById(R.id.signUpLayout);
        book_now = (TextView) activity.findViewById(R.id.book_now);
    }

    void setupSplash() {
        if (preferences.getString("isLoggedIn", "0").equals("1")) {
            loggedIn = true;
            signUpLayout.setVisibility(View.GONE);
            book_now.setVisibility(View.VISIBLE);
            book_now.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    circleProgressBar.setVisibility(View.VISIBLE);
                    String URL = "http://www.coutloot.com/coutloot/admin_services/logitaxi_check_pending_order.php";
                    StringRequest stringRequest = new StringRequest(StringRequest.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response != null) {
                                PendingOrderDataModel pendingOrderDataModel = new GsonBuilder().create().fromJson(response, PendingOrderDataModel.class);
                                if (pendingOrderDataModel.getSuccess().equalsIgnoreCase("1")) {
                                    List<OrderDetail> orderDetails = pendingOrderDataModel.getOrderDetails();
                                    if (orderDetails.size() > 0) {
                                        Intent intent = new Intent(activity, ProductOnWay.class);
                                        intent.putExtra("orderID", orderDetails.get(0).getOrderid());
                                        startActivity(intent);
                                        return;
                                    }

                                } else {
                                    AppController.showToast(pendingOrderDataModel.getMessage());
                                }
                            }
                            circleProgressBar.setVisibility(View.GONE);
                            startActivity(new Intent(activity, ProductDetails.class));
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            circleProgressBar.setVisibility(View.GONE);
                            AppController.errorToast();
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            SharedPreferences sharedPreferences = getSharedPreferences(AppConstant.APP_DATA, MODE_PRIVATE);
                            //params.put("gcm_id", sharedPreferences.getString("GCM", " "));
                            params.put("user_id", sharedPreferences.getString("userID", ""));
                            Log.d(TAG, "from params : " + params.toString());
                            return AppConstant.checkParams(params);
                        }
                    };
                    AppController.getInstance().addToRequestQueue(stringRequest);

                }
            });
        }

        if (loggedIn) {

        } else {
            signUpLayout.setVisibility(View.VISIBLE);
            book_now.setVisibility(View.GONE);
            loginView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(activity, Login.class));
                }
            });
            registerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(activity, Register.class));
                }
            });
        }
    }
}
