package pt.ulisboa.tecnico.cmov.proj;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CacheSize extends AppCompatActivity {

    EditText cacheSizeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_cache_size);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);

        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(v->clearCache());

        cacheSizeText = findViewById(R.id.cacheSizeText);
    }

    public void clearCache() {
        int cacheSize = Integer.parseInt(cacheSizeText.getText().toString());

        if (cacheSize >= 1) {
            Intent intent = new Intent();
            intent.putExtra("cacheSize", cacheSize);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
        else {
            Toast.makeText(getApplicationContext(), "Cache size must be at least 1 KB", Toast.LENGTH_SHORT).show();
        }
    }

}
