package com.ggg.songplayer;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.palette.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.davidmiguel.dragtoclose.DragListener;
import com.davidmiguel.dragtoclose.DragToClose;
import com.ggg.songplayer.Strings.RxMessageStrings;
import com.ggg.songplayer.Strings.ServiceStrings;
import com.ggg.songplayer.Strings.SharedPrefsStrings;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.ggg.songplayer.MyApplication.baseSwatch;


public class MainActivity extends AppCompatActivity {
    /*Cosas por añadir después para no estancarme por perfeccionista
        -Efecto al presionar el botón por largo tiempo
        -Saltar progresivamente al dejar presionado prev o next con un factor de {salto=1s; salta; salto=salto*1.5;}
    */
    //Variables globales
    boolean set=false;//si hay una canción actualmente seleccionada
    boolean permiso=false;//si hay permiso de lectura de la sd
    boolean isSeeking=false;//evita que el seekbar salte cuando se recorra manualmente
    boolean ready=false;//si ya hay colores para usar
    boolean isshuffle=false;
    boolean toRepeat=false;


    //Definición de recursos
    Drawable drawPause;
    Drawable drawPlay;
    ImageView artwork;
    TextView songTitle;
    TextView songInfo;
    TextView currSongPos;
    SeekBar songProgress;
    TextView songDuration;
    ImageButton playPause;
    ImageButton prevSong;
    ImageButton nextSong;
    ImageButton random;
    ImageButton repeat;
    LinearLayout mainBackgroud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prepareRxHandler();
        //Asignación de recursos
        artwork = findViewById(R.id.artwork);
        songTitle = findViewById(R.id.txtTitle);
        songInfo = findViewById(R.id.txtExtraInfo);
        currSongPos = findViewById(R.id.txtCurrPos);
        songProgress = findViewById(R.id.songProgress);
        songDuration = findViewById(R.id.txtSongDuration);
        playPause = findViewById(R.id.btnPlayPause);
        prevSong = findViewById(R.id.btnPrevious);
        nextSong = findViewById(R.id.btnNext);
        random = findViewById(R.id.btnRand);
        repeat = findViewById(R.id.btnRepeat);
        mainBackgroud = findViewById(R.id.mainBackgroud);
        Intent serviceIntent = new Intent(this, MusicService.class);
        serviceIntent.putExtra(ServiceStrings.GET_SONG_DATA, "dummyString");
        startService(serviceIntent);
        int seconds = (int) MyApplication.getSharedPref(SharedPrefsStrings.SONG_CURR_TIME, 0);
        songProgress.setProgress(seconds);
        currSongPos.setText(getTimeAsText(seconds));

        //Recursos como botones
        drawPause = ContextCompat.getDrawable(getBaseContext(), R.drawable.pause);
        drawPlay = ContextCompat.getDrawable(getBaseContext(), R.drawable.play);

