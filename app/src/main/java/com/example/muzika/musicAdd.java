package com.example.muzika;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.IOException;
import java.util.UUID;

public class musicAdd extends AppCompatActivity {
    int SELECT_PICTURE = 200;
    int SELECT_MP3 = 300;
    Button BSelectImage;
    Button BSelectMP3;
    TextView mp3url;
    TextView picurl;
    Button BSubmitter;
    EditText songnamefield;
    EditText descriptionfield;
    String songName = "";
    String Description = "";
    private Uri picture;
    private Uri song;
    private Uri imgURL;
    private Uri songURL;

    FirebaseStorage storage;
    FirebaseFirestore firestore;
    StorageReference storageReference;
    CollectionReference collectionReference;
    CollectionReference userCollection;
    FirebaseUser user;
    private NotifHandler notifHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_music_add);
        user = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        firestore = FirebaseFirestore.getInstance();
        userCollection = firestore.collection("newUsers");
        collectionReference = firestore.collection("musics");
        super.onCreate(savedInstanceState);
        songnamefield = this.findViewById(R.id.songname);
        descriptionfield = this.findViewById(R.id.descriptionSong);
        BSelectImage = findViewById(R.id.songpicbutton);
        BSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectPicture(v);
            }
        });
        BSelectMP3 = findViewById(R.id.mp3uploadbutton);
        BSelectMP3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectMp3(v);
            }
        });
        BSubmitter = findViewById(R.id.submit);
        BSubmitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    submitSong(v);
                } catch (IOException e) {
                    Toast.makeText(v.getContext(), "Maybe try filling in the blanks to your vibe?", Toast.LENGTH_LONG).show();
                }
            }
        });
        ActionBar actionBar;
        actionBar = getSupportActionBar();
        ColorDrawable colorDrawable
                = new ColorDrawable(
                Color.parseColor("#0F9D58"));
        actionBar.setBackgroundDrawable(colorDrawable);
        mp3url = findViewById(R.id.mp3url);
        picurl = findViewById(R.id.mp3imageupload);
        notifHandler = new NotifHandler(this);
    }

    public void SelectPicture(View view) {
        if(!isReadStoragePermissionGranted()) return;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data.getData() != null) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImage = data.getData();
                if (null != selectedImage) {
                    Log.i("New Music", "Added a picture "+selectedImage);
                    this.picture = selectedImage;
                    picurl.setText(selectedImage.toString());
                }
            }
            if (requestCode == SELECT_MP3) {
                Uri selectedSong = data.getData();
                if (selectedSong != null) {
                    Log.i("New Music", "Added a song "+selectedSong);
                    this.song = selectedSong;
                    mp3url.setText(selectedSong.toString());
                }
            }
        }
    }

    public void submitSong(View view) throws IOException{
        try{
            String email = user.getEmail();
            String author = email; //ah, sod, getting username from collection seems finnicky rn
            String songname = songnamefield.getText().toString();
            String desc = descriptionfield.getText().toString();
            if(songname.trim().equals("") || desc.trim().equals("") ||picture.toString().equals("") || song.toString().equals("")){
                throw new IOException();
            }
            Log.i("asdf", songname);
            Log.i("asdf", desc);
            uploadImage();
            uploadMP3();
            music newMusic = new music(songname, author, desc, "mp3/" +song.toString(), "images/"+picture.toString());
            uploadMusicData(newMusic);
        }catch (IOException e){
            Toast.makeText(this, "Maybe try filling in the blanks? ", Toast.LENGTH_LONG).show();
        }
        catch (Exception e){
            Toast.makeText(this, "It seems something went wrong, maybe try a different vibe? ", Toast.LENGTH_LONG).show();
        }
    }

    public void selectMp3(View view) {
        if(!isReadStoragePermissionGranted()) return;
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, SELECT_MP3);
    }


    private void uploadImage() { //all this for uploading an image... damn
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading song picture...");
        progressDialog.show();
        StorageReference ref = storageReference.child("images/" + picture.toString());
        ref.putFile(picture).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Log.i("New Music", "Picture of song uploaded successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e("New Music", "Picture uploading failed " + e.getMessage());
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressDialog.setMessage("Uploaded " + (int) progress + "%");
            }
        });
        progressDialog.dismiss();
    }
    public boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("New music","Permission is granted1");
                return true;
            } else {

                Log.v("New music","Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("New music","Permission is granted1");
            return true;
        }
    }

    private void uploadMP3() {
        try {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading song...");
        progressDialog.show();
        StorageReference ref = storageReference.child("mp3/" + song.toString());
        ref.putFile(this.song).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Log.i("New Music", "Picture of song uploaded successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.e("New Music", "Picture uploading failed " + e.getMessage());
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressDialog.setMessage("Uploaded " + (int) progress + "%");
            }
        });
        progressDialog.dismiss();
    }
        catch (Exception e){
            Toast.makeText(this, "It seems that vibe is already uploaded...", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.backbutton, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.backToFeed:
                loadFeed(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 3) {
            Log.d("New Music", "External storage1");
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.v("New Music", "Permission: " + permissions[0] + "was " + grantResults[0]);
                //resume tasks needing this permission
            } else {
                Log.e("New Music", "Permission not granted");
            }
        }
    }
    private void uploadMusicData(music NewMusic) {
        this.collectionReference.add(NewMusic).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                try{
                    loadFeed(true);
                }catch (Exception e){
                    Log.e("New Music", e.getMessage());
                }
            }
        });
    }
    private void loadFeed(boolean Uploading) { //just because othervise this would log out with finish, dunno why tho
        if(Uploading) notifHandler.send("Successfully uploaded your music! vibe");
        Intent intent = new Intent(this, Feed.class);
        startActivity(intent);
    }
}