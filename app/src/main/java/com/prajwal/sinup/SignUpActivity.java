package com.prajwal.sinup;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    EditText editTextEmail,editTextPassword;
    ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editTextEmail= (EditText) findViewById(R.id.editTextEmail);
        editTextPassword= (EditText) findViewById(R.id.editTextPassword);
        progressBar= (ProgressBar) findViewById(R.id.progressbar);

        mAuth = FirebaseAuth.getInstance();
        findViewById(R.id.buttonSignup).setOnClickListener(this);
        findViewById(R.id.textViewLogin).setOnClickListener(this);
     }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonSignup:
                registerUser();
                break;
            case R.id.textViewLogin:
                finish();
                startActivity(new Intent(this,MainActivity.class));
        }
    }

    private void registerUser() {
        String email=editTextEmail.getText().toString().trim();
        String password=editTextPassword.getText().toString().trim();

        if(email.isEmpty()){
            editTextEmail.setError("Email is Required");
            editTextEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Enter A valid Email");
            editTextEmail.requestFocus();
            return;
        }
        if(password.isEmpty()){
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }
        if(password.length()<6){
            editTextPassword.setError("Minimum length of password should be");
            editTextPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
               if(task.isSuccessful()){
                   finish();
               startActivity(new Intent(SignUpActivity.this,ProfileActivity.class));}
                   else{
                  if(task.getException() instanceof FirebaseAuthUserCollisionException){
                      Toast.makeText(getApplicationContext(), "You are Already registered", Toast.LENGTH_SHORT).show();
                  }
                  else{
                      Toast.makeText(getApplicationContext(),task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                  }
               }
            }
        });
    }

}
