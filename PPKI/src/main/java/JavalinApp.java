import io.javalin.Javalin;

import java.util.HashMap;

public class JavalinApp {

    public static final int PORT = 9090;

    public JavalinApp() {

    }

    public Javalin init() {

        KeyStoreInterface iKeyStore = new KeyStoreInterface();

        Javalin app = Javalin.create().start(PORT);

        app.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(400);
            ctx.result("Invalid request");
        });

        app.error(404, ctx -> {
            ctx.result("This link does not exist");
        });

        HashMap<String, String> hashRoot = new HashMap<String, String>();
        hashRoot.put("PPKI", "OK");
        // Requests to the root path will return the JSON { "status" : "OK" }
        app.get("/", ctx -> ctx.json(hashRoot));

        app.get("/publickey/:username/:session-id/:album-id", ctx -> {
            HashMap<String, String> mapResponse = iKeyStore.getPublicKey(ctx.pathParam("username"), Integer.valueOf(ctx.pathParam("session-id")), Integer.valueOf(ctx.pathParam("album-id")));
            System.out.println("HTTP success: " + mapResponse.get("success"));
            System.out.println("HTTP error: " + mapResponse.get("error"));
            System.out.println("HTTP public key: " + mapResponse.get("publicKey"));
            System.out.println("HTTP private key: " + mapResponse.get("privateKey"));
            ctx.json(mapResponse);
        });

        app.get("/privatekey/:username/:session-id/:album-id", ctx -> {
            HashMap<String, String> mapResponse = iKeyStore.getPrivateKey(ctx.pathParam("username"), Integer.valueOf(ctx.pathParam("session-id")), Integer.valueOf(ctx.pathParam("album-id")));
            System.out.println("HTTP success: " + mapResponse.get("success"));
            System.out.println("HTTP error: " + mapResponse.get("error"));
            System.out.println("HTTP private key: " + mapResponse.get("privateKey"));
            ctx.json(mapResponse);
        });

        return app;
    }
}
