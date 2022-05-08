package com.example.muzika;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
@SuppressLint("NotifyDataSetChanged")
public class Feed extends AppCompatActivity {
    private FirebaseUser user;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final String LOG_TAG = Feed.class.getName();
    private static final int SECRET_KEY = 1234;
    private static final String PREF_KEY = Feed.class.getPackage().toString();
    private FirebaseFirestore store;
    private CollectionReference coll;
    private RecyclerView recView;
    private ArrayList<music> musicList;
    private musicAdapter adapter;
    private NotifHandler notifHandler;
    private int gridNum = 1;
    private int limit = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.i(LOG_TAG, "User found");
        } else {
            Log.e(LOG_TAG, "User not found, redirecting to login");
            finish();
        }
        notifHandler = new NotifHandler(this);
        recView = findViewById(R.id.recyclerView);
        recView.setLayoutManager(new GridLayoutManager(this, gridNum));
        musicList = new ArrayList<>();
        adapter = new musicAdapter(this, musicList);
        recView.setAdapter(adapter);
        store = FirebaseFirestore.getInstance();
        coll = store.collection("musics");
        adapter.getFilter().filter("");
        Log.i(LOG_TAG, "list is "+musicList.size());
    }

    private void query(){
        Log.i(LOG_TAG, "query called");
        musicList.clear();
        Log.i(LOG_TAG, this.coll.getPath());
        this.coll.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    Log.i(LOG_TAG, "successful "+ task.getResult().size());
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(LOG_TAG, document.getId() + " => " + document.getData());
                        music mus = document.toObject(music.class);
                        musicList.add(mus);
                        adapter.notifyDataSetChanged();
                    }

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(LOG_TAG, "asdf: "+e.getMessage());
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.musiclistmenu, menu);
        MenuItem menuItem = menu.findItem(R.id.searchbar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }
    public void toNewSong(){
        Intent intent = new Intent(this, musicAdd.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.log_out_button:
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            case R.id.settings_button:
                finish();
                return true;
            case R.id.newSong:
                toNewSong();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        musicList.clear();
        notifHandler.cancel();
        adapter.notifyDataSetChanged();
        query();
    }
}