package pt.ulisboa.tecnico.cmov.proj.HTMLHandlers;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import pt.ulisboa.tecnico.cmov.proj.HomePage;
import pt.ulisboa.tecnico.cmov.proj.MainActivity;

public class HttpRequestDeleteSession extends HttpRequest {

    public HttpRequestDeleteSession(Context context) {
        super(context);
    }

    public static void httpRequest(@NonNull String sessionId, @NonNull String URL_SIGNOUT){
        android.util.Log.d("debug", "Starting DELETE request to URL " + URL_SIGNOUT + "/" + sessionId);
        createHTTPQueue();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, URL_SIGNOUT + "/" + sessionId, null,
                httpResponse -> {
                    try {
                        setHTTPResponse(httpResponse);
                        android.util.Log.d("debug", httpResponse.toString());
                        if(httpResponse.has("error")) {
                            error = httpResponse.getString("error");
                            android.util.Log.d("debug", "Error");
                            android.util.Log.d("debug", error);
                            Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show();
                            ((HomePage) ctx).updateApplicationLogs("Sign Out", error);
                        }
                        else if(httpResponse.has("success")) {
                            success = httpResponse.getString("success");
                            android.util.Log.d("debug", "Success");
                            android.util.Log.d("debug", success);
                            Toast.makeText(ctx, "Sign out successful", Toast.LENGTH_SHORT).show();
                            ((HomePage) ctx).updateApplicationLogs("Sign Out", success);
                            ((HomePage) ctx).confirmSignOut();
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
            android.util.Log.d("debug", "DELETE error");
        }
        );
        queue.add(request);
    }

}
