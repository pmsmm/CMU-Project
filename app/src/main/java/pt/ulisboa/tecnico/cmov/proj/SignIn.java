package pt.ulisboa.tecnico.cmov.proj;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.util.Date;

import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;
import pt.ulisboa.tecnico.cmov.proj.HTMLHandlers.HttpRequestPutSignIn;


public class SignIn extends AppCompatActivity {

    //public static final String URL_BASE = "http://localhost:8080";
    public String URL_BASE;
    public String URL_SIGNIN;

    private EditText usernameView;
    private EditText passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        URL_BASE = getString(R.string.serverIP);
        URL_SIGNIN = URL_BASE + "/login";

        usernameView = findViewById(R.id.username_login);
        passwordView = findViewById(R.id.password_login);

        Button signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(v -> signIn());
    }

    private void signIn() {
        // Reset errors.
        usernameView.setError(null);
        passwordView.setError(null);

        new HttpRequestPutSignIn(this);
        HttpRequestPutSignIn.httpRequest(usernameView.getText().toString(), passwordView.getText().toString(), URL_SIGNIN);
    }

    public void InitialVariableSetup(String sessionId){
        ((Peer2PhotoApp) this.getApplication()).setUsername(((EditText)findViewById(R.id.username_login)).getText().toString());
        ((Peer2PhotoApp) this.getApplication()).setPassword(((EditText)findViewById(R.id.password_login)).getText().toString());
        ((Peer2PhotoApp) this.getApplication()).setSessionId(sessionId);
        ((Peer2PhotoApp) this.getApplication()).getAlbums(getApplicationContext().getFilesDir().getPath() + "/albums.txt");

        try {
            String rootPhotoDirectoryPath = getString(R.string.app_directory_name);
            File rootPhotoDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), rootPhotoDirectoryPath);
            if (!rootPhotoDirectory.exists() || !rootPhotoDirectory.isDirectory()) {
                if (rootPhotoDirectory.mkdir()) {
                    System.out.println("Directory Created Successfully");
                } else {
                    throw new Exception("Failed to Create Photos Directory");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, ModeChoosing.class);
        startActivity(intent);

    }

    public void updateApplicationLogs(@NonNull String operationResult){
        String Operation = "OPERATION: Sign In" + "\n";
        String timeStamp = "TIMESTAMP: " + new Date().toString() + "\n";
        String result = "RESULT: " + operationResult + "\n";

        ((Peer2PhotoApp)getApplication()).updateLog(Operation + timeStamp + result);

    }

}