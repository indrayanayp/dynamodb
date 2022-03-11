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

import com.amazonaws.mobileconnectors.cognitoauth.Auth;
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession;
import com.amplifyframework.core.Amplify;

public class SignInActivity extends AppCompatActivity {
    EditText username_text, password_text;
    Button signin;
    TextView result_view, sign_uplink;
    boolean signedin = false;
    String result_text = "";


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        username_text = findViewById(R.id.usernamesignin);
        password_text = findViewById(R.id.passwordsignin);
        signin = findViewById(R.id.signinbutton);
        result_view = findViewById(R.id.resultsignin);
        sign_uplink = findViewById(R.id.signuplink);
        result_text = "";
        result_view.setText(result_text);



        signin.setOnClickListener(view -> {
            String username = username_text.getText().toString();
            String password = password_text.getText().toString();
            Log.i("MyAmplifyApp", "I am starting");
            Amplify.Auth.signIn(
                    username,
                    password,
                    result -> {
                        Log.i("AuthQuickstart", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete");
                        if (result.isSignInComplete()){
                            signedin = true;
                            result_text = "Sign in succeeded!";
                        }
                        else{
                            signedin = false;
                            result_text = "Sign in failed. Please try again.";
                        }
                    },
                    error -> {
                        Log.e("AuthQuickstart", error.toString());
                        result_text = "Sign in failed. Please try again.";
                    }
            );

            Log.i("MyAmplifyApp", "I am here");
            //handler.postDelayed(() -> result_view.setText(result_text), 10000);
            //String result_text1= result_text;
            //result_view.setText(result_text1);
            result_view.setText("Please wait while we sign you in..");
            Handler handler = new Handler();
            handler.postDelayed(() -> result_view.setText(result_text), 5000);
            handler.postDelayed(() -> {
                if (signedin){
                    globalVariable globalVariable = com.example.simpleui.globalVariable.getInstance();
                    globalVariable.setUsername_global("indrayanayp");
                    Intent intent = new Intent(SignInActivity.this, RecordingHistory.class);
                    startActivity(intent);
                    }
                else {
                    result_view.setText("Connection issue. Please try again.");
                }
            }, 6000);
        });

        sign_uplink.setOnClickListener(view -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent); });
    }
}