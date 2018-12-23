package com.ggg.songplayer;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;
import android.util.Log;

import com.ggg.songplayer.Strings.RxMessageStrings;
import com.ggg.songplayer.Strings.ServiceStrings;
import com.ggg.songplayer.Strings.SharedPrefsStrings;

import java.io.ByteArrayInputStream;
import java.io.FileDescriptor;
import java.io.IOException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MusicService extends Service {

    /**
     * Main Variables
     */
    static boolean isPlaying = false;
    MediaPlayer mediaPlayer = new MediaPlayer();
    Uri actualSongUri = null;


    Bitmap artworkBitmap = null;
    String songTitle, songArtist, songAlbum;
    int songLenght;

    /**
     * Service methods
     */
    public MusicService() {
        super();
    }

    @Override
    public void onCreate() {
        prepareRxHandler();
        super.onCreate();
    }

    int lastProgressBeforePause;
    Uri lastUriBeforePause;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getStringExtra(ServiceStrings.SONG_URI) != null) {
            String tempString = intent.getStringExtra("songUri");
            Uri tempNewUri = Uri.parse(tempString);
            if(actualSongUri!=null) {
                if (!actualSongUri.toString().equals(tempNewUri.toString())) {
                    actualSongUri = tempNewUri;
                    SaveLastProgress(0);
                    playSong(actualSongUri);
                }
            }
            else {
                actualSongUri = tempNewUri;
                playSong(actualSongUri);
            }
        }
        else if(intent.getIntExtra(ServiceStrings.SEEK_TO, -1) != -1){
            int seconds = intent.getIntExtra(ServiceStrings.SEEK_TO, -1);
            mediaPlayer.seekTo(seconds * 1000);
        }
        else if(intent.getStringExtra(ServiceStrings.PLAY_PAUSE_PLAYER)!=null){
            if(mediaPlayer.isPlaying()) {
                lastUriBeforePause = actualSongUri;
                lastProgressBeforePause = mediaPlayer.getCurrentPosition();
                mediaPlayer.pause();
            }
            else {
                mediaPlayer.start();
                if(actualSongUri==lastUriBeforePause)
                    mediaPlayer.seekTo(lastProgressBeforePause);
            }
            isPlaying = mediaPlayer.isPlaying();
        }
        else if(intent.getStringExtra(ServiceStrings.GET_SONG_DATA)!=null){
            transmitMessageToNowPlaying(new RxMessage(RxMessageStrings.SONG_DATA,
                    new Object[]{
                            songTitle,
                            songArtist,
                            songAlbum,
                            songLenght
                    }
            ));
            MyApplication.setBitmap(artworkBitmap);
            MyApplication.setSwatch(baseSwatch);
            transmitMessageToSongSelector(new RxMessage(RxMessageStrings.NEW_BG_READY, new Object[]{}));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Player methods
     */

    public void stopSong(){
        if(mediaPlayer!=null) {
            mediaPlayer.pause();
            isPlaying = false;
        }
        isPlaying=false;
    }

    public void playSong(Uri selSongUri){
        stopSong();
        isPlaying=true;
        mediaPlayer = MediaPlayer.create(getBaseContext(), selSongUri);
        mediaPlayer.start();
        
        FileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = getContentResolver().openFileDescriptor(selSongUri, "r").getFileDescriptor();//mode:r(ead) || w(rite)
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            mediaMetadataRetriever.setDataSource(fileDescriptor);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        byte [] artworkByte = mediaMetadataRetriever.getEmbeddedPicture();
        ByteArrayInputStream arrayInputStream;
        Bitmap tempArtworkBitmap = null;
        if(artworkByte!=null) {
            arrayInputStream = new ByteArrayInputStream(artworkByte);
            tempArtworkBitmap = BitmapFactory.decodeStream(arrayInputStream);
        }
        int width = (Integer)MyApplication.getSharedPref(SharedPrefsStrings.SCREEN_WIDTH, 0);
        if(tempArtworkBitmap!=null) {
            tempArtworkBitmap = Bitmap.createScaledBitmap(tempArtworkBitmap, width, width, true);
        }
        else {
            Bitmap mainBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_cover);
            tempArtworkBitmap = Bitmap.createScaledBitmap(mainBitmap, width, width, true);
        }
        artworkBitmap = tempArtworkBitmap;

        new Thread(new Runnable() {
            @Override
            public void run() {
                generaPaleta();
            }
        }).start();
        songTitle = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        songArtist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        songAlbum = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        songLenght = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        executeEverySecond();
        mediaMetadataRetriever.release();
    }

    Handler handler = new Handler();
    void executeEverySecond() {
        if(mediaPlayer.isPlaying()) {
            handler.postDelayed(new Runnable() {
                public void run() {
                    int seconds = mediaPlayer.getCurrentPosition()/1000;
                    SaveLastProgress(seconds);
                    handler.postDelayed(this, 1000);
                }
            }, 1000);
            isPlaying = mediaPlayer.isPlaying();

        }
    }
    void SaveLastProgress(int seconds){
        MyApplication.saveSharedPref(SharedPrefsStrings.SONG_CURR_TIME, seconds);
    }

    public static boolean isPlayerPlaying(){
        return isPlaying;
    }

    Palette basePallete;
    Palette.Swatch baseSwatch;
    public void generaPaleta(){
        if(artworkBitmap!=null) {
            Palette.from(artworkBitmap).maximumColorCount(16).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    basePallete = palette;
                    baseSwatch = basePallete.getDominantSwatch();
                    if(baseSwatch==null) {
                        baseSwatch = basePallete.getLightMutedSwatch();
                    }
                }
            });
        }
    }

    /***
     * Rx Messages components
     */
    public static io.reactivex.Observer<RxMessage> serviceRxMessagesHandler;

    void prepareRxHandler() {
        serviceRxMessagesHandler = new io.reactivex.Observer<RxMessage>() {
            @Override
            public void onNext(RxMessage rxMessage) {
                if(rxMessage.getKey().equals("ggg")){
                    Log.d("Message on service", rxMessage.getKey());
                }
            }
            @Override
            public void onSubscribe(Disposable d) { }
            @Override
            public void onError(Throwable e) { }
            @Override
            public void onComplete() { }
        };
    }

    void transmitMessageToNowPlaying(RxMessage rxMessage){
        if(MainActivity.playerUIMessagesHandler != null) {
            RxMessage.transmitMessage(rxMessage)
                    // Run on a background thread
                    .subscribeOn(Schedulers.io())
                    // Be notified on the main thread
                    .observeOn(AndroidSchedulers.mainThread())
                    // The handler on another class
                    .subscribe(MainActivity.playerUIMessagesHandler);
            //Log.d("Service", "Message transmitted succesfully");
        }
    }

    void transmitMessageToSongSelector(RxMessage rxMessage){
        if(SongSelector.songSelectorRxMessagesHandler != null) {
            RxMessage.transmitMessage(rxMessage)
                    // Run on a background thread
                    .subscribeOn(Schedulers.io())
                    // Be notified on the main thread
                    .observeOn(AndroidSchedulers.mainThread())
                    // The handler on another class
                    .subscribe(SongSelector.songSelectorRxMessagesHandler);
            //Log.d("Service", "Message transmitted succesfully");
        }
    }
}
