package pt.ulisboa.tecnico.cmov.proj;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.dropbox.core.android.Auth;

import pt.ulisboa.tecnico.cmov.proj.Dropbox.DropboxActivity;

public class ModeChoosing extends DropboxActivity {

    Context ctx = this;

    private final static String ACCESS_KEY = "ktxcdvzt610l2ao";
    private final static String ACCESS_SECRET = "wurqteptiyuh9s2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_choosing);

        Button cloud = findViewById(R.id.Cloud_Mode);
        Button WiFi = findViewById(R.id.WiFiDirect_Mode);

        cloud.setOnClickListener(v -> {

            if(isNetworkAvailable()) {
                if (!hasToken()) {
                    Auth.startOAuth2Authentication(ModeChoosing.this, ACCESS_KEY);
                    Toast.makeText(ModeChoosing.this, "Accessing Dropbox for Login",
                            Toast.LENGTH_SHORT).show();
                } else {
                    android.util.Log.d("debug", "Dropbox token obtained");
                    Intent intent = new Intent(ctx, HomePage.class);
                    startActivity(intent);
                    finish();
                }
            } else {
                Toast.makeText(ModeChoosing.this, "Network not available",
                        Toast.LENGTH_SHORT).show();
            }
        });

        WiFi.setOnClickListener(v -> {
            Intent intent = new Intent(ctx, HomePage_Wifi.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(hasToken()) {
            Toast.makeText(ModeChoosing.this, "You may now enter Cloud Mode",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void loadData() {

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
