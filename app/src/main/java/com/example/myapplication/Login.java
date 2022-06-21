package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private EditText pass,email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //night mode ui is not supported

        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.emailedit);
        pass = findViewById(R.id.passwordedit);
            email.setText("");
            pass.setText("");

        //if the holder has already logged in once while using the app there's no need to sign in again
            if(auth.getCurrentUser()!=null)
                startActivity(new Intent(this, NewReportActivity.class));
        }



    //if the authentication is successful the holder is sent to the Sender activity otherwise an error message appears
    public void signIn(View view){
        if (email.getText().toString().matches("") || pass.getText().toString().matches("")){
            Toast toast = Toast.makeText(this, R.string.blankField, Toast.LENGTH_SHORT);
            View v = toast.getView();
            v.setBackgroundResource(R.drawable.error_toast);
            TextView t = (TextView) toast.getView().findViewById(android.R.id.message);
            t.setTextColor(Color.RED);
            toast.show();
        }

        auth.signInWithEmailAndPassword(email.getText().toString(),pass.getText().toString()).addOnCompleteListener(this,
                task -> { if(task.isSuccessful()) {
                    currentUser = auth.getCurrentUser();
                    startActivity(new Intent(this, MyReportsActivity.class).putExtra("activity",0));
                }
                else Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show(); });
    }

}