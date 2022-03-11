package com.example.simpleui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amplifyframework.core.Amplify;

public class SignUpConfirmationActivity extends AppCompatActivity {
    EditText usernameconfirm, confirmationcode;
    Button confirmbutton;
    boolean confirmresult;
    TextView result_view;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_confirmation);
        usernameconfirm = findViewById(R.id.usernameconfirmsignup);
        confirmationcode = findViewById(R.id.confirmationcode);
        confirmbutton = findViewById(R.id.confirmbutton);
        result_view = findViewById(R.id.result);
        confirmbutton.setOnClickListener(view -> {
            String username = usernameconfirm.getText().toString();
            String confirmcode = confirmationcode.getText().toString();
            Amplify.Auth.confirmSignUp(
                    username,
                    confirmcode,
                    result -> {
                        Log.i("AuthQuickstart", result.isSignUpComplete() ? "Confirm signUp succeeded" : "Confirm sign up not complete");
                        confirmresult = result.isSignUpComplete();
                    },
                    error -> {
                        Log.e("AuthQuickstart", error.toString());
                        confirmresult = false;
                    }
            );
            result_view.setText("Please wait while we confirm your sign up..");
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (confirmresult) {
                    result_view.setText("Confirmation succeeded!");
                } else {
                    result_view.setText("Confirmation failed. Please try again.");
                }
            }, 5000);
            handler.postDelayed(() -> {
                if (confirmresult){
                    Intent intent = new Intent(SignUpConfirmationActivity.this, SignInActivity.class);
                    startActivity(intent);}
                }, 8000);
            });
        }
    }