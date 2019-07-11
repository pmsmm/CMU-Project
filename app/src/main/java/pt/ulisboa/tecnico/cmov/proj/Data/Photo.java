package pt.ulisboa.tecnico.cmov.proj.Data;

import android.graphics.Bitmap;

public class Photo {

    private Bitmap photoBitmap = null;

    public Photo(Bitmap bitmap) {
        this.photoBitmap = bitmap;
    }

    public Bitmap getBitmap() { return photoBitmap; }
}
