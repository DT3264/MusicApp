package com.ggg.songplayer;

/**
 * Created by Dani on 29/11/2017.
 */

public class Song {
    private String ID;
    private String Name;
    private String Artist;
    private String Album;
    private String hasCover;

    public Song(String ID, String Name, String Artist, String Album){
        setID(ID);
        setName(Name);
        setArtist(Artist);
        setAlbum(Album);
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getArtist() {
        return Artist;
    }

    public void setArtist(String artist) {
        Artist = artist;
    }

    public String getAlbum() {
        return Album;
    }

    public void setAlbum(String album) {
        Album = album;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getHasCover() {
        return hasCover;
    }

    public void setHasCover(String hasCover) {
        this.hasCover = hasCover;
    }
}
