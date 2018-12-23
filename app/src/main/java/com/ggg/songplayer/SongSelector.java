package com.ggg.songplayer;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;
import android.widget.Toast;


import com.ggg.songplayer.Strings.RxMessageStrings;
import com.ggg.songplayer.Strings.ServiceStrings;
import com.ggg.songplayer.Strings.SharedPrefsStrings;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.ggg.songplayer.MyApplication.saveSharedPref;

public class SongSelector extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    private ListView songView;
    private boolean permiso = false;
    private boolean busca=false;
    //private RecyclerView recycler;
    private FastScrollRecyclerView recycler;
    private LinearLayoutManager lManager;
    private SongAdapter adapter;
    private ArrayList<Song> songs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_selector);
        prepareRxHandler();
        // Obtener el Recycler
        recycler = findViewById(R.id.reciclador);
        //fastScroller = findViewById(R./id.fastscroll);
        recycler.setHasFixedSize(true);

        // Usar un administrador para LinearLayout
        lManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(lManager);
        //This would be a secret between you and me ;)
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        saveSharedPref(SharedPrefsStrings.SCREEN_WIDTH, width);
        MyApplication.saveSharedPref(SharedPrefsStrings.SONG_CURR_TIME, 0);
        pidePermiso();
        if(permiso) {
            hayCanciones();
            muestraCanciones();
        }
    }

    public void hayCanciones(){
        SQLiteDatabase sql = openOrCreateDatabase("songs.db", MODE_PRIVATE, null);
        sql.execSQL("CREATE TABLE if not exists songs ("
                + "id varchar(250),"
                + "name varchar(100) NOT NULL,"
                + "artist varchar(100) NOT NULL,"
                + "album varchar(100) NOT NULL)");
        Cursor c = sql.rawQuery("select count(*) from songs", null);
        if(c.moveToFirst())
            c.moveToFirst();
        if(c.getInt(0)==0)
            busca=true;
        sql.close();
    }

    public void muestraCanciones(){
        if(busca) {
            getSongList();
        }
        // Crear un nuevo adaptador
        songs = new ArrayList<>();
        SQLiteDatabase sql = openOrCreateDatabase("songs.db", MODE_PRIVATE, null);
        Cursor c = sql.rawQuery("select * from songs order by upper(name)", null);
        c.moveToFirst();
        do {
            songs.add(new Song(
                    c.getString(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3)
            ));
        }
        while (c.moveToNext());
        sql.close();
        adapter = new SongAdapter(songs);
        recycler.setAdapter(adapter);
        //fastScroller.setRecyclerView(recycler);
    }

    public void pidePermiso(){
        //se comprueba que haya permisos para leer la sd
        if(ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        else{
            permiso=true;
        }
    }
    @Override//una vez respondido a la petición de permisos
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        //switch en caso de que haya más permisos pedidos
        switch(requestCode){
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getBaseContext(), "Permiso concedido", Toast.LENGTH_SHORT).show();
                    permiso=true;
                    hayCanciones();
                    muestraCanciones();
                }
                else{
                    Toast.makeText(getBaseContext(), "Permiso denegado, necesita dar permiso para leer la sd para ver las canciones", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void getSongList() {
        songs = new ArrayList<>();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        getAllSongs(musicUri);
        musicUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        getAllSongs(musicUri);
        busca=false;
    }
    public void getAllSongs(Uri musicUri){
        SQLiteDatabase sql = openOrCreateDatabase("songs.db", MODE_PRIVATE, null);
        ContentResolver musicResolver = getContentResolver();
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int is_music = musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC);
            int song_duration = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int mime = musicCursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE);
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            do {
                //casos para excluir tonos
                if(musicCursor.getInt(is_music)==1 && !musicCursor.getString(mime).equals("application/ogg") && musicCursor.getInt(song_duration)>10000){
                    int thisId = musicCursor.getInt(idColumn);
                    String thisTitle = musicCursor.getString(titleColumn);
                    String thisArtist = musicCursor.getString(artistColumn);
                    String thisAlbum = musicCursor.getString(albumColumn);
                    Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, thisId);
                    String id = "";
                    if (uri != null)
                        id += uri.toString();
                    Song s = new Song(id, thisTitle, thisArtist, thisAlbum);
                    ContentValues c = new ContentValues();
                    c.put("id", id);
                    c.put("name", thisTitle);
                    c.put("artist", thisArtist);
                    c.put("album", thisAlbum);
                    sql.insert("songs", null, c);
                    //Log.d("songName", s.getName());
                    songs.add(s);
                }
            }
            while (musicCursor.moveToNext());
        }
        sql.close();
        musicCursor.close();
    }
    public void playSong(View v){
        String id = v.getTag().toString();

        Intent serviceIntent = new Intent(this, MusicService.class);
        serviceIntent.putExtra(ServiceStrings.SONG_URI, id);
        startService(serviceIntent);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        //(1:la actividad que entra, 2:la actividad que sale)
        overridePendingTransition(R.anim.enter_from_bottom, R.anim.exit_from_top);

    }

    /**
     * Rx Components
     */
    public static io.reactivex.Observer<RxMessage> songSelectorRxMessagesHandler;
    private io.reactivex.Observer<RxMessage> _songSelectorRxMessagesHandler = new io.reactivex.Observer<RxMessage>() {
        @Override
        public void onSubscribe(Disposable d) { }
        @Override
        public void onNext(RxMessage rxMessage) {
            Log.d("Msg on PlayActivity", rxMessage.getKey());
            //Logic for every message this would handle
            if(rxMessage.getKey().equals(RxMessageStrings.NEW_BG_READY)){
                /*recycler.setBackgroundColor(MyApplication.getRGB());
                findViewById(R.id.songLayout).setBackgroundColor(MyApplication.getRGB());
                recycler.setThumbColor(MyApplication.getBodyColor());
                recycler.setPopupBgColor(MyApplication.getRGB());
                recycler.setPopupTextColor(MyApplication.getBodyColor());*/
            }
        }
        @Override
        public void onError(Throwable e) { }
        @Override
        public void onComplete() { }
    };

    void transmitMessate(RxMessage rxMessage){
        RxMessage.transmitMessage(rxMessage)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(MusicService.serviceRxMessagesHandler);
    }
            void prepareRxHandler(){ songSelectorRxMessagesHandler = _songSelectorRxMessagesHandler;}
}
