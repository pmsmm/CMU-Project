package pt.ulisboa.tecnico.cmov.proj;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import pt.ulisboa.tecnico.cmov.proj.Adapters.UserAdapter;
import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;
import pt.ulisboa.tecnico.cmov.proj.Data.User;
import pt.ulisboa.tecnico.cmov.proj.HTMLHandlers.HttpRequestGetAllUsers;

public class FindUsers extends AppCompatActivity {

    private static ArrayList<User> users = new ArrayList<>();
    private static ArrayAdapter<User> userAdapter = null;

    protected ArrayList<String> albumUsers = new ArrayList<>();
    protected ArrayList<String> addedUsers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_find_users);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Find Users");

        albumUsers = getIntent().getStringArrayListExtra("AlbumUsers");
        addedUsers = getIntent().getStringArrayListExtra("AddedUsers");

        users.clear();

        String URL_BASE = getString(R.string.serverIP);
        String URL_GET_ALL_USERS = URL_BASE + "/users";

        String sessionId = ((Peer2PhotoApp)getApplication()).getSessionId();
        String username = ((Peer2PhotoApp)getApplication()).getUsername();
        String URL = URL_GET_ALL_USERS + "/" + sessionId + "/" + username;

        new HttpRequestGetAllUsers(this);
        HttpRequestGetAllUsers.httpRequest(URL);

        userAdapter = new UserAdapter(this, 0, users);
        ListView userTable = findViewById(R.id.userList);
        userTable.setAdapter(userAdapter);

        userTable.setOnItemClickListener((parent, view, position, id) -> addUser(position));
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(Activity.RESULT_CANCELED, intent);
        super.onBackPressed();
    }

    private void addUser(int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(FindUsers.this);
        builder.setTitle("Add " + users.get(position).getUserName() + " to album?");

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            Intent intent = new Intent();
            intent.putExtra("userName", users.get(position).getUserName());
            setResult(Activity.RESULT_OK, intent);
            finish();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void updateApplicationLogs(@NonNull String operationResult){
        String Operation = "OPERATION: List All Users" + "\n";
        String timeStamp = "TIMESTAMP: " + new Date().toString() + "\n";
        String result = "RESULT: " + operationResult + "\n";

        ((Peer2PhotoApp)getApplication()).updateLog(Operation + timeStamp + result);

    }

    public void parseUsers(@NonNull String allUsers){
        users.clear();
        String[] parsedUsers = allUsers.split(",");
        for (String user : parsedUsers) {
            if (albumUsers == null || addedUsers == null) {
                users.add(new User(0, user));
                continue;
            }
            if (!albumUsers.contains(user) && !addedUsers.contains(user)) users.add(new User(0, user));
        }
        userAdapter.notifyDataSetChanged();
    }

}
