package vendor.datalogic.service.appforeground;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.datalogic.android.sdk.*;
import com.datalogic.decode.configuration.LengthControlMode;
import com.datalogic.device.configuration.*;
import com.datalogic.device.PersistenceType;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private boolean stop;
    private ProfileManager pm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // 1. Create new instance of ProfileManager with context
        pm = new ProfileManager(this);

        // 2. Create an HashMap and add it the couples {PropertyID,value}
        // to be set applying the profile.
        HashMap map = new HashMap();
        map.put(PropertyID.GREEN_SPOT_ENABLE, "false");  // BooleanProperty
        map.put(PropertyID.LABEL_PREFIX, "testing"); // TextProperty

        // 3. Create the profile calling createProfile()
        pm.createProfile( "testing_profile.json",
                map,
                "Example Profile",
                PersistenceType.ENTERPRISE_RESET_PERSISTENT //persistent to device reboots
        );

        // 4.
        // Calling addProfileRule() to automatically load profile
        // testing_profile.json when the app come to foreground.
        // ( the app which contain this package "com.example.configurationexample")
        StringBuffer rule_test = new StringBuffer("rule_test");
        pm.addProfileRule(rule_test, "testing_profile.json", "com.example.configurationexample", new ArrayList());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Perform final cleanup before activity is destroyed
        pm.removeProfileRule("rule_test");
        pm.deleteProfile("testing_profile.json");
    }
}