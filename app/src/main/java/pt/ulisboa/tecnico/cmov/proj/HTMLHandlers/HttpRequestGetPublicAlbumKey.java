package pt.ulisboa.tecnico.cmov.proj.HTMLHandlers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;
import pt.ulisboa.tecnico.cmov.proj.HomePage;
import pt.ulisboa.tecnico.cmov.proj.R;

public class HttpRequestGetPublicAlbumKey extends HttpRequest {

    public HttpRequestGetPublicAlbumKey(Context context) {
        super(context);
    }

    public static void httpRequest(@NonNull String username, @NonNull String sessionId, @NonNull String albumId){
        String finalUrl = ctx.getString(R.string.ppkiIP) + "/publickey/" + username + "/" + sessionId + "/" + albumId;
        android.util.Log.d("debug", "Starting GET request to URL " + finalUrl);
        createHTTPQueue();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, finalUrl, null,
                httpResponse -> {
                    try {
                        setHTTPResponse(httpResponse);
                        android.util.Log.d("debug", httpResponse.toString());
                        if(httpResponse.has("error")) {
                            error = httpResponse.getString("error");
                            android.util.Log.d("debug", "Error");
                            android.util.Log.d("debug", error);
                        }
                        else if(httpResponse.has("success")) {
                            success = httpResponse.getString("success");
                            String publicKeyString = (String) httpResponse.get("publicKey");
                            String privateKeyString = (String) httpResponse.get("privateKey");
                            android.util.Log.d("debug", "Success");
                            android.util.Log.d("debug", success);
                            android.util.Log.d("debug", "Public Key");
                            android.util.Log.d("debug", publicKeyString);
                            android.util.Log.d("debug", "Private Key");
                            android.util.Log.d("debug", privateKeyString);
                            ((HomePage)ctx).updateApplicationLogs("Get Album Public Key", success);
                            if(publicKeyString!=null) {
                                X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(new Gson().fromJson(publicKeyString, byte[].class));
                                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                                ((Peer2PhotoApp) ((HomePage) ctx).getApplication()).addAlbumPublicKey(Integer.valueOf(albumId), keyFactory.generatePublic(pubSpec));
                                PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(new Gson().fromJson(privateKeyString, byte[].class));
                                keyFactory = KeyFactory.getInstance("RSA");
                                ((Peer2PhotoApp) ((HomePage) ctx).getApplication()).addAlbumPrivateKey(Integer.valueOf(albumId), keyFactory.generatePrivate(privSpec));
                                HttpRequestPutSetUrl.httpRequestCode(ctx);
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

}
