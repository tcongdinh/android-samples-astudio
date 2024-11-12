package vendor.datalogic.service.eventprofiles;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Toast;

import com.datalogic.device.PersistenceType;
import com.datalogic.device.configuration.ConfigException;
import com.datalogic.device.configuration.ProfileManager;
import com.datalogic.device.configuration.PropertyID;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private ProfileManager pm;
    private HashMap map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Long","onCreate is called");
        setContentView(R.layout.activity_main);

        // 1. Setting the wifi connectivity
        // to notify when Access Point has been changed
        setUp();

        // 2. Create new instance of ProfileManager with context
        pm = new ProfileManager(this);

        // 3. Create an HashMap and add it the couples {PropertyID,value}
        // to be set applying the profile.
        map = new HashMap();
        map.put(PropertyID.GREEN_SPOT_ENABLE, "false");  // BooleanProperty
        map.put(PropertyID.DEVICE_NAME_BASE , "wifi"); // TextProperty

        // 4. Create the profile calling createProfile()
        pm.createProfile( "wifi_test.json",
                map,
                "Wifi Test Profile",
                PersistenceType.ENTERPRISE_RESET_PERSISTENT //persistent to device reboots
        );

        // When wifi connection is ready, profile wifi_test.json will be loaded (line 75)
        // When wifi connection is lost, profile wifi_test.json will be unloaded (line 83).
    }

    private void setUp() {
        // 1. Setting the wifi connectivity
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Check for permission (ACCESS_FINE_LOCATION required on devices running API 23+)
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            registerNetworkCallback();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void registerNetworkCallback() {
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // Network available, check if it's Wi-Fi
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
                if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Toast.makeText(MainActivity.this, "Connected to Wi-Fi", Toast.LENGTH_SHORT).show();
                    pm.loadProfile("wifi_test.json");
                }
            }

            @Override
            public void onLost(Network network) {
                // Wi-Fi or mobile data network lost
                Toast.makeText(MainActivity.this, "Network lost", Toast.LENGTH_SHORT).show();
                pm.unloadProfile();
            }
        };

        NetworkRequest.Builder requestBuilder = new NetworkRequest.Builder();
        requestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);  // Listen only for Wi-Fi
        connectivityManager.registerNetworkCallback(requestBuilder.build(), networkCallback);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        pm.createProfile( "wifi_test.json",
                map,
                "Wifi Test Profile",
                PersistenceType.ENTERPRISE_RESET_PERSISTENT //persistent to device reboots
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Long", "onDestroy is called");
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        pm.deleteProfile("wifi_test.json");
    }

}
