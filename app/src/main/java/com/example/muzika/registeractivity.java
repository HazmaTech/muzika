package com.example.muzika;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class registeractivity extends AppCompatActivity {

    private static final String LOG_TAG = registeractivity.class.getName();
    private static final String PREF_KEY = registeractivity.class.getPackage().toString();
    private static final int SECRET_KEY = 1234;

    EditText user;
    EditText email;
    EditText pass;
    EditText repass;
    RadioGroup creatorListener;
    private SharedPreferences preferences;
    private FirebaseAuth auth;
    FirebaseFirestore firestore;
    CollectionReference collectionReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        firestore = FirebaseFirestore.getInstance();
        collectionReference = firestore.collection("NewUsers");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeractivity);
        int key = getIntent().getIntExtra("SECRET_KEY", 0);
        if (key != 1234) {
            finish();
        }
        auth = FirebaseAuth.getInstance();
        user = findViewById(R.id.userName);
        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        repass = findViewById(R.id.repassword);
        creatorListener = findViewById(R.id.accountTypeGroup);
        creatorListener.check(R.id.listener);
        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        Log.i(LOG_TAG, "Entered register ac");
    }

    public void register(View view) {
        String userName = user.getText().toString();
        String emailText = email.getText().toString();
        String password = pass.getText().toString();
        String passwordConfirm = repass.getText().toString();
        int accType = creatorListener.getCheckedRadioButtonId();
        RadioButton radioButton = (RadioButton) findViewById(accType);
        String role;
        Log.i(LOG_TAG, radioButton.getText().toString());
        if (radioButton.getText().toString().equals("Listener")) role = "Listener";
        else role = "Creator";
        if (!password.equals(passwordConfirm)) {
            Log.e(LOG_TAG, "Passwords don't match");

            return;
        }
        Log.i(LOG_TAG, "Registering user " + userName + "...");

        collectionReference.add(new User(userName, emailText, role)).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.i(LOG_TAG, "Added new user to collection");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        });
        auth.createUserWithEmailAndPassword(emailText, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.i(LOG_TAG, "Registration is successful");
                    loadFeed();
                } else {
                    Log.e(LOG_TAG, "Unsuccessful registration");
                    Toast.makeText(registeractivity.this, "Unsuccessful registration " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.backbutton, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.backToFeed:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void loadFeed() {
        Intent intent = new Intent(this, Feed.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

}