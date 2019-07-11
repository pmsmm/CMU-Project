package pt.ulisboa.tecnico.cmov.proj.HTMLHandlers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import pt.ulisboa.tecnico.cmov.proj.HomePage;

public class HttpRequestGetUserAlbums extends HttpRequest {

    public HttpRequestGetUserAlbums(Context context) {
        super(context);
    }

    public static void httpRequest(@NonNull String username, @NonNull String sessionId, @NonNull String URL){
        android.util.Log.d("debug", "Starting GET request to URL " + URL + "/" + sessionId + "/" + username);
        createHTTPQueue();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL + "/" + sessionId + "/" + username, null,
                httpResponse -> {
                    try {
                        setHTTPResponse(httpResponse);
                        android.util.Log.d("debug", httpResponse.toString());
                        if(httpResponse.has("error")) {
                            error = httpResponse.getString("error");
                            android.util.Log.d("debug", "Error");
                            android.util.Log.d("debug", error);
                            Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show();
                            ((HomePage)ctx).updateApplicationLogs("List User Albums", error);

                        }
                        else if(httpResponse.has("success")) {
                            success = httpResponse.getString("success");
                            android.util.Log.d("debug", "Success");
                            android.util.Log.d("debug", success);
                            Toast.makeText(ctx, success, Toast.LENGTH_SHORT).show();
                            ((HomePage)ctx).updateApplicationLogs("List User Albums", success);
                            if(!httpResponse.getString("size").equals("0")){
                                String[] albumIds = httpResponse.getString("albums").split(",");
                                ((HomePage)ctx).parseAlbumNames(albumIds, httpResponse);
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
        });
        queue.add(request);
    }

}
