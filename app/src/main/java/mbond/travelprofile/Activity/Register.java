package mbond.travelprofile.Activity;

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
import android.widget.Toast;

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
import mbond.travelprofile.R;
import mbond.travelprofile.DataModel.RegisterDataModel;

public class Register extends AppCompatActivity {

    private static final String TAG = "REGISTER";
    @BindView(R.id.name)
    MaterialEditText name;
    @BindView(R.id.email)
    MaterialEditText email;
    @BindView(R.id.number)
    MaterialEditText number;
    @BindView(R.id.password)
    MaterialEditText password;
    @BindView(R.id.register)
    LinearLayout register;
    CircleProgressBar circleProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.register);
        ButterKnife.bind(this);
        circleProgressBar = (CircleProgressBar) findViewById(R.id.circle_progress);
        ((ImageButton) findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


    }

    @OnClick(R.id.register)
    public void registerUser() {
        if (name.getText().toString().isEmpty()) {
            name.setErrorColor(Color.RED);
            name.setError("Incorrect Input");
            return;
        } else if (email.getText().toString().isEmpty()) {
            email.setErrorColor(Color.RED);
            email.setError("Incorrect Input");
            return;
        } else if (number.getText().toString().length() != 10) {
            number.setErrorColor(Color.RED);
            number.setError("Incorrect Input");
            return;
        } else if (password.getText().toString().isEmpty()) {
            password.setErrorColor(Color.RED);
            password.setError("Incorrect Input");
            return;
        }
        String URL = AppConstant.REGISTER;
        StringRequest stringRequest = new StringRequest(StringRequest.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    try {
                        RegisterDataModel registerData = new GsonBuilder().create().fromJson(response, RegisterDataModel.class);
                        if (registerData != null) {
                            if (registerData.getSuccess().equalsIgnoreCase("1")) {
                                getSharedPreferences("loginMeta", MODE_PRIVATE).edit().putString("isLoggedIn", "1").apply();
                                getSharedPreferences("loginMeta", MODE_PRIVATE).edit().putString("userID", registerData.getUserId()).apply();
                                getSharedPreferences("loginMeta", MODE_PRIVATE).edit().putString("userName", name.getText().toString()).apply();
                                getSharedPreferences("loginMeta", MODE_PRIVATE).edit().putString("email", email.getText().toString()).apply();
                                getSharedPreferences("loginMeta", MODE_PRIVATE).edit().putString("number", number.getText().toString()).apply();
                                Toast.makeText(Register.this, "Successfully Registered", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(Register.this, ProductDetails.class));
                            } else {
                                AppController.showToast(registerData.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        AppController.errorToast();
                    }
                }
                circleProgressBar.setVisibility(View.INVISIBLE);


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                circleProgressBar.setVisibility(View.INVISIBLE);
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name.getText().toString());
                params.put("email", email.getText().toString());
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
