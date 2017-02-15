package mbond.travelprofile.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mbond.travelprofile.R;

public class ProductOnWay extends AppCompatActivity {
    @BindView(R.id.okay)
    Button okay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.product_on_way);
        ((TextView) findViewById(R.id.order_no)).setText("#" + getIntent().getStringExtra("orderID"));
        ButterKnife.bind(this);
    }

    @Override
    public void onBackPressed() {
        return;

    }

    @OnClick(R.id.okay)
    public void Okay() {
        Intent intent = new Intent(ProductOnWay.this, SplashScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
