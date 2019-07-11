package pt.ulisboa.tecnico.cmov.proj.HTMLHandlers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.security.PublicKey;
import java.util.HashMap;

import pt.ulisboa.tecnico.cmov.proj.Cryptography;
import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;
import pt.ulisboa.tecnico.cmov.proj.HomePage;

public class HttpRequestPutSetUrl extends HttpRequest {

    private static String URL;
    private static String sliceURL;
    private static String sessionId;
    private static String username;
    private static String albumId;

    public HttpRequestPutSetUrl(Context context) {
        super(context);
    }

    public static void httpRequest(@NonNull String sessionId, @NonNull String username, @NonNull String sliceURL, @NonNull String albumId, @NonNull Context mContext, @NonNull String url) {
        HttpRequestPutSetUrl.URL = url;
        HttpRequestPutSetUrl.sliceURL = sliceURL;
        HttpRequestPutSetUrl.sessionId = sessionId;
        HttpRequestPutSetUrl.username = username;
        HttpRequestPutSetUrl.albumId = albumId;

        PublicKey publicKey = ((Peer2PhotoApp) ((HomePage) mContext).getApplication()).getAlbumPublicKey(Integer.valueOf(albumId));
        if (publicKey == null) {
            HttpRequestGetPublicAlbumKey.httpRequest(username, sessionId, albumId);
        } else {
            httpRequestCode(mContext);
        }
    }

    public static void httpRequestCode(Context mContext) {
        android.util.Log.d("debug", "Starting PUT request to URL " + URL);
        createHTTPQueue();

        String cipheredSliceURL = HttpRequestPutSetUrl.cipherURL(sliceURL, albumId, mContext);

        HashMap<String, String> mapRequest = new HashMap<>();
        mapRequest.put("sessionId", sessionId);
        mapRequest.put("username", username);
        mapRequest.put("URL", cipheredSliceURL);
        mapRequest.put("albumId", albumId);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, URL, new JSONObject(mapRequest),
                httpResponse -> {
                    try {
                        setHTTPResponse(httpResponse);
                        android.util.Log.d("debug", httpResponse.toString());
                        if(httpResponse.has("error")) {
                            error = httpResponse.getString("error");
                            android.util.Log.d("debug", "Error");
                            android.util.Log.d("debug", error);
                            Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
                            ((HomePage)mContext).updateApplicationLogs("Create Album", error);
                        }
                        else if(httpResponse.has("success")) {
                            success = httpResponse.getString("success");
                            android.util.Log.d("debug", "Success");
                            android.util.Log.d("debug", success);
                            Toast.makeText(mContext, success, Toast.LENGTH_SHORT).show();
                            ((HomePage)mContext).updateApplicationLogs("Create Album", success);
                        }
                        else {
                            Toast.makeText(mContext, "No adequate response received", Toast.LENGTH_SHORT).show();
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

    private static String cipherURL(String sliceurl, String albumId, Context mContext) {
        PublicKey publicKey = ((Peer2PhotoApp) ((HomePage) mContext).getApplication()).getAlbumPublicKey(Integer.valueOf(albumId));
        return Cryptography.cipher(publicKey, sliceurl);
    }
}