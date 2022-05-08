package com.example.muzika;

import androidx.annotation.ContentView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firestore.v1.WriteResult;

import java.io.IOException;
import java.lang.invoke.MutableCallSite;

public class ListenToMusic extends AppCompatActivity {

    MediaPlayer mp;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ImageView imageView;
    private FirebaseFirestore firestore;
    private CollectionReference collectionReference;
    private NotifHandler notifHandler;
    private music Muzika;
    String musicPicRes = "";
    String musicSourceRes = "";
    String desc = "";
    String author = "";
    String musicName = "";
    int likes = 0;
    int listenCount = 0;
    boolean playing = false;
    private String mus = "";
    boolean likedRightNow = false;
    Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_to_music);
        seperateInit();
    }

    public void seperateInit() {
        notifHandler = new NotifHandler(this);
        mp = new MediaPlayer();
        imageView = findViewById(R.id.imageView);
        firestore = FirebaseFirestore.getInstance();
        animation = AnimationUtils.loadAnimation(this, R.anim.musicblinking);
        collectionReference = firestore.collection("musics");
        try { //wow this is so stupid
            Bundle bundle = getIntent().getExtras();
            musicSourceRes = bundle.getString("musicRes");
            musicPicRes = bundle.getString("musicPicRes");
            desc = bundle.getString("desc");
            author = bundle.getString("author");
            musicName = bundle.getString("musicName");
            likes = bundle.getInt("likes");
            listenCount = bundle.getInt("listenCount");
        } catch (Exception e) {
            Log.e("Listen", e.getMessage());
        }
        Log.i("Listen", musicSourceRes);
        collectionReference.limit(1).whereEqualTo("musicSourceRes", musicSourceRes).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.i("Listen", "AAAAAAAAAAA" + task.getResult().size());
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.i("Listen", document.get("author").toString());
                    Muzika = document.toObject(music.class);
                    Muzika.setListenCount(Muzika.getListenCount() + 1);
                    String id = document.getId();
                    Task<Void> result = collectionReference.document(id).update("listenCount", Muzika.getListenCount());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Listen", e.getMessage());
            }
        });
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        storageReference.child(musicSourceRes).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Log.i("Listen", "successful music download");
                try {
                    mus = task.getResult().toString();
                    mp.setDataSource(task.getResult().toString());
                    mp.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Listen", "Couldn't download " + e.getMessage());
            }
        });
        seperateImageLoader(Muzika, this.imageView);
        TextView Title = (TextView) findViewById(R.id.Title);
        Title.setText(musicName);
        TextView likesText = findViewById(R.id.LikesCount);
        TextView listenText = findViewById(R.id.ListenCount);
        TextView authorText = findViewById(R.id.AuthorFill);
        TextView descText = findViewById(R.id.DescriptionBox);
        likesText.setText(String.valueOf(likes) + " likes");
        listenText.setText(String.valueOf(listenCount + 1) + " listens");
        authorText.setText(author);
        descText.setText(desc);
    }

    public void seperateImageLoader(music Muzika, View view) {
        storageReference.child(musicPicRes).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Log.i("Listen", "successful music download");
                Glide.with(view).load(task.getResult()).into(imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Listen", "Couldn't download " + e.getMessage());
            }
        });
    }


    public void PlaySong(View view) {
        if (playing) {
            imageView.clearAnimation();
            mp.stop();
            try {
                mp.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            playing = false;
        } else {
            imageView.startAnimation(animation);
            mp.start();
            playing = true;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.musicsingleitemlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.backTo:
                finish();
                return true;
            case R.id.deletesong:
                deleteCurrentSong();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void deleteCurrentSong() {
        collectionReference.limit(1)
                .whereEqualTo("musicName", musicName)
                .whereEqualTo("desc", desc)
                .whereEqualTo("musicPicRes", musicPicRes)
                .whereEqualTo("author", author)
                .whereEqualTo("listenCount", listenCount+1)
                .whereEqualTo("musicSourceRes", musicSourceRes) // welp, this is a thing of beauty
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                Log.i("Listen", "AAAAAAAAAAA" + task.getResult().size());
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String id = document.getId();
                    Task<Void> result = collectionReference.document(id).delete();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Listen", e.getMessage());
            }
        });
        try {
            notifHandler.send("Deleting current song...no vibes allowed, huh");
            finish();
        } catch (Exception e) {
            Log.e("Listen", e.getMessage());
        }

    }

    public void LikeSonghaha(View view) {
        if (!likedRightNow) {
            this.likedRightNow = true;
            collectionReference.limit(1).whereEqualTo("musicSourceRes", musicSourceRes).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    Log.i("Listen", "AAAAAAAAAAA" + task.getResult().size());
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.i("Listen", document.get("author").toString());
                        Muzika = document.toObject(music.class);
                        Muzika.setLikes(Muzika.getLikes() + 1);
                        String id = document.getId();
                        TextView likesText = findViewById(R.id.LikesCount);
                        likesText.setText(String.valueOf(likes + 1) + " likes");
                        Task<Void> result = collectionReference.document(id).update("likes", Muzika.getLikes());
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Listen", e.getMessage());
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        mp.stop();
        super.onDestroy();
    }
}