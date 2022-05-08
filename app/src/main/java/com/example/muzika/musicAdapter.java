package com.example.muzika;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;

public class musicAdapter extends RecyclerView.Adapter<musicAdapter.ViewHolder> implements Filterable {

    private ArrayList<music> musicDataAll;
    private ArrayList<music> musicDataFiltered;
    private Context ncontext;
    private int lastpos = -1;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    musicAdapter(Context context, ArrayList<music> musicData) {
        this.musicDataAll = musicData;
        this.musicDataFiltered = musicData;
        this.ncontext = context;

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(ncontext).inflate(R.layout.song_list_element, parent, false));
    }

    @Override
    public int getItemCount() {
        return musicDataFiltered.size();
    }

    @Override
    public void onBindViewHolder(musicAdapter.ViewHolder holder, int position) {
        music newMusic = musicDataFiltered.get(position);
        Log.i("adapter", "position of item is "+ position);
        holder.bindTo(newMusic);
        if(holder.getAdapterPosition() > lastpos) {
            Animation animation = AnimationUtils.loadAnimation(ncontext, R.anim.slidefade);
            holder.itemView.startAnimation(animation);
            lastpos = holder.getAdapterPosition();
        }
    }

    @Override
    public Filter getFilter() {
        Log.i("adapter", "returning filter rn");
        return musicfilter;
    }

    private Filter musicfilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<music> filtered = new ArrayList<>();
            FilterResults res = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                res.count = musicDataAll.size();
                res.values = musicDataAll;
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (music filterable : musicDataAll) {
                    if (filterable.getMusicName().toLowerCase().contains(filterPattern)) {
                        filtered.add(filterable);
                    }
                }
                res.count = filtered.size();
                res.values = filtered;
            }
            return res;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            musicDataFiltered = (ArrayList) results.values;
            notifyDataSetChanged();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView musicName;
        private TextView desc;
        private TextView likes;
        private Button listenbutton;
        private ImageView musicPicRes;

        public ViewHolder(View itemView) {
            super(itemView);
            musicName = itemView.findViewById(R.id.itemTitle);
            desc = itemView.findViewById(R.id.description);
            likes = itemView.findViewById(R.id.likes);
            musicPicRes = itemView.findViewById(R.id.itemImage);
            listenbutton = itemView.findViewById(R.id.listenbutton);
        }

        @SuppressLint("SetTextI18n") //what moron designed this
        public void bindTo(music newMusic) {
            musicName.setText("Song: "+newMusic.getMusicName());
            desc.setText("Description: "+String.valueOf(newMusic.getDesc()));
            likes.setText("Likes: "+String.valueOf(newMusic.getLikes()));
            Log.i("adapter", "downloading "+newMusic.getMusicPicRes());
            listenbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toMusic(newMusic);
                }
            });
            storageReference.child(newMusic.getMusicPicRes()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Log.i("adapter", "downloaded successfully "+task.getResult());
                    Glide.with(ncontext).load(task.getResult()).into(musicPicRes);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("adapter", e.getMessage());
                }
            });
        }
    }

    private void toMusic(music neededMusic) {
        Log.d("Activity", "pressed buton");
        Intent intent = new Intent(ncontext, ListenToMusic.class);
        intent.putExtra("musicRes", neededMusic.getMusicSourceRes());
        intent.putExtra("musicPicRes", neededMusic.getMusicPicRes());
        intent.putExtra("desc", neededMusic.getDesc());
        intent.putExtra("author", neededMusic.getAuthor());
        intent.putExtra("musicName", neededMusic.getMusicName());
        intent.putExtra("likes", neededMusic.getLikes());
        intent.putExtra("listenCount", neededMusic.getListenCount());
        ncontext.startActivity(intent);
    }
}
/**
 * private String musicName;
 * private String desc;
 * private float rating;
 * private final int musicSourceRes;
 * private final int musicPicRes;
 */