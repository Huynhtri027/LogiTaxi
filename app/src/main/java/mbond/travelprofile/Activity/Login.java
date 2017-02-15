package mbond.travelprofile.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.GsonBuilder;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mbond.travelprofile.Util.AppConstant;
import mbond.travelprofile.AppController;
import mbond.travelprofile.DataModel.LoginDataModel;
import mbond.travelprofile.R;
import mbond.travelprofile.DataModel.User;

public class Login extends AppCompatActivity {

    Activity activity;
    @BindView(R.id.number)
    MaterialEditText number;
    @BindView(R.id.password)
    MaterialEditText password;
    @BindView(R.id.login)
    LinearLayout login;
    @BindView(R.id.circle_progress)
    CircleProgressBar circleProgressBar;
    private String TAG = "LOGIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.login);
        ButterKnife.bind(this);
        activity = this;

        ((ImageButton) findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


    }

    @OnClick(R.id.login)
    public void checkLogin() {
        if (number.getText().toString().length() != 10) {
            number.setErrorColor(Color.RED);
            number.setError("Incorrect Input");
            return;
        }
        circleProgressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(StringRequest.Method.POST, AppConstant.LOGIN_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    LoginDataModel loginDataModel = new GsonBuilder().create().fromJson(response, LoginDataModel.class);
                    if (loginDataModel != null) {
                        if (loginDataModel.getSuccess().equalsIgnoreCase("1")) {
                            User user = loginDataModel.getUser().get(0);
                            getSharedPreferences("loginMeta", MODE_PRIVATE).edit().putString("isLoggedIn", "1").apply();
                            getSharedPreferences("loginMeta", MODE_PRIVATE).edit().putString("userID", user.getUserid()).apply();
                            getSharedPreferences("loginMeta", MODE_PRIVATE).edit().putString("userName", user.getName()).apply();
                            startActivity(new Intent(Login.this, ProductDetails.class));
                            Log.d(TAG, "onResponse: " + response);
                        } else {
                            AppController.showToast(loginDataModel.getMessage());
                        }
                    }
                    circleProgressBar.setVisibility(View.INVISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                    circleProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                circleProgressBar.setVisibility(View.INVISIBLE);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("number", number.getText().toString());
                params.put("password", password.getText().toString());
                SharedPreferences sharedPreferences = getSharedPreferences(AppConstant.APP_DATA, MODE_PRIVATE);
                params.put("gcm_id", sharedPreferences.getString("GCM", " "));
                Log.d(TAG, "from params : " + params.toString());
                return AppConstant.checkParams(params);
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest);
    }
}
