package mbond.travelprofile.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import mbond.travelprofile.R;

public class ProductDetails extends AppCompatActivity {

    Activity activity;
    CardView name, type, weight, price;
    EditText nameText, weightView, priceView;
    ScrollView scroller;
    Spinner type_spinner;
    FloatingActionButton details_done;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_product_details);
        ((ImageView) findViewById(R.id.back)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        activity = this;
        initiate_views();
    }

    void initiate_views() {
        scroller = (ScrollView) activity.findViewById(R.id.scroller);
        name = (CardView) activity.findViewById(R.id.package_name_layout);
        type = (CardView) activity.findViewById(R.id.package_type_layout);
        weight = (CardView) activity.findViewById(R.id.package_weight_layout);
        price = (CardView) activity.findViewById(R.id.package_price_layout);

        nameText = (EditText) activity.findViewById(R.id.name);
        nameText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if (nameText.getText().toString().isEmpty()) {

                    } else {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(type.getWindowToken(), 0);
                        (new Handler()).postDelayed(new Runnable() {

                            public void run() {
                                type.setVisibility(View.VISIBLE);
                            }
                        }, 600);
                    }
                }
                return false;
            }
        });

        type_spinner = (Spinner) activity.findViewById(R.id.type_spinner);
        weightView = (EditText) activity.findViewById(R.id.weight);
        priceView = (EditText) activity.findViewById(R.id.price);
        details_done = (FloatingActionButton) activity.findViewById(R.id.details_done);
        type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    weight.setVisibility(View.VISIBLE);
                    price.setVisibility(View.INVISIBLE);
                    weightView.requestFocus();
                    (new Handler()).postDelayed(new Runnable() {
                        public void run() {
                            weightView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                            weightView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                            scroller.smoothScrollTo(0, weightView.getScrollY());
                        }
                    }, 200);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        weightView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if (weightView.getText().toString().isEmpty()) {

                    } else {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(type.getWindowToken(), 0);
                        (new Handler()).postDelayed(new Runnable() {

                            public void run() {
                                price.setVisibility(View.VISIBLE);
                                priceView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                                priceView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                                priceView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                    @Override
                                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                                            (new Handler()).postDelayed(new Runnable() {
                                                public void run() {
                                                    details_done.setVisibility(View.VISIBLE);
                                                }
                                            }, 600);
                                        }
                                        return false;
                                    }
                                });
                            }
                        }, 200);
                    }
                }
                return false;
            }
        });

        details_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validated()) {
                    Toast.makeText(activity, "Now finally book the cab", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ProductDetails.this, ShowTaxi.class);
                    intent.putExtra("product_name", nameText.getText().toString());
                    intent.putExtra("product_cat", type_spinner.getSelectedItem().toString());
                    intent.putExtra("product_price", priceView.getText().toString());
                    intent.putExtra("product_weight", weightView.getText().toString());
                    startActivity(intent);

                }
            }
        });

    }

    boolean validated() {
        if (nameText.getText().toString().isEmpty()) {
            nameText.requestFocus();
            nameText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
            nameText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
            Toast.makeText(activity, "Please Enter Package Name", Toast.LENGTH_LONG).show();
            scroller.smoothScrollTo(0, nameText.getScrollY());
            return false;
        }

        if (type_spinner.getSelectedItemPosition() == 0) {
            Toast.makeText(activity, "Please Enter Package Type", Toast.LENGTH_LONG).show();
            scroller.smoothScrollTo(0, type_spinner.getScrollY());
            return false;
        }

        if (weightView.getText().toString().isEmpty()) {
            weightView.requestFocus();
            weightView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
            weightView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
            Toast.makeText(activity, "Please Enter Package Weight", Toast.LENGTH_LONG).show();
            scroller.smoothScrollTo(0, weightView.getScrollY());
            return false;
        }

        if (priceView.getText().toString().isEmpty()) {
            priceView.requestFocus();
            priceView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
            priceView.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
            Toast.makeText(activity, "Please Enter Package Price", Toast.LENGTH_LONG).show();
            scroller.smoothScrollTo(0, priceView.getScrollY());
            return false;
        }
        return true;
    }
}
