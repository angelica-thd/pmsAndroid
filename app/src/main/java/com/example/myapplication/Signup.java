package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class Signup extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase db;
    private DatabaseReference ref;
    private int pressed;

    private EditText pass,email,confirm_pass,username,fullname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //night mode ui is not supported
        pressed = 0;

        auth = FirebaseAuth.getInstance();      //firebase authentication initialization
        // Write a message to the database
        db = FirebaseDatabase.getInstance("https://course9-b6dac-default-rtdb.europe-west1.firebasedatabase.app/");
        ref = db.getReference("users");

        email = findViewById(R.id.emailedit);
        username = findViewById(R.id.username_st);
        fullname = findViewById(R.id.fname_st);
        pass = findViewById(R.id.pass_st);
        confirm_pass = findViewById(R.id.pass_conf_st);
        email.setText("");
        pass.setText("");
        confirm_pass.setText("");


    }

    //create a new account with a 2 factor authentication and a confirmation of the password
    public void signUp(View view){
        Log.i("click","click");
        pressed +=1;
        //check iff any fields are empty and check if the two password fields are the same
        if(!email.getText().toString().equals("")
                || !email.getText().toString().equals(null)
                || !username.getText().toString().equals("")
                || !username.getText().toString().equals(null)
                || !fullname.getText().toString().equals("")
                || !fullname.getText().toString().equals(null)
                || !pass.getText().toString().equals("")
                || !pass.getText().toString().equals(null)
                || !pass.getText().toString().equals("")
                || !confirm_pass.getText().toString().equals(null)) {

            final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";

            Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
            if (!pattern.matcher(pass.getText().toString()).matches()){
                findViewById(R.id.pass_req).setVisibility(View.VISIBLE);
            }
            if(!pass.getText().toString().matches(confirm_pass.getText().toString())) {
                Toast toast = Toast.makeText(this, R.string.invalid_pass, Toast.LENGTH_SHORT);
                View tview = toast.getView();
                tview.setBackgroundResource(R.drawable.error_toast);
                TextView text = (TextView) toast.getView().findViewById(android.R.id.message);
                text.setTextColor(Color.RED);
                toast.show();
            }

            if(pressed > 0){
                Log.i("click","press");


                User user = new User() ;
                user.setEmail(email.getText().toString());
                user.setFullname(fullname.getText().toString());
                user.setUsername(username.getText().toString());


                auth.createUserWithEmailAndPassword(email.getText().toString(),pass.getText().toString()).addOnCompleteListener(this,
                        task -> {
                            if(task.isSuccessful()){
                                currentUser = auth.getCurrentUser();
                                changeUserProfile(username.getText().toString(),currentUser);
                                ref.child(currentUser.getUid()).setValue(user).addOnCompleteListener(t -> {
                                    if(t.isSuccessful()){
                                        Log.i("click","ref");
                                        startActivity(new Intent(this,MainActivity.class));
                                    }
                                    else  Log.i("click",task.getException().getMessage());

                                }).addOnFailureListener(e -> Log.i("click",e.getMessage()));

                            }else  Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            pressed = 0;
                        });
            }

        }else {
            Toast toast = Toast.makeText(this, R.string.blankField, Toast.LENGTH_SHORT);
            View tview = toast.getView();
            tview.setBackgroundResource(R.drawable.error_toast);
            TextView text = (TextView) toast.getView().findViewById(android.R.id.message);
            text.setTextColor(Color.RED);
            toast.show();
        }

    }


    private void changeUserProfile(String username,FirebaseUser user){
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
        user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(),"User  updated",Toast.LENGTH_LONG).show();
            }
        });
    }

    //go back to login activity
    public void login(View view){ startActivity(new Intent(this,Login.class));}
}

