package pt.ulisboa.tecnico.cmov.proj.HTMLHandlers;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Date;

public abstract class HttpRequest {

    static String success;
    static String error;

    static Context ctx;
    static RequestQueue queue = null;
    private static JSONObject httpResponse = null;

    HttpRequest(Context context){
        ctx = context;
        System.out.println("Starting HTTP Request To Server");
    }

    public static void httpRequest(String... params) {
    }

    static void setHTTPResponse(JSONObject json) {
        httpResponse = json;
    }

    static void cleanHTTPResponse() {
        success = null;
        error = null;
        httpResponse = null;
        android.util.Log.d("debug", "Cleaned " + new Date().getTime());
    }

    static void createHTTPQueue() {
        if(queue == null) {
            queue = Volley.newRequestQueue(ctx);
        }
    }

}
