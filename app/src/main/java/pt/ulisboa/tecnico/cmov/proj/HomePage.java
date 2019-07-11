package pt.ulisboa.tecnico.cmov.proj;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.v2.files.FileMetadata;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import pt.ulisboa.tecnico.cmov.proj.Adapters.AlbumAdapter;
import pt.ulisboa.tecnico.cmov.proj.Data.Album;
import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.DropboxActivity;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.DropboxClientFactory;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.UploadFileTask;
import pt.ulisboa.tecnico.cmov.proj.HTMLHandlers.HttpRequestDeleteSession;
import pt.ulisboa.tecnico.cmov.proj.HTMLHandlers.HttpRequestGetUserAlbums;
import pt.ulisboa.tecnico.cmov.proj.HTMLHandlers.HttpRequestPostCreateAlbum;

public class HomePage extends DropboxActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private final static String ACCESS_KEY = "ktxcdvzt610l2ao";
    private final static String ACCESS_SECRET = "wurqteptiyuh9s2";

    public final static int ALBUM_VIEW_REQUEST = 3234;

    //public static final String URL_BASE = "http://localhost:8080";
    public String URL_BASE;
    public String URL_CREATE_ALBUM;
    public String URL_LOAD_ALBUMS;
    public String URL_SIGNOUT;

    protected boolean usingWifiDirect = false;

    protected static ArrayList<Album> albums = new ArrayList<>();
    protected static ArrayAdapter<Album> albumAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usingWifiDirect = savedInstanceState != null && savedInstanceState.getBoolean("isWifi");

        setContentView(usingWifiDirect ? R.layout.activity_home_page_wifi : R.layout.activity_home_page);

        URL_BASE = getString(R.string.serverIP);
        URL_CREATE_ALBUM = URL_BASE + "/createalbum";
        URL_LOAD_ALBUMS = URL_BASE + "/useralbums";
        URL_SIGNOUT = URL_BASE + "/logout";

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Home");

        albums.clear();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> createAlbumByUser());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        albumAdapter = new AlbumAdapter(this, 0, albums);
        GridView albumTable = findViewById(R.id.album_grid);
        albumTable.setAdapter(albumAdapter);

        loadAlbums();

        albumTable.setOnItemClickListener((parent, view, position, id) -> {
            enterAlbum(position);
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(getApplicationContext(), "Application required to write to storage", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        }
    }

    protected void enterAlbum(int position) {
        Intent intent = new Intent(this, AlbumView.class);
        Bundle b = new Bundle();
        b.putString("AlbumId", albums.get(position).getAlbumId());
        b.putString("AlbumName", albums.get(position).getAlbumName());
        intent.putExtras(b);
        startActivityForResult(intent, ALBUM_VIEW_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Application will not run without write storage permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logs) {
            startActivity(new Intent(this, LogView.class));
        } else if (id == R.id.nav_signOut) {
            signOut();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void signOut() {
        String sessionId = ((Peer2PhotoApp) this.getApplication()).getSessionId();
        new HttpRequestDeleteSession(this);
        HttpRequestDeleteSession.httpRequest(sessionId, URL_SIGNOUT);
    }

    public void confirmSignOut() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onResume(){
        super.onResume();

        if(hasToken()){
            Toast.makeText(this, "You Are Now Logged In To Your Dropbox", Toast.LENGTH_LONG).show();
            TextView username = findViewById(R.id.UsernameDisplay);
            TextView mail = findViewById(R.id.MailDisplay);
        }
    }

    @Override
    protected void loadData() {
    }

    public void addNewAlbum(String albumId, String albumName) {
        for (Album album : albums) if (album.getAlbumName().equals(albumName)) return;

        albums.add(new Album(albumId, albumName, R.drawable.empty_thumbnail));
        albumAdapter.notifyDataSetChanged();
    }

    private void createAlbumByUser(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Album Title");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String albumName = input.getText().toString();
            String sessionId = ((Peer2PhotoApp) getApplication()).getSessionId();
            String username = ((Peer2PhotoApp) getApplication()).getUsername();

            if(new File(getApplicationContext().getFilesDir().getPath() + "/" + input.getText().toString()).exists()){
                while (new File(getApplicationContext().getFilesDir().getPath() + "/" + input.getText().toString()).exists()){
                    Toast.makeText(this, "An Album With The Name " + input.getText().toString() + " Already Exists!", Toast.LENGTH_SHORT).show();
                    builder.setView(input);
                    builder.show();
                }
            }

            new HttpRequestPostCreateAlbum(this);
            HttpRequestPostCreateAlbum.httpRequest(username, sessionId, albumName, URL_CREATE_ALBUM);

        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void processNewAlbum(String albumName, String albumId) {
        createAlbumInCloud(albumName, albumId);
        addNewAlbum(albumId, albumName);
    }

    public void createAlbumInCloud(String albumName, String albumId){
        if (!usingWifiDirect) {
            new UploadFileTask(this, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {

                @Override
                public void onUploadComplete(FileMetadata result) {
                    Toast.makeText(HomePage.this, "Upload Complete!", Toast.LENGTH_SHORT).show();
                    ((Peer2PhotoApp) getApplication()).addAlbum(albumId, albumName, getApplicationContext().getFilesDir().getPath() + "/albums.txt");
                }

                @Override
                public void onError(Exception e) {
                }
            }).execute( albumName,
                        "/Peer2Photo",
                        "NEW_ALBUM",
                        ((Peer2PhotoApp) getApplication()).getSessionId(),
                        ((Peer2PhotoApp) getApplication()).getUsername(),
                        albumId);
        }
        else {
            createAlbumFolders(albumName, albumId);
        }
    }

    private void createAlbumFolders(String albumName, String albumId) {
        File localFile = new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName);
        File file = new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName + "/" + albumName + ".txt");
        File localPhotosFile = new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName+ "/" + albumName + "_LOCAL.txt");
        ((Peer2PhotoApp) getApplication()).addAlbum(albumId, albumName, getApplicationContext().getFilesDir().getPath() + "/albums.txt");
        try{
            if(localFile.mkdir()){
                System.out.println("Folder Created Successfuly!");
                if(file.createNewFile()){
                    System.out.println("Album File Created Successfully!");
                }else {
                    System.out.println("Failed To Create Album File!");
                }

                if (localPhotosFile.createNewFile()){
                    System.out.println("The Local Photos File Was Created Successfuly!");
                }else{
                    System.out.println("The Local Photos File Already Exists!");
                }
            }
            else {
                System.out.println("The File Already Exists in Local Storage. Perfoming Update...");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadAlbums() {
        File[] directories = new File(getApplicationContext().getFilesDir().getPath()).listFiles(File::isDirectory);

        if(!(directories.length == 0)){
            for (File i : directories){
                String albumName = i.getName();
                String albumId = ((Peer2PhotoApp) getApplication()).getAlbumId(albumName);
                addNewAlbum(albumId, albumName);
            }
        }

        updateApplicationLogs("List User Albums", "Local Albums Loaded Successfully");

        new HttpRequestGetUserAlbums(this);
        HttpRequestGetUserAlbums.httpRequest(((Peer2PhotoApp)getApplication()).getUsername(), ((Peer2PhotoApp)getApplication()).getSessionId(), URL_LOAD_ALBUMS);
    }


    public void parseAlbumNames(String[] albumIds, JSONObject httpResponse) {
        // 3\\ cases - User was not added to third party albums;
        // User was added to album with same name as another album user already has;
        // User was added to album with a name different of all user's albums
        try{
            for (String albumId : albumIds) {
                String albumName = httpResponse.getString(albumId);
                if (((Peer2PhotoApp) getApplication()).getAlbumId(albumName) == null) {
                    if (!(new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName).exists())) {
                        createAlbumInCloud(albumName, albumId);
                        addNewAlbum(albumId, albumName);
                        Log.d("debug", "User has been added to album of other user and its name does not exist in user's albums");
                    } else {
                        File fileToDelete = new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName);
                        if (fileToDelete.delete()) {
                            createAlbumInCloud(albumName, albumId);
                            addNewAlbum(albumId, albumName);
                            Log.d("debug", "User has been added to album of other user and its name does not exist in user's albums");
                        }
                    }
                } else {
                    if (!((Peer2PhotoApp) getApplication()).getAlbumId(albumName).equals(albumId)) {
                        String newName = albumName + "_" + albumId;
                        createAlbumInCloud(newName, albumId);
                        addNewAlbum(albumId, newName);
                        Log.d("debug", "User has been added to album of other user with name equal to one of user's albums");
                    } else {
                        if(!new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName).exists()){
                            createAlbumInCloud(albumName, albumId);
                            addNewAlbum(albumId, albumName);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void updateApplicationLogs(@NonNull String operation, @NonNull String operationResult){
        String Operation = "OPERATION: " + operation + "\n";
        String timeStamp = "TIMESTAMP: " + new Date().toString() + "\n";
        String result = "RESULT: " + operationResult + "\n";

        ((Peer2PhotoApp)getApplication()).updateLog(Operation + timeStamp + result);

    }

}