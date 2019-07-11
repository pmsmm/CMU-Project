import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

public class KeyStoreInterface {

    public static final String SERVER_URL = "http://localhost:8080";

    // Main methods

    HashMap<String, String> getPublicKey(String username, int sessionId, int albumId) {
        File file = new File("Album" + albumId + ".pub");
        if(!file.exists())
            addKeyPairToKeyStore(albumId);

        HashMap<String, String> response = new HashMap<>();
        try {
            if(!isUserInAlbum(username, sessionId, albumId)) {
                response.put("error", "User is not in album or user albums could not be obtained");
                return response;
            }
            byte[] keyBytes = Files.readAllBytes(Paths.get("Album" + albumId + ".pub"));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);

            keyBytes = Files.readAllBytes(Paths.get("Album" + albumId + ".key"));
            PKCS8EncodedKeySpec spec2 = new PKCS8EncodedKeySpec(keyBytes);
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(spec2);

            response.put("success", "Public key obtained successfully");
            response.put("publicKey", new Gson().toJson(publicKey.getEncoded()));
            response.put("privateKey", new Gson().toJson(privateKey.getEncoded()));
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Exception while getting public key of album" + albumId);
            return response;
        }
    }

    HashMap<String, String> getPrivateKey(String username, int sessionId, int albumId) {
        File file = new File("Album" + albumId + ".key");
        if(!file.exists())
            addKeyPairToKeyStore(albumId);

        HashMap<String, String> response = new HashMap<>();
        try {
            if(!isUserInAlbum(username, sessionId, albumId)) {
                response.put("error", "User is not in album or user albums could not be obtained");
                return response;
            }
            byte[] keyBytes = Files.readAllBytes(Paths.get("Album" + albumId + ".key"));

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);

            response.put("success", "Private key obtained successfully");
            response.put("privateKey", new Gson().toJson(privateKey.getEncoded()));
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Exception while getting private key of album" + albumId);
            return response;
        }
    }

    // Auxiliary methods

    private void addKeyPairToKeyStore(int albumId) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(4096);
            KeyPair pair = kpg.generateKeyPair();

            FileOutputStream out = new FileOutputStream("Album" + albumId + ".key");
            out.write(pair.getPrivate().getEncoded());
            out.close();

            out = new FileOutputStream("Album" + albumId + ".pub");
            out.write(pair.getPublic().getEncoded());
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Could not create key pair");
        }
    }

    private boolean isUserInAlbum(String username, int sessionId, int albumId) {
        try {
            String URL = SERVER_URL + "/useralbums/" + sessionId + "/" + username;
            Request request = new Request.Builder().url(URL).get().build();
            Response response = new OkHttpClient().newCall(request).execute();

            HashMap<String, String> mapResponse = new Gson().fromJson(response.body().string(), HashMap.class);
            return mapResponse.get(String.valueOf(albumId)) != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
