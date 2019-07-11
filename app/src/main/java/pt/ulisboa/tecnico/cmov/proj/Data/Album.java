package pt.ulisboa.tecnico.cmov.proj.Data;

import android.media.Image;

import java.util.ArrayList;
import java.util.Collection;

public class Album {

    private String albumId;
    private String albumName;
    private int thumbnail;

    public Album(String albumId, String albumName, int thumbnail) {
        this.albumId = albumId;
        this.albumName = albumName;
        this.thumbnail = thumbnail;
    }

    public String getAlbumId() { return this.albumId; }

    public String getAlbumName() { return this.albumName; }

    public int getAlbumThumbnail() { return this.thumbnail; }

}
