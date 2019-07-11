package pt.ulisboa.tecnico.cmov.proj;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class LogView extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    private static ArrayAdapter<String> adapter = null;

    public String URL_BASE;
    public String URL_LOAD_LOGS;

    Context ctx = this;
    private RequestQueue queue = null;
    private JSONObject httpResponse = null;

    String success;
    String error;

    String logs;

    ListView listView;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_log_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        URL_BASE = getString(R.string.serverIP);
        URL_LOAD_LOGS = URL_BASE + "/logs";

        FloatingActionButton fab = findViewById(R.id.returnFab);
        fab.setOnClickListener(view -> goBack());

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLogs);
        swipeRefreshLayout.setOnRefreshListener(this);
        updateTextView(getString(R.string.logs_default_text));

        httpRequestForServerLogsLoading();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.log_view, menu);
        return true;
    }

    protected void goBack() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        httpRequestForServerLogsLoading();
        return super.onOptionsItemSelected(item);
    }

    private void httpRequestForServerLogsLoading() {
        android.util.Log.d("debug", "Starting GET request to URL " + URL_LOAD_LOGS);
        createHTTPQueue();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL_LOAD_LOGS, null,
                httpResponse -> {
                    try {
                        setHTTPResponse(httpResponse);
                        android.util.Log.d("debug", httpResponse.toString());
                        if(httpResponse.has("error")) {
                            error = httpResponse.getString("error");
                            android.util.Log.d("debug", "Error");
                            android.util.Log.d("debug", error);
                            Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show();
                        }
                        else if(httpResponse.has("success")) {
                            success = httpResponse.getString("success");
                            logs = httpResponse.getString("logs");
                            android.util.Log.d("debug", "Success");
                            android.util.Log.d("debug", success);
                            android.util.Log.d("debug", "Logs");
                            android.util.Log.d("debug", logs);
                            Toast.makeText(ctx, success, Toast.LENGTH_SHORT).show();

                            if(logs!=null && logs.trim().length() > 0) {
                                updateTextView(logs);
                            }
                        }
                        else {
                            Toast.makeText(ctx, "No adequate response received", Toast.LENGTH_SHORT).show();
                            throw new Exception("No adequate response received", new Exception());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cleanHTTPResponse();
                }, error -> {
            cleanHTTPResponse();
            android.util.Log.d("debug", "GET error");
        }
        );
        queue.add(request);
    }

    private void setHTTPResponse(JSONObject json) {
        this.httpResponse = json;
    }

    private void cleanHTTPResponse() {
        success = null;
        error = null;
        this.httpResponse = null;
        android.util.Log.d("debug", "Cleaned " + new Date().getTime());
    }

    private void createHTTPQueue() {
        if(this.queue == null) {
            this.queue = Volley.newRequestQueue(ctx);
        }
    }

    private void updateTextView(String text) {
        ArrayList<String> array = new ArrayList<>();
        array.add(text);

        listView = findViewById(R.id.listViewLogs);
        adapter = new ArrayAdapter<>(this, R.layout.logs_list_view_element, array);
        listView.setAdapter(adapter);

        //You explicitly need to tell Android when to hide loading animation
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        httpRequestForServerLogsLoading();
    }
}