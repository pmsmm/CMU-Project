package pt.ulisboa.tecnico.cmov.proj;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;
import pt.ulisboa.tecnico.cmov.proj.Data.BitmapDataObject;
import pt.ulisboa.tecnico.cmov.proj.Data.Peer2PhotoApp;

public class TermiteComponent implements SimWifiP2pManager.PeerListListener, SimWifiP2pManager.GroupInfoListener {

    public static final String TAG = "msgsender";
    public static final String SEND_USERNAME = "SEND_USERNAME";
    public static final String PHOTO = "PHOTO";

    public String virtualIP = "";
    private SimWifiP2pManager mManager = null;
    private SimWifiP2pManager.Channel mChannel = null;
    private Context context = null;
    private boolean isBound = false;
    private Messenger mService = null;
    private SimWifiP2pSocketServer mSrvSocket = null;
    private SimWifiP2pBroadcastReceiver mReceiver = null;
    private ServiceConnection mConnection = null;
    private AppCompatActivity activity = null;
    private HashMap<String, String> ip_Username_Map = new HashMap<>();
    public HashMap<String, ArrayList<String>> albumName_User_Map = new HashMap<>();

    public TermiteComponent(AppCompatActivity activity, Context context, Looper looper) {
        this.activity = activity;
        this.context = context;
        createConnection(context, looper);
        initTermite(context);
        beginService(context);
        isBound = true;
    }

