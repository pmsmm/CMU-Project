package pt.ulisboa.tecnico.cmov.proj.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.proj.Data.Photo;
import pt.ulisboa.tecnico.cmov.proj.R;

public class PhotoAdapter extends ArrayAdapter<Photo> {

    private Context context;
    private List<Photo> photos;

    public PhotoAdapter(Context context, int resource, ArrayList<Photo> objects) {
        super(context, resource, objects);

        this.context = context;
        this.photos = objects;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        //get the property we are displaying
        Photo photo = photos.get(position);
        Bitmap photoBitmap = photo.getBitmap();

        //get the inflater and inflate the XML layout for each item
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (view == null) view = inflater.inflate(R.layout.activity_photo_thumb, null);

        ImageView image = view.findViewById(R.id.photo_image);
        if (photoBitmap != null) {
            image.setImageBitmap(photoBitmap);
        }
        else {
            image.setImageResource(R.drawable.empty_thumbnail);
        }

        return view;
    }
}