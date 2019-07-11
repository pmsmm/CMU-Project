package pt.ulisboa.tecnico.cmov.proj.Dropbox;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.dropbox.core.v2.DbxClientV2;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Task to download a file from Dropbox and put it in the Downloads folder
 */

public class DownloadFileTask extends AsyncTask<String, String, File> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public DownloadFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDownloadComplete(result);
        }
    }

    @Override
    protected File doInBackground(String... params) {
        //###############################ATENCAO###############################################
        //A SINTAXE DOS PARAMETROS E A SEGUINTE
        //PARAMS 0 ---> String com os users do album
        //PARAMS 1 ---> Username
        //PARAMS 2 ---> JSONObject obtido com o pedido HTTP para obter os URLs das slices do album
        //PARAMS 3 ---> Application Directory
        //PARAMS 4 ---> Application Photo Directory
        //###############################ATENCAO###############################################

        try {
            android.util.Log.d("debug", "Starting Async Task: Download File Task");
            String[] users = params[0].split(",");
            String mUsername = params[1];
            JSONObject mapResponse = new JSONObject(params[2]);
            String albumName = mapResponse.getString("name");
            String applicationDirectory = params[3];
            String applicationPhotoDirectory = params[4];

            //The Array List that will have all the URLs for the slices
            HashMap<String, String> URLs = new HashMap<>();

            for (int i = 0; i < users.length; i++) {
                if (!users[i].equals(mUsername)) { //User will ignore its own slice
                    if (mapResponse.getString(users[i]) != null && mapResponse.getString(users[i]).trim().length() > 0) {
                        URLs.put(users[i], mapResponse.getString(users[i]));
                    } else {
                        android.util.Log.d("debug","Null Slice");
                    }
                }
            }
            //Toast.makeText(mContext, "Downloading photos from " + URLs.size() + " users", Toast.LENGTH_SHORT).show();
            String url;
            String username;
            Iterator iterator = URLs.entrySet().iterator();
            Map.Entry pair;
            while (iterator.hasNext()){
                pair = (Map.Entry) iterator.next();
                username = (String) pair.getKey();
                url = (String) pair.getValue();

                //Saving the obtained slice from the cloud in a file to later remove the file
                FileUtils.copyURLToFile(new URL(url.replaceAll("\u003d", "=")), new File(applicationDirectory + "/" + albumName + "/" + username + "_SLICE.txt"));
                //The Recently Saved Slice
                File Slice = new File(params[3] + "/" + albumName + "/" + username + "_SLICE.txt");

                if(Slice.exists()){
                    //The Local File That Contains the Paths of The Photos Downloaded in Local Storage
                    File RemotePhotosPath = new File(params[3] + "/" + albumName + "/" + username + "_REMOTE.txt");

                    if(!RemotePhotosPath.exists()){
                        RemotePhotosPath.createNewFile();
                    }
                    //The URLs for The Photos of the current slice being processed
                    List<String> contents = FileUtils.readLines(Slice);

                    //The Path For The Galery Directory of The App
                    File imageRoot = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), applicationPhotoDirectory + "/" + albumName);

                    for(int x = 0; x < contents.size(); x++){
                        //Downloading a photo and saving it to the galery inside the album created for the app with the Nomeclature USERNAME + _PHOTO + NUMBER_OF_PHOTO + EXTENSION
                        FileUtils.copyURLToFile(new URL(contents.get(x)), new File(imageRoot, username + "_PHOTO" + x + ".jpg"));
                        //Saving the path of the downloaded photo to a file to load the images and later remove them
                        FileUtils.writeStringToFile(RemotePhotosPath, new File(imageRoot, username + "_PHOTO" + x + ".jpg").getPath());

                        //((AlbumView) mContext).imageScalingAndPosting(new File(imageRoot, users[i] + "_PHOTO" + x + ".jpg").getPath());

                        //Toast.makeText(mContext, x + "/" + contents.size() + " photos obtained", Toast.LENGTH_SHORT).show();
                    }

                    Slice.delete();

                }
                //Toast.makeText(mContext, "Photos obtained from " + i + "/" + URLs.size() + " users", Toast.LENGTH_SHORT).show();
            }
            //Toast.makeText(mContext, "All photos obtained for album " + albumName, Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    /*@Override
    protected void onProgressUpdate(String... sentence) {
        Toast.makeText(mContext, sentence[0], Toast.LENGTH_SHORT).show();
    }*/

    public interface Callback {
        void onDownloadComplete(File result);
        void onError(Exception e);
    }

}