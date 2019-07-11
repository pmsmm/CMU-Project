package pt.ulisboa.tecnico.cmov.proj;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import pt.ulisboa.tecnico.cmov.proj.Data.Album;
import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;

public class HomePage_Wifi extends HomePage {

    private TermiteComponent termite = null;

    private static final int CACHE_SIZE = 333;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle bundle = savedInstanceState != null ? savedInstanceState : new Bundle();
        bundle.putBoolean("isWifi", true);
        termite = new TermiteComponent(this, getApplication(), getMainLooper());

        super.onCreate(bundle);

        FloatingActionButton updateFab = findViewById(R.id.updateFab);
        updateFab.setOnClickListener(view -> updateAlbums());
    }

    protected void onNewIntent(Intent intent) {
        //Process album from termite
        super.onNewIntent(intent);
        setIntent(intent);
        int code = intent.getExtras().getInt("Code");
        if (code == 0) {
            String albumId = intent.getExtras().getString("AlbumId");
            String albumName = intent.getExtras().getString("AlbumName");
            int numPhotos = intent.getExtras().getInt("NumPhotos");
            boolean isNew = intent.getExtras().getBoolean("IsNew");
            if (!isNew) processNewAlbum(albumName, albumId);
            Toast.makeText(getApplicationContext(), "Received " + numPhotos + " new photos for " + albumName, Toast.LENGTH_SHORT).show();
        }
        else if (code == 1) {
            String albumId = intent.getExtras().getString("AlbumId");
            String albumName = intent.getExtras().getString("AlbumName");
            int numPhotos = intent.getExtras().getInt("NumPhotos");
            processNewAlbum(albumName, albumId);
            Toast.makeText(getApplicationContext(), "Sent " + numPhotos + " new photos for " + albumName, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void signOut() {
        termite.destroy();
        super.signOut();
    }

    @Override
    protected void onDestroy() {
        termite.destroy();
        super.onDestroy();
    }

    public void updateAlbums() {
        termite.clearData();
        termite.requestPeers();
    }

    @Override
    protected void enterAlbum(int position) {
        Intent intent = new Intent(this, usingWifiDirect ? AlbumView_Wifi.class :  AlbumView.class);
        String albumId = albums.get(position).getAlbumId();
        String albumName = albums.get(position).getAlbumName();
        intent.putExtra("AlbumId", albumId);
        intent.putExtra("AlbumName", albumName);
        intent.putExtra("AlbumUsers", termite.albumName_User_Map.get(albumName));
        startActivityForResult(intent, ALBUM_VIEW_REQUEST);
    }

    @Override
    public void addNewAlbum(String albumId, String albumName) {
        super.addNewAlbum(albumId, albumName);
        termite.addNewAlbum(albumId, albumName);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logs) {
            startActivity(new Intent(this, LogView.class));
        } else if (id == R.id.nav_signOut) {
            signOut();
        } else if (id == R.id.nav_clean){
            Intent intent = new Intent(this, CacheSize.class);
            startActivityForResult(intent, CACHE_SIZE);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ALBUM_VIEW_REQUEST) {
            if (resultCode == RESULT_OK) {
                String usernames = data.getExtras().getString("Users");
                if (!usernames.equals("")) {
                    String albumName = data.getExtras().getString("AlbumName");
                    String[] users = usernames.split(",");
                    for (String user : users) {
                        termite.albumName_User_Map.get(albumName).add(user);
                    }
                }
                if (data.getExtras().getBoolean("shouldSignOut")) signOut();
            }
        }
        else if (requestCode == CACHE_SIZE) {
            int cacheSize = data.getExtras().getInt("cacheSize");
            cleanPhotos(cacheSize);
        }
    }


    private void cleanPhotos(int maxSize) {
        try {
            long totalSize = 0;
            ArrayList<File> deleteCandidate = new ArrayList<>();
            for (Album album : albums) {
                String albumName = album.getAlbumName();
                File localAlbumDir = new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName);
                if (localAlbumDir.exists()) {
                    File[] localFiles = localAlbumDir.listFiles();
                    File localPhotosFile = new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName + "/" + albumName + "_LOCAL.txt");
                    for (File file : localFiles) {
                        String filename = file.getName();
                        if (filename.equals(albumName + ".txt") || filename.equals(albumName + "_LOCAL.txt"))
                            continue;
                        boolean shouldAdd = true;
                        List<String> localPhotos = FileUtils.readLines(localPhotosFile);
                        for (String path : localPhotos) {
                            String localPhotoName = path.substring(path.lastIndexOf('/')+1);
                            if (localPhotoName.equals(file.getName())) {
                                shouldAdd = false;
                                break;
                            }
                        }
                        if (!shouldAdd) continue;

                        totalSize += file.length() / 1000;
                        boolean deleted = deleteCandidate.add(file);
                        if (deleted) {
                            deleted = true;
                        }
                    }
                }
            }
            if (totalSize > maxSize) {
                int numDeletedFiles = 0;
                for (File candidate : deleteCandidate) {
                    totalSize -= candidate.length() / 1000;
                    candidate.delete();
                    numDeletedFiles++;
                    if (totalSize <= maxSize) {
                        Toast.makeText(getApplicationContext(), "Deleted " + numDeletedFiles + " remote photos", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }
        catch (IOException e) {
            Log.d("debug", "Failed to read file while cleaning!");
        }
    }

    @Override
    public void parseAlbumNames(String[] albumIds, JSONObject httpResponse) {
        // 3\\ cases - User was not added to third party albums;
        // User was added to album with same name as another album user already has;
        // User was added to album with a name different of all user's albums
        try{
            HashMap<String, ArrayList<String>> albumName_User_Map = new HashMap<>();
            for (String albumId : albumIds) {
                String albumName = httpResponse.getString(albumId);
                String users = httpResponse.getString("Users_" + albumId);
                String[] usersArray = users.split(",");
                albumName_User_Map.put(albumName, new ArrayList<>(Arrays.asList(usersArray)));
                if (((Peer2PhotoApp) getApplication()).getAlbumId(albumName) == null) {
                    if (!(new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName).exists())) {
                        addNewAlbum(albumId, albumName);
                        Log.d("debug", "User has been added to album of other user and its name does not exist in user's albums");
                    } else {
                        File fileToDelete = new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName);
                        if (fileToDelete.delete()) {
                            addNewAlbum(albumId, albumName);
                            Log.d("debug", "User has been added to album of other user and its name does not exist in user's albums");
                        }
                    }
                } else {
                    if (!((Peer2PhotoApp) getApplication()).getAlbumId(albumName).equals(albumId)) {
                        String newName = albumName + "_" + albumId;
                        addNewAlbum(albumId, newName);
                        Log.d("debug", "User has been added to album of other user with name equal to one of user's albums");
                    } else {
                        if(!new File(getApplicationContext().getFilesDir().getPath() + "/" + albumName).exists()){
                            addNewAlbum(albumId, albumName);
                        }
                    }
                }
            }
            termite.albumName_User_Map = albumName_User_Map;

            Toast.makeText(HomePage_Wifi.this, "Updated albums",
                    Toast.LENGTH_LONG).show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}