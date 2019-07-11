package pt.ulisboa.tecnico.cmov.proj;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;

import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;
import pt.ulisboa.tecnico.cmov.proj.HTMLHandlers.HttpRequestPostSignUp;

public class SignUp extends AppCompatActivity {

    //public static final String URL_BASE = "http://localhost:8080";
    public String URL_BASE;
    public String URL_SIGNUP;

    private EditText UsernameView;
    private EditText PasswordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        URL_BASE = getString(R.string.serverIP);
        URL_SIGNUP = URL_BASE + "/signup";

        UsernameView = findViewById(R.id.username_signup);
        PasswordView = findViewById(R.id.password_signup);

        Button SignUpButton = findViewById(R.id.sign_up_button);
        SignUpButton.setOnClickListener(v -> {
            UsernameView.setError(null);
            PasswordView.setError(null);

            new HttpRequestPostSignUp(this);
            HttpRequestPostSignUp.httpRequest(UsernameView.getText().toString(), PasswordView.getText().toString(), URL_SIGNUP);
        });
    }

    public void startActivityOnSuccess(){
        Intent intent = new Intent(this, SignIn.class);
        startActivity(intent);
    }

    public void updateApplicationLogs(@NonNull String operationResult){
        String Operation = "OPERATION: Sign In" + "\n";
        String timeStamp = "TIMESTAMP: " + new Date().toString() + "\n";
        String result = "RESULT: " + operationResult + "\n";

        ((Peer2PhotoApp)getApplication()).updateLog(Operation + timeStamp + result);

    }

}
