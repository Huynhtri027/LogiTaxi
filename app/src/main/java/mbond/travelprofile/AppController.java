package mbond.travelprofile;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import mbond.travelprofile.Util.VolleySingleton;

/**
 * Created by Aman Jain on 01/03/16.
 */
public class AppController extends Application {
    public static final String TAG = "AppController";
    public static boolean applicationRunning;
    public static VolleySingleton volleyQueueInstance;
    static Toast toast;
    private static AppController mInstance;
    private static Context mContext;
    private RequestQueue mRequestQueue;

    public static Context getContext() {
        return mContext;
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public static boolean isOnline() {

        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        boolean res = netInfo != null && netInfo.isConnectedOrConnecting();
        if (!res) {
            noInternetToast();
        }
        return res;
    }

    public static void showToast(String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static void errorToast() {

        showToast("Error occured. Try again");
    }

    public static void noInternetToast() {
        showToast("No Internet Connection");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mContext = this.getApplicationContext();
        volleyQueueInstance = VolleySingleton.getInstance(getApplicationContext());
        // LeakCanary.install(this);

        // CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/FuturaStdbook.ttf").setFontAttrId(R.attr.fontPath).build());
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
        req.setRetryPolicy(new DefaultRetryPolicy(5000, 4, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
        req.setRetryPolicy(new DefaultRetryPolicy(5000, 4, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);

        }
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);

    }


}

