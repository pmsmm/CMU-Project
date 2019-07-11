package pt.ulisboa.tecnico.cmov.proj.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

    private final Context mContext;
    private final Bitmap[] images;

    public ImageAdapter(Context context, Bitmap[] images){
        this.mContext = context;
        this.images = images;
    }

    @Override
    public int getCount(){
        return images.length;
    }

    @Override
    public long getItemId(int position){
        return 0;
    }

    @Override
    public Bitmap getItem(int position){
        return images[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ImageView view = new ImageView(mContext);
        view.setImageBitmap(images[position]);
        return view;
    }

}
