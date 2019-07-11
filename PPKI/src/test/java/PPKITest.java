import com.google.gson.Gson;
import com.squareup.okhttp.*;
import io.javalin.Javalin;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.KeyFactory;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

public class PPKITest {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String URL_BASE_SERVER = "http://localhost:8080";
    private static final String URL_SIGNUP = URL_BASE_SERVER + "/signup";
    private static final String URL_LOGIN = URL_BASE_SERVER + "/login";
    private static final String URL_CREATE_ALBUM = URL_BASE_SERVER + "/createalbum";

    private static final String URL_BASE_PPKI = "http://localhost:9090";
    private static final String URL_GET_PUBLICKEY = URL_BASE_PPKI + "/publickey";
    private static final String URL_GET_PRIVATEKEY = URL_BASE_PPKI + "/privatekey";

    private KeyStoreInterface keyStoreInterface;
    private Javalin app;
    private OkHttpClient client = null;
    private String username = "userPPKI";
    private String password = "passPPKI";
    private String albumName = "albumPPKI";
    private int sessionId = 0;
    private int albumId = 0;

    @Before
    public void setUp() {
        try {
            //Setting up PPKI

            JavalinApp javalinApp = new JavalinApp();
            this.app = javalinApp.init();
            this.client = new OkHttpClient();

            //Setting up server

            //Signup
            HashMap<String, String> mapRequest = new HashMap<>();
            mapRequest.put("username", username);
            mapRequest.put("password", password);
            RequestBody body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            Request request = new Request.Builder().url(URL_SIGNUP).post(body).build();
            client.newCall(request).execute();

            //Login
            body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            request = new Request.Builder().url(URL_LOGIN).put(body).build();
            Response response = client.newCall(request).execute();
            HashMap mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            sessionId = Integer.valueOf((String)mapResponse.get("sessionId"));

            //Create album
            mapRequest.put("sessionId", String.valueOf(sessionId));
            mapRequest.put("albumName", albumName);
            body = RequestBody.create(JSON, new Gson().toJson(mapRequest));
            request = new Request.Builder().url(URL_CREATE_ALBUM).post(body).build();
            response = client.newCall(request).execute();
            mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            albumId = Integer.valueOf((String)mapResponse.get("albumId"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This test will only pass if server is running
    @Test
    public void ppkiTest() {
        try {
            String publicKey;
            String privateKey;

            //Get album public key
            String URL = URL_GET_PUBLICKEY + "/" + username + "/" + sessionId + "/" + albumId;
            Request request = new Request.Builder().url(URL).get().build();
            Response response = client.newCall(request).execute();
            HashMap mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Public key obtained successfully", mapResponse.get("success"));
            publicKey = (String) mapResponse.get("publicKey");
            Assert.assertNotNull(publicKey);
            Assert.assertNull(mapResponse.get("error"));

            //Get album private key
            URL = URL_GET_PRIVATEKEY + "/" + username + "/" + sessionId + "/" + albumId;
            request = new Request.Builder().url(URL).get().build();
            response = client.newCall(request).execute();
            mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            Assert.assertEquals("Private key obtained successfully", mapResponse.get("success"));
            privateKey = (String) mapResponse.get("privateKey");
            Assert.assertNotNull(privateKey);
            Assert.assertNull(mapResponse.get("error"));

            //Ensure they belong to the same pair

            //Create signature
            Signature sign = Signature.getInstance("SHA512withRSA");
            PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(new Gson().fromJson(privateKey, byte[].class));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            sign.initSign(keyFactory.generatePrivate(privSpec));
            byte[] bytes = "message".getBytes();
            sign.update(bytes);
            byte[] signature = sign.sign();

            //Verify signature
            sign = Signature.getInstance("SHA512withRSA");
            X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(new Gson().fromJson(publicKey, byte[].class));
            keyFactory = KeyFactory.getInstance("RSA");
            sign.initVerify(keyFactory.generatePublic(pubSpec));
            sign.update("message".getBytes());

            Assert.assertTrue(sign.verify(signature));

            //Ensure PPKI returns same keys for same album

            //Get album public key
            URL = URL_GET_PUBLICKEY + "/" + username + "/" + sessionId + "/" + 1;
            request = new Request.Builder().url(URL).get().build();
            response = client.newCall(request).execute();
            mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            String publicKey1 = (String) mapResponse.get("publicKey");
            request = new Request.Builder().url(URL).get().build();
            response = client.newCall(request).execute();
            mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            String publicKey2 = (String) mapResponse.get("publicKey");
            Assert.assertEquals(publicKey1, publicKey2);

            //Get album private key
            URL = URL_GET_PRIVATEKEY + "/" + username + "/" + sessionId + "/" + 1;
            request = new Request.Builder().url(URL).get().build();
            response = client.newCall(request).execute();
            mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            String privateKey1 = (String) mapResponse.get("privateKey");
            request = new Request.Builder().url(URL).get().build();
            response = client.newCall(request).execute();
            mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            String privateKey2 = (String) mapResponse.get("privateKey");
            Assert.assertEquals(privateKey1, privateKey2);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @After
    public void tearDown() {
        keyStoreInterface = null;
        this.app = null;
        sessionId = 0;
        albumId = 0;
    }
}
