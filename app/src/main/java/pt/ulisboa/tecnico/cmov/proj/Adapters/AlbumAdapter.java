package pt.ulisboa.tecnico.cmov.proj.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.proj.Data.Album;
import pt.ulisboa.tecnico.cmov.proj.R;

public class AlbumAdapter extends ArrayAdapter<Album> {

    private Context context;
    private List<Album> albums;

    public AlbumAdapter(Context context, int resource, ArrayList<Album> objects) {
        super(context, resource, objects);

        this.context = context;
        this.albums = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        //get the property we are displaying
        Album album = albums.get(position);
        String albumName = album.getAlbumName();
        int albumThumbnail = album.getAlbumThumbnail();

        //get the inflater and inflate the XML layout for each item
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (view == null) view = inflater.inflate(R.layout.activity_album_thumb, null);

        TextView albumNameText = view.findViewById(R.id.album_name);
        albumNameText.setText(albumName);

        ImageView thumbnail = view.findViewById(R.id.album_image);
        thumbnail.setImageResource(albumThumbnail);

        return view;
    }
}