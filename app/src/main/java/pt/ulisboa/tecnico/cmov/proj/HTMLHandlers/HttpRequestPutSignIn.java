package pt.ulisboa.tecnico.cmov.proj.HTMLHandlers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.proj.SignIn;

public class HttpRequestPutSignIn extends HttpRequest {

    public HttpRequestPutSignIn(@NonNull Context context) {
        super(context);
    }

    public static void httpRequest(@NonNull String username, @NonNull String password, @NonNull String URL){
        android.util.Log.d("debug", "Starting PUT request to URL " + URL);
        createHTTPQueue();
        HashMap<String, String> mapRequest = new HashMap<>();
        mapRequest.put("username", username);
        mapRequest.put("password", password);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL, new JSONObject(mapRequest),
                httpResponse -> {
                    try {
                        setHTTPResponse(httpResponse);
                        android.util.Log.d("debug", httpResponse.toString());
                        if(httpResponse.has("error")) {
                            error = httpResponse.getString("error");
                            android.util.Log.d("debug", "Error");
                            android.util.Log.d("debug", error);
                            Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show();
                            ((SignIn) ctx).updateApplicationLogs(error);
                        }
                        else if(httpResponse.has("success")) {
                            success = httpResponse.getString("success");
                            android.util.Log.d("debug", "Success");
                            android.util.Log.d("debug", success);
                            Toast.makeText(ctx, success, Toast.LENGTH_SHORT).show();
                            ((SignIn) ctx).updateApplicationLogs(success);
                            ((SignIn) ctx).InitialVariableSetup(httpResponse.getString("sessionId"));
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
            android.util.Log.d("debug", "PUT error");
        });
        queue.add(request);
    }

}
