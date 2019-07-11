package pt.ulisboa.tecnico.cmov.proj.HTMLHandlers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.security.PrivateKey;

import pt.ulisboa.tecnico.cmov.proj.AlbumView;
import pt.ulisboa.tecnico.cmov.proj.Cryptography;

public class HttpRequestGetAlbumPhotos extends HttpRequest {

    public HttpRequestGetAlbumPhotos(Context context) {
        super(context);
    }

    public static void httpRequest(@NonNull String albumId, @NonNull String username, @NonNull String sessionId, @NonNull String URL_ALBUM){
        android.util.Log.d("debug", "Starting GET request to URL " + URL_ALBUM + "/" + sessionId + "/" + username + "/" + albumId);
        createHTTPQueue();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, URL_ALBUM + "/" + sessionId + "/" + username + "/" + albumId, null,
                httpResponse -> {
                    try {
                        setHTTPResponse(httpResponse);
                        android.util.Log.d("debug", httpResponse.toString());
                        if(httpResponse.has("error")) {
                            error = httpResponse.getString("error");
                            android.util.Log.d("debug", "Error");
                            android.util.Log.d("debug", error);
                            Toast.makeText(ctx, error, Toast.LENGTH_SHORT).show();
                            ((AlbumView)ctx).updateApplicationLogs(error, "View Album");
                        }
                        else if(httpResponse.has("success")) {
                            success = httpResponse.getString("success");
                            android.util.Log.d("debug", "Success");
                            android.util.Log.d("debug", success);
                            Toast.makeText(ctx, success, Toast.LENGTH_SHORT).show();

                            urlReplacer(httpResponse, sessionId, username, albumId, ctx);

                            ((AlbumView)ctx).updateApplicationLogs(success, "View Album");
                            ((AlbumView)ctx).urlParser(httpResponse.getString("users"), httpResponse);

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

    private static void urlReplacer(JSONObject httpResponse, String sessionId, String username, String albumId, Context mContext) {
        try {
            String[] users = httpResponse.getString("users").split(",");
            for (String user : users) {
                String cipheredURL = httpResponse.getString(user);
                String decipheredURL = decipherURL(sessionId, username, cipheredURL, albumId, mContext);
                httpResponse.remove(user);
                httpResponse.put(user, decipheredURL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String decipherURL(String sessionId, String username, String cipheredSliceURL, String albumId, Context mContext) {
        PrivateKey privateKey = ((AlbumView)mContext).getPrivateKey(Integer.valueOf(albumId));
        if(privateKey==null) {
            //HttpRequestGetPrivateAlbumKey.httpRequest("http://localhost:9090", username, sessionId, albumId);
        } else {
            return Cryptography.decipher(privateKey, cipheredSliceURL);
        }
        return null;
    }

}
