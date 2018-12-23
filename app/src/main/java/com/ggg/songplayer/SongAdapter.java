package com.ggg.songplayer;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Dani on 26/11/2017.
 */

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongsViewHolder> implements FastScrollRecyclerView.SectionedAdapter{

    private List<Song> items;
    public static class SongsViewHolder extends RecyclerView.ViewHolder {
        // Campos respectivos de un item
        public CardView cardView;
        //public ImageView imagen;
        ImageView imagen;
        TextView nombre;
        TextView artistAlbumInfo;

        public SongsViewHolder(View v) {
            super(v);
            imagen = v.findViewById(R.id.thumbnail);
            nombre = v.findViewById(R.id.songName);
            artistAlbumInfo = v.findViewById(R.id.songExtra);
            cardView = v.findViewById(R.id.cardView);
        }
    }

    public SongAdapter(ArrayList<Song> items) {
        this.items = items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public SongsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.song_lay, viewGroup, false);
        return new SongsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final SongsViewHolder viewHolder, int i) {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.no_cover6)
                .fallback(R.drawable.no_cover6)
                .error(R.drawable.no_cover6)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .override(250, 250)
                .priority(Priority.LOW);

        Glide.with(viewHolder.imagen.getContext())
                .load(items.get(i).getID() + "/albumart")
                .apply(options)
                .into(viewHolder.imagen);
        //new ImageLoader().execute(new ImageLoadItems(items.get(i).getID(), viewHolder.imagen));
        viewHolder.nombre.setText(items.get(i).getName());
        viewHolder.artistAlbumInfo.setText((items.get(i).getArtist()));
        if(MyApplication.existColor()) {
            viewHolder.nombre.setTextColor(MyApplication.getBodyColor());
            viewHolder.artistAlbumInfo.setTextColor(MyApplication.getBodyColor());
        }
        viewHolder.cardView.setTag(items.get(i).getID());
    }

    @Override
    public String getSectionName(int position) {
        return items.get(position).getName().substring(0,1);
    }
}