    private void createConnection(Context context, Looper looper) {
        mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                mService = new Messenger(service);
                mManager = new SimWifiP2pManager(mService);
                mChannel = mManager.initialize(context, looper, null);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mService = null;
                mManager = null;
                mChannel = null;
            }
        };
    }

    private void initTermite(Context context) {
        SimWifiP2pSocketManager.Init(context);

        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiver(activity, this);
        activity.registerReceiver(mReceiver, filter);
    }

    private void beginService(Context context) {

        Intent intent = new Intent(context, SimWifiP2pService.class);
        activity.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        // spawn the chat server background task
        new IncommingCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void destroy() {
        if (!isBound) return;

        try {
            if (mSrvSocket != null) mSrvSocket.close();
            mSrvSocket = null;
            activity.unbindService(mConnection);
            activity.unregisterReceiver(mReceiver);
            isBound = false;
        }
        catch (IOException e) {
            Log.d("debug", "Failed to close socket");
        }
    }

    public void requestPeers() {
        mManager.requestGroupInfo(mChannel, this);
    }

    public void clearData() {
        ip_Username_Map.clear();
    }

    private String getUsername() {
        Peer2PhotoApp app = (Peer2PhotoApp)activity.getApplication();
        return (app != null) ? app.getUsername() : "null";
    }

    private List<String> getLocalPhotosPaths(String albumName) throws IOException {
        ArrayList<String> paths = new ArrayList<>();
        ArrayList<String> photoNames = new ArrayList<>();
        File localAlbumDir = new File(context.getFilesDir().getPath() + "/" + albumName);
        File localPhotosFile = new File(context.getFilesDir().getPath() + "/" + albumName + "/" + albumName + "_LOCAL.txt");
        if (localPhotosFile.isFile()) {
            paths.addAll(FileUtils.readLines(localPhotosFile));
            for (String path : paths) photoNames.add(path.substring(path.lastIndexOf('/')+1));
        }
        if (localAlbumDir.exists()) {
            File[] localFiles = localAlbumDir.listFiles();
            for (File file : localFiles) {
                String filename = file.getName();
                if (photoNames.contains(filename)) continue;
                if (filename.equals(albumName + ".txt") || filename.equals(albumName + "_LOCAL.txt"))
                    continue;
                paths.add(file.getPath());
                photoNames.add(filename);
            }
        }
        return paths;
    }

    private void sendAlbumPhotos() {
        try {
            for (Map.Entry<String, ArrayList<String>> albumEntry : albumName_User_Map.entrySet()) {
                String albumName = albumEntry.getKey();
                List<String> contents = getLocalPhotosPaths(albumName);
                for (Map.Entry<String, String> userEntry : ip_Username_Map.entrySet()) {
                    if (albumEntry.getValue().contains(userEntry.getValue())) {
                        sendPhotos(contents, albumName, userEntry.getKey());
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addNewAlbum(String albumId, String albumName) {
        if (!albumName_User_Map.containsKey(albumName)) {
            ArrayList<String> Users = new ArrayList<>();
            Users.add(((Peer2PhotoApp) activity.getApplication()).getUsername());
            albumName_User_Map.put(albumName, Users);
        }
    }

    private void sendUsername(String destinationIpAddress) {
        new SendUsername().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                getUsername(), virtualIP, destinationIpAddress);
    }

    private void sendPhotos(List<String> filePaths, String albumName, String destinationIpAddress) {
        try {
            new SendPhoto().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    filePaths, albumName, destinationIpAddress);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean hasLocalAlbum(String albumName) {
        return (new File(context.getFilesDir().getPath() + "/" + albumName).exists());
    }

    private boolean hasLocalPhoto(String albumName, String photoName) {
        File photo = new File(context.getFilesDir().getPath() + "/" + albumName + "/" + photoName);
        return photo.exists();
    }

    private String getLocalPhotoConfirmation(String albumName, String photoNames) {
        String confirmationString = "";

        String[] photoNameArray = photoNames.split(",");
        for (String photoName : photoNameArray) {
            confirmationString += ((hasLocalPhoto(albumName, photoName) ? "0" : "1") + ",");
        }

        return confirmationString;
    }

    private void processUsername(String username, String ipAddress) {
        ip_Username_Map.put(ipAddress, username);
        verifySendAlbumPhotos();
    }

    private void verifySendAlbumPhotos() {
        boolean requestCompleted = true;
        for (String user : ip_Username_Map.values()) {
            if (user.equals("")) {
                requestCompleted = false;
                break;
            }
        }
        if (requestCompleted) {
            sendAlbumPhotos();
        }
    }

    private void processPhoto(byte[] photoBytes, String albumName, String photoName) {
        try {
            Bitmap photo = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
            File photoDir = new File(context.getFilesDir().getPath() + "/" + albumName);
            if (!photoDir.exists()) {
                if (!photoDir.mkdir()) {
                    Log.d("debug", "Failed to create photo directory!");
                }
            }
            File newPhoto = new File(context.getFilesDir().getPath() + "/" + albumName + "/" + photoName);
            if (!newPhoto.createNewFile()) {
                Log.d("debug", "Failed to create new photo file!");
            }
            FileOutputStream out = new FileOutputStream(context.getFilesDir().getPath() + "/" + albumName + "/" + photoName);
            photo.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateApplicationLogs(@NonNull String operation, @NonNull String operationResult){
        String Operation = "OPERATION: " + operation + "\n";
        String timeStamp = "TIMESTAMP: " + new Date().toString() + "\n";
        String result = "RESULT: " + operationResult + "\n";

        ((Peer2PhotoApp)activity.getApplication()).updateLog(Operation + timeStamp + result);

    }

    /*
     * Asynctasks implementing message exchange
     */

    public class IncommingCommTask extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            android.util.Log.d("debug", "INCOMING!");
            Log.d(TAG, "IncommingCommTask started (" + this.hashCode() + ").");

            try {
                if (mSrvSocket != null) mSrvSocket.close();
                mSrvSocket = new SimWifiP2pSocketServer(10001);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SimWifiP2pSocket sock = mSrvSocket.accept();
                    android.util.Log.d("debug", "Accepted socket!");
                    BufferedReader sockIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    String messageType = sockIn.readLine();
                    android.util.Log.d("debug", "Received: " + messageType);

                    if (messageType.equals(SEND_USERNAME)) {
                        String username = sockIn.readLine();
                        String ipAddress = sockIn.readLine();
                        sock.getOutputStream().write((getUsername() + "\n").getBytes());
                        sock.getOutputStream().write((virtualIP + "\n").getBytes());
                        android.util.Log.d("debug", "Processed username " + username);
                    }
                    else if (messageType.equals(PHOTO)) {
                        String albumId = sockIn.readLine();
                        String albumName = sockIn.readLine();
                        String photoNames = sockIn.readLine();
                        sock.getOutputStream().write((getLocalPhotoConfirmation(albumName, photoNames) + "\n").getBytes());

                        int numPhotos = Integer.parseInt(sockIn.readLine());
                        android.util.Log.d("debug", "Confirming " + numPhotos + " photos");

                        HashMap<String, String> photoStrings = new HashMap<>();
                        for (int i = 0; i < numPhotos; i++) {
                            photoStrings.put(sockIn.readLine(), sockIn.readLine());
                        }
                        sock.getOutputStream().write(("\n").getBytes());
                        android.util.Log.d("debug", "Receiving " + photoStrings.size() + " photos");

                        Intent intent = new Intent(context, HomePage_Wifi.class);
                        intent.putExtra("Code", 0);
                        intent.putExtra("IsNew", hasLocalAlbum(albumName));
                        intent.putExtra("AlbumId", albumId);
                        intent.putExtra("AlbumName", albumName);
                        intent.putExtra("NumPhotos", photoStrings.size());
                        activity.startActivity(intent);

                        for (Map.Entry<String, String> photoString : photoStrings.entrySet()) {
                            byte[] bytes = new Gson().fromJson(photoString.getValue(), byte[].class);
                            processPhoto(bytes, albumName, photoString.getKey());
                            android.util.Log.d("debug", "Processed new photo");
                        }
                    }
                    sock.close();
                    //publishProgress();
                } catch (IOException e) {
                    Log.d("Error socket:", e.getMessage());
                    break;
                }
            }
            return null;
        }
    }

    public class SendUsername extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String username = params[0];
                String localIp = params[1];
                String ipAddress = params[2];
                SimWifiP2pSocket mCliSocket = new SimWifiP2pSocket(ipAddress, 10001);

                android.util.Log.d("debug", "Sending username");
                mCliSocket.getOutputStream().write((SEND_USERNAME + "\n").getBytes());
                mCliSocket.getOutputStream().write((username + "\n").getBytes());
                mCliSocket.getOutputStream().write((localIp + "\n").getBytes());

                BufferedReader sockIn = new BufferedReader(
                        new InputStreamReader(mCliSocket.getInputStream()));
                String otherUsername = sockIn.readLine();
                String otherIp = sockIn.readLine();
                processUsername(otherUsername, otherIp);

                android.util.Log.d("debug", "Username Sent");
                mCliSocket.close();
            } catch (UnknownHostException e) {
                return "Unknown Host:" + e.getMessage();
            } catch (IOException e) {
                return "IO error:" + e.getMessage();
            }
            return null;
        }
    }

    public class SendPhoto extends AsyncTask<Object, Void, String> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                List<String> photoPaths = (List<String>)params[0];
                String albumName = (String)params[1];
                String albumId = ((Peer2PhotoApp) context).getAlbumId(albumName);
                String ipAddress = (String)params[2];
                SimWifiP2pSocket mCliSocket = new SimWifiP2pSocket(ipAddress, 10001);

                String photoNames = "";

                for (String photoPath : photoPaths) {
                    String[] splitPath = photoPath.split("/");
                    photoNames += (splitPath[splitPath.length-1] + ",");
                }

                android.util.Log.d("debug", "Sending photos");
                mCliSocket.getOutputStream().write((PHOTO + "\n").getBytes());
                mCliSocket.getOutputStream().write((albumId + "\n").getBytes());
                mCliSocket.getOutputStream().write((albumName + "\n").getBytes());
                mCliSocket.getOutputStream().write((photoNames + "\n").getBytes());

                BufferedReader sockIn = new BufferedReader(
                        new InputStreamReader(mCliSocket.getInputStream()));
                String photoConfirmation = sockIn.readLine();

                String[] confirmations = photoConfirmation.split(",");
                HashMap<String, String> serializedPhotos = new HashMap<>();

                for (int i = 0; i < photoPaths.size(); i++) {
                    if (confirmations[i].equals("0")) continue;
                    Bitmap photo = BitmapFactory.decodeFile(photoPaths.get(i));
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    BitmapDataObject bitmapDataObject = new BitmapDataObject();
                    bitmapDataObject.imageByteArray = stream.toByteArray();
                    String[] splitPath = photoPaths.get(i).split("/");
                    serializedPhotos.put(splitPath[splitPath.length-1], new Gson().toJson(stream.toByteArray()));
                }

                mCliSocket.getOutputStream().write((serializedPhotos.size() + "\n").getBytes());
                for (Map.Entry<String, String> serialized : serializedPhotos.entrySet()) {
                    mCliSocket.getOutputStream().write((serialized.getKey() + "\n").getBytes());
                    mCliSocket.getOutputStream().write((serialized.getValue() + "\n").getBytes());
                }
                sockIn.readLine();
                android.util.Log.d("debug", "Photos Sent");

                Intent intent = new Intent(context, HomePage_Wifi.class);
                intent.putExtra("Code", 1);
                intent.putExtra("AlbumId", albumId);
                intent.putExtra("AlbumName", albumName);
                intent.putExtra("NumPhotos", confirmations.length);

                activity.startActivity(intent);
                mCliSocket.close();
            } catch (UnknownHostException e) {
                return "Unknown Host:" + e.getMessage();
            } catch (IOException e) {
                return "IO error:" + e.getMessage();
            }
            return null;
        }
    }

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {

    }

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices,
                                     SimWifiP2pInfo groupInfo) {

        if (groupInfo.getDevicesInNetwork().size() > 0) {
            if (virtualIP == "") {
                for (SimWifiP2pDevice device : devices.getDeviceList()) {
                    if (device.deviceName.equals(groupInfo.getDeviceName())) {
                        virtualIP = device.getVirtIp();
                        break;
                    }
                }
            }
            else {
                for (SimWifiP2pDevice device : devices.getDeviceList()) {
                    if (!device.deviceName.equals(groupInfo.getDeviceName())) {
                        if (!ip_Username_Map.containsKey(device.getVirtIp()))
                            ip_Username_Map.put(device.getVirtIp(), "");
                        sendUsername(device.getVirtIp());
                    }
                }
            }
        }
    }
}