        //Event Listeners
        artwork.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //mostrar menú contextual
                //TODO: Menu contextual en artwork y al presionar cada canción en el recycler
                return false;
            }
        });
        songProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(isSeeking) {
                    int seconds = songProgress.getProgress();
                    currSongPos.setText(getTimeAsText(seconds));
                    updatePlayerCurrTime();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking=true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //evita que haya error al mover el progress sin que haya canción
                if(MusicService.isPlayerPlaying()) {
                    int seconds = songProgress.getProgress();
                    currSongPos.setText(getTimeAsText(seconds));
                    updatePlayerCurrTime();
                    isSeeking = false;
                }
            }
        });
        DragToClose dragToClose = findViewById(R.id.DragToCloseView);
        dragToClose.setDragListener(new DragListener() {
            @Override
            public void onStartDraggingView() {}

            @Override
            public void onViewCosed() {
                overridePendingTransition(R.anim.enter_from_top, R.anim.exit_from_bottom);
            }
        });
    }
    String getTimeAsText(int seconds){
        int minutesTXT = seconds / 60;
        int secondsTXT = seconds - (minutesTXT*60);
        return (minutesTXT<10 && minutesTXT>0 ? '0' + Integer.toString(minutesTXT) : Integer.toString(minutesTXT)) +
                        ':' +
                        (secondsTXT<10 ? '0' + Integer.toString(secondsTXT) : Integer.toString(secondsTXT));
    }

    void updatePlayerCurrTime(){
        Intent serviceIntent = new Intent(getBaseContext(), MusicService.class);
        serviceIntent.putExtra(ServiceStrings.SEEK_TO, songProgress.getProgress());
        startService(serviceIntent);
        MyApplication.saveSharedPref(SharedPrefsStrings.SONG_CURR_TIME, songProgress.getProgress());
    }

    public void drawer(Palette.Swatch paleta){
        mainBackgroud.setBackgroundColor(paleta.getRgb());
        songInfo.setTextColor(paleta.getTitleTextColor());
        songTitle.setTextColor(paleta.getBodyTextColor());
        currSongPos.setTextColor(paleta.getBodyTextColor());
        songDuration.setTextColor(paleta.getBodyTextColor());
        songProgress.setProgressTintList(ColorStateList.valueOf(paleta.getTitleTextColor()));
        nextSong.getDrawable().setColorFilter(paleta.getTitleTextColor(), PorterDuff.Mode.MULTIPLY);
        prevSong.getDrawable().setColorFilter(paleta.getTitleTextColor(), PorterDuff.Mode.MULTIPLY);
        random.getDrawable().setColorFilter(paleta.getTitleTextColor(), PorterDuff.Mode.MULTIPLY);
        repeat.getDrawable().setColorFilter(paleta.getTitleTextColor(), PorterDuff.Mode.MULTIPLY);
        drawPause.setColorFilter(paleta.getTitleTextColor(), PorterDuff.Mode.MULTIPLY);
        drawPlay.setColorFilter(paleta.getTitleTextColor(), PorterDuff.Mode.MULTIPLY);
        playPause.setImageDrawable(drawPause);

    }

    //TODO: Implement this
    public void changeState(View v){
        //TODO: Service message to play/pause a song with boolean isPaused
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra(ServiceStrings.PLAY_PAUSE_PLAYER, "playPause");
        startService(intent);
        if (!MusicService.isPlayerPlaying()) {
                playPause.setImageDrawable(drawPause);

            } else {
            playPause.setImageDrawable(drawPlay);
        }
    }

    //TODO: Implement this
    public void repeat(View v){
        if(set) {
            if (toRepeat) {
                repeat.getDrawable().setColorFilter(baseSwatch.getTitleTextColor(), PorterDuff.Mode.MULTIPLY);
                repeat.setAlpha(0.5f);
                toRepeat = false;
            } else {
                repeat.getDrawable().setColorFilter(baseSwatch.getBodyTextColor(), PorterDuff.Mode.MULTIPLY);
                repeat.setAlpha(1.0f);
                toRepeat = true;
            }
        }

    }

    //TODO: Implement this
    public void shuffle(View v){
        if(set) {
            if (isshuffle) {
                random.getDrawable().setColorFilter(baseSwatch.getTitleTextColor(), PorterDuff.Mode.MULTIPLY);
                random.setAlpha(0.5f);
                isshuffle = false;
            } else {
                random.getDrawable().setColorFilter(baseSwatch.getBodyTextColor(), PorterDuff.Mode.MULTIPLY);
                random.setAlpha(1.0f);
                isshuffle = true;
            }
        }
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_top, R.anim.exit_from_bottom);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    /**
     * Timer stuff
     */
    Handler handler = new Handler();
    void executeEverySecond() {
        handler.postDelayed(new Runnable() {
            public void run() {
                if(!isSeeking) {
                    int seconds = (int) MyApplication.getSharedPref(SharedPrefsStrings.SONG_CURR_TIME, 0);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        songProgress.setProgress(seconds, true);
                    } else {
                        songProgress.setProgress(seconds);
                    }
                    int minutesTXT = seconds / 60;
                    int secondsTXT = seconds - (minutesTXT * 60);
                    currSongPos.setText(
                            (minutesTXT < 10 && minutesTXT > 0 ? '0' + Integer.toString(minutesTXT) : Integer.toString(minutesTXT)) +
                                    ':' +
                                    (secondsTXT < 10 ? '0' + Integer.toString(secondsTXT) : Integer.toString(secondsTXT)));
                }
                handler.postDelayed(this, 500);
            }
        }, 1000);
    }

    /**
     * Rx Stuff
     */
    public static io.reactivex.Observer<RxMessage> playerUIMessagesHandler;

    private io.reactivex.Observer<RxMessage> _playerUIMessagesHandler = new io.reactivex.Observer<RxMessage>() {
        @Override
        public void onSubscribe(Disposable d) { }
        @Override
        public void onNext(RxMessage rxMessage) {
            Log.d("Msg on PlayActivity", rxMessage.getKey());
            //Logic for every message this would handle
            if(rxMessage.getKey().equals(RxMessageStrings.SONG_DATA)){
                artwork.setImageBitmap(MyApplication.getBitmap());
                drawer(MyApplication.getBaseSwatch());
                Object[] songData = rxMessage.getVal();
                songTitle.setText((String)songData[0]);
                String songInfoString = (songData[1]!=null? (String)songData[1] : "Unknown") + " - " + (songData[2]!=null? (String)songData[2] : "Unknown");
                songInfo.setText(songInfoString);
                int length = (int)songData[3]/1000;
                int minutes = length/60;
                int seconds = length - (minutes*60);
                songDuration.setText(
                        (minutes<10? '0' + Integer.toString(minutes) : Integer.toString(minutes)) +
                                ':' +
                                (seconds<10? '0' + Integer.toString(seconds) : Integer.toString(seconds)));
                songProgress.setMax(length);
                executeEverySecond();
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

    void prepareRxHandler(){ playerUIMessagesHandler = _playerUIMessagesHandler;}
}
