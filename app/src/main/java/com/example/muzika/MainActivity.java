package com.example.muzika;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;


//main as in login, lmao
public class MainActivity extends AppCompatActivity {
    EditText username;
    EditText password;
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final int SECRET_KEY = 1234;
    private static final int RC_SIGNIN = 4567;
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    private SharedPreferences preferences;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private GoogleSignInClient googol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = findViewById(R.id.userName);
        password = findViewById(R.id.password);
        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
    }

    public void login(View view) {
        String user = username.getText().toString();
        String pass = password.getText().toString();
        auth.signInWithEmailAndPassword(user, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.i(LOG_TAG, "Logged in successfully");
                    loadFeed();
                } else {
                    Log.e(LOG_TAG, "unsuccessful login");
                    Toast.makeText(MainActivity.this, "Unsuccessful registration " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int reqCode, int result, Intent data) {
        super.onActivityResult(reqCode, result, data);
        if (reqCode == RC_SIGNIN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount acc = task.getResult(ApiException.class);
                fireauthgoogle(acc.getIdToken());
            } catch (ApiException e) {
                Log.e(LOG_TAG, "signinfailed", e);
            }
        }
    }

    private void fireauthgoogle(String id) {
        AuthCredential cred = GoogleAuthProvider.getCredential(id, null);
        auth.signInWithCredential(cred).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "google login successful");
                    loadFeed();
                } else {
                    Log.e(LOG_TAG, "google login unsuccessful");
                }
            }

        });
    }

    public void register(View view) {
        Intent intent = new Intent(this, registeractivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userName", username.getText().toString());
        editor.putString("password", password.getText().toString());
        editor.apply();

        Log.i(LOG_TAG, "Paused main");
    }

    private void loadFeed() {
        Intent intent = new Intent(this, Feed.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }
}