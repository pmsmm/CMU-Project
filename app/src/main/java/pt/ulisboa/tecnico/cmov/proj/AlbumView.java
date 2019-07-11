package pt.ulisboa.tecnico.cmov.proj;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.dropbox.core.v2.files.FileMetadata;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import pt.ulisboa.tecnico.cmov.proj.Adapters.PhotoAdapter;
import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;
import pt.ulisboa.tecnico.cmov.proj.Data.Photo;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.DownloadFileTask;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.DropboxClientFactory;
import pt.ulisboa.tecnico.cmov.proj.Dropbox.UploadFileTask;
import pt.ulisboa.tecnico.cmov.proj.HTMLHandlers.HttpRequestGetAlbumPhotos;
import pt.ulisboa.tecnico.cmov.proj.HTMLHandlers.HttpRequestPutAddUserToAlbum;

public class AlbumView extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static ArrayList<Photo> photos = new ArrayList<>();
    private static ArrayAdapter<Photo> photoAdapter = null;

    static final int FIND_USER_REQUEST = 420;
    static final int SELECT_IMAGE_REQUEST = 1234;
    public String URL_BASE;
    public String URL_ALBUM;
    public String URL_ADD_USER_TO_ALBUM;

    protected String albumId = "";
    protected String albumName = "";
    protected ArrayList<String> albumUsers = new ArrayList<String>();
    protected ArrayList<String> addedUsers = new ArrayList<>();

    private boolean usingWifiDirect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);

        usingWifiDirect = savedInstanceState != null && savedInstanceState.getBoolean("isWifi");
        albumUsers = getIntent().getStringArrayListExtra("AlbumUsers");

        URL_BASE = getString(R.string.serverIP);
        URL_ALBUM = URL_BASE + "/album";
        URL_ADD_USER_TO_ALBUM = URL_BASE + "/adduser";

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        albumName = getIntent().getStringExtra("AlbumName");
        albumId = getIntent().getStringExtra("AlbumId");
        toolbar.setTitle(albumName);

        photos.clear();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            final int ACTIVITY_SELECT_IMAGE = 1234;
            startActivityForResult(i, ACTIVITY_SELECT_IMAGE);
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        photoAdapter = new PhotoAdapter(this, 0, photos);
        GridView photoTable = findViewById(R.id.photo_grid);
        photoTable.setAdapter(photoAdapter);

        photoTable.setOnItemClickListener((parent, view, position, id) -> { });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        loadLocalPhotos();
        if (!usingWifiDirect) getRemotePhotos();
    }

    private void addNewPhoto(Bitmap photoBitmap, String photoName) {
        try {
            File photoDir = new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName);
            if (!photoDir.exists()) photoDir.mkdir();
            File newPhoto = new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName + "/" + photoName);
            if (!newPhoto.exists()) {
                if (!newPhoto.createNewFile()) {
                    Log.d("debug", "Failed to create new photo file!");
                }
                FileOutputStream out = new FileOutputStream(getApplicationContext().getFilesDir().getPath() + "/" + albumName + "/" + photoName);
                photoBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
        }
        catch (IOException e) {
            Log.d("debug", "Failed to add local photo file!");
        }
        photos.add(new Photo(photoBitmap));
        photoAdapter.notifyDataSetChanged();
        updateApplicationLogs("Photo Successfully Added to Album", "Add Photo To Album");
    }

    protected void addUserToAlbum(String username) {
        String sessionId = ((Peer2PhotoApp) this.getApplication()).getSessionId();
        new HttpRequestPutAddUserToAlbum(this);
        HttpRequestPutAddUserToAlbum.httpRequest(albumId, ((Peer2PhotoApp) this.getApplication()).getUsername(), sessionId, username, URL_ADD_USER_TO_ALBUM);
    }

    protected void beginAddUser() {
        Intent intent = new Intent(this, FindUsers.class);
        intent.putExtra("AlbumUsers", albumUsers);
        intent.putExtra("AddedUsers", addedUsers);
        startActivityForResult(intent, FIND_USER_REQUEST);
    }

    protected void beginAddPhoto() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(i, SELECT_IMAGE_REQUEST);
    }

    protected void processExit(boolean shouldSignOut) {
        String userResult = "";
        for (String user : addedUsers) userResult += (user + ",");
        Intent intent = new Intent();
        intent.putExtra("Users", userResult);
        intent.putExtra("AlbumName", albumName);
        intent.putExtra("shouldSignOut", shouldSignOut);
        setResult(Activity.RESULT_OK, intent);
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FIND_USER_REQUEST) {
            if (resultCode == RESULT_OK) {
                String username = data.getExtras().getString("userName");
                addUserToAlbum(username);
            }
        }
        else if (requestCode == SELECT_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                assert selectedImage != null;
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                assert cursor != null;
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();

                Bundle b = getIntent().getExtras();
                String name = "ERROR"; // or other values
                if (b != null)
                    name = b.getString("AlbumName");

                String PhotoName = name + "_Photo_" + new Random().nextInt();

                if (!usingWifiDirect) {
                    new UploadFileTask(AlbumView.this, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
                        @Override
                        public void onUploadComplete(FileMetadata result) {

                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    }).execute( PhotoName,
                                "/Peer2Photo",
                                "NEW_PHOTO",
                                filePath,
                                name,
                                ((Peer2PhotoApp) getApplication()).getSessionId(),
                                ((Peer2PhotoApp) getApplication()).getUsername(),
                                albumId);
                }
                else {
                    try {
                        //File localSlice = new File(getApplicationContext().getFilesDir().getPath() + "/" + name + "/" + name + ".txt");
                        File photosFile = new File(getApplicationContext().getFilesDir().getPath() + "/" + name + "/" + name + "_LOCAL.txt");
                        BufferedWriter out = new BufferedWriter(new FileWriter(photosFile, true));
                        out.write(filePath + "\n");
                        out.flush();
                        out.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                imageScalingAndPosting(filePath);
            }
        }
    }

    public void imageScalingAndPosting(String filePath){
        Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);

        final int maxSize = 628;
        int outWidth;
        int outHeight;
        int inWidth = yourSelectedImage.getWidth();
        int inHeight = yourSelectedImage.getHeight();
        if(inWidth > inHeight){
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }

        Bitmap scaled = Bitmap.createScaledBitmap(yourSelectedImage, outWidth, outHeight, false);

        String[] splitPath = filePath.split("/");
        addNewPhoto(scaled, splitPath[splitPath.length-1]);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            setResult(Activity.RESULT_CANCELED);
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.album_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        String option = (String) item.getTitleCondensed();

        //noinspection SimplifiableIfStatement
        if (option.equals("Add User")) {
            beginAddUser();
            return true;
        }
        else if (option.equals("Add Photo")) {
            beginAddPhoto();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            super.onBackPressed();
        } else if (id == R.id.nav_logs) {
            startActivity(new Intent(this, LogView.class));
        } else if (id == R.id.nav_signOut) {
            processExit(true);
        } else if (id == R.id.nav_clean) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadLocalPhotos(){
        File localPhotoPaths = new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName + "/" + albumName + "_LOCAL.txt");

        ArrayList<String> photoNames = new ArrayList<>();
        if(localPhotoPaths.isFile()){
            try{
                BufferedReader br = new BufferedReader(new FileReader(localPhotoPaths));
                String line;
                while ((line = br.readLine()) != null) {
                    photoNames.add(line.substring(line.lastIndexOf('/')+1));
                    imageScalingAndPosting(line);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else {
            android.util.Log.d("debug", "IT IS NOT A FILE!!");
        }

        //Check local photo files
        File localAlbumDir = new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName);
        if (localAlbumDir.exists()) {
            File[] localFiles = localAlbumDir.listFiles();
            for (File file : localFiles) {
                String filename = file.getName();
                if (photoNames.contains(filename)) continue;
                if (filename.equals(albumName + ".txt") || filename.equals(albumName + "_LOCAL.txt"))
                    continue;
                photoNames.add(filename);
                imageScalingAndPosting(file.getPath());
            }
        }
    }

    private void getRemotePhotos(){
        String SessionId = ((Peer2PhotoApp) (getApplication())).getSessionId();
        String Username = ((Peer2PhotoApp) (getApplication())).getUsername();

        File imageRoot = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), getString(R.string.app_directory_name) + "/" + albumName);

        if(imageRoot.exists() && imageRoot.isDirectory()){
            try{
                File[] files = imageRoot.listFiles();
                for (File file : files) {
                    imageScalingAndPosting(file.getPath());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        new HttpRequestGetAlbumPhotos(this);
        HttpRequestGetAlbumPhotos.httpRequest(albumId, Username, SessionId, URL_ALBUM);

    }

    public void urlParser(String usersString, JSONObject mapResponse){

        new DownloadFileTask(AlbumView.this, DropboxClientFactory.getClient(), new DownloadFileTask.Callback(){
            @Override
            public void onDownloadComplete(File file) {

            }

            @Override
            public void onError(Exception e) {

            }
        }).execute(usersString, ((Peer2PhotoApp)getApplication()).getUsername(), mapResponse.toString(), getApplicationContext().getFilesDir().getPath(), getString(R.string.app_directory_name));

    }

    public void updateApplicationLogs(@NonNull String operationResult, @NonNull String operation){
        String Operation = "OPERATION: " + operation + "\n";
        String timeStamp = "TIMESTAMP: " + new Date().toString() + "\n";
        String result = "RESULT: " + operationResult + "\n";

        ((Peer2PhotoApp)getApplication()).updateLog(Operation + timeStamp + result);

    }

    public PrivateKey getPrivateKey(int albumId) {
        return ((Peer2PhotoApp)getApplication()).getAlbumPrivateKey(albumId);
    }

}
