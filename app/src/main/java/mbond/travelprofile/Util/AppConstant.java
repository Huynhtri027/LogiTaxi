package mbond.travelprofile.Util;

import android.content.Context;
import android.provider.Settings;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by AMAN on 25/12/16.
 */

public class AppConstant {
    public static final int DROP = 16;
    public static final String APP_DATA = "APP_DATA";
    public static int PICK_UP = 15;
    public static String LOGIN_URL = "http://www.coutloot.com/coutloot/admin_services/logitaxi_login.php";
    public static String REGISTER = "http://www.coutloot.com/coutloot/admin_services/logitaxi_register.php";
    public static String ORDER = "http://www.coutloot.com/coutloot/admin_services/logitaxi_place_order.php";
    public static Map<String, String> checkParams(Map<String, String> map) {
        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pairs = it.next();
            if (pairs.getValue() == null) {
                map.put(pairs.getKey(), "");
            }
        }
        return map;
    }

    public static String getDeviceID(Context mActivity) {
        return Settings.Secure.getString(mActivity.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
