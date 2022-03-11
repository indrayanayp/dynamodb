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

import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.core.Amplify;

public class SignUpActivity extends AppCompatActivity {
    EditText username_signup, password_signup, email_signup, repassword_signup;
    Button signup;
    TextView result_signup;
    String result_notif;
    boolean ContinueToConfirmation;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        email_signup = findViewById(R.id.usernamesignin);
        username_signup = findViewById(R.id.usernamesignup2);
        password_signup = findViewById(R.id.passwordsignin);
        repassword_signup = findViewById(R.id.passwordsignup2);
        signup= findViewById(R.id.signinbutton);
        result_signup = findViewById(R.id.resultsignup);
        signup.setOnClickListener(view -> {
            String username = username_signup.getText().toString();
            String email = email_signup.getText().toString();
            String password = password_signup.getText().toString();
            String repassword = repassword_signup.getText().toString();
            if (!password.equals(repassword)) {
                result_signup.setText("Password and retyped password do not match.");
            }
            else {
                AuthSignUpOptions options = AuthSignUpOptions.builder()
                        .userAttribute(AuthUserAttributeKey.email(), email)
                        .build();
                Amplify.Auth.signUp(username, password, options,
                        result -> {
                                Log.i("AuthQuickStart", "Result: " + result.toString());
                                if (result.isSignUpComplete()){
                                    result_notif = "Sign up succeeded!";
                                    ContinueToConfirmation = true;
                                    Log.i("MyAmplifyApp", result_notif);
                                }
                                else {
                                    result_notif = "Sign up process failed. Try another email/username or ensure that your password contains minimum 8 characters.";
                                    ContinueToConfirmation = false;
                                    Log.i("MyAmplifyApp", result_notif);
                                }
                        },

                        error -> {
                            Log.e("AuthQuickStart", "Sign up failed", error);
                            result_notif = "Sign up process failed. Try another email/username or ensure that your password contains minimum 8 characters.";
                            ContinueToConfirmation = false;
                            Log.i("MyAmplifyApp", result_notif);
                        }
                );
                result_signup.setText("Please wait while we sign you up...");
                Handler handler = new Handler();
                handler.postDelayed(() -> result_signup.setText(result_notif), 5000);
                handler.postDelayed(() -> {
                   if (ContinueToConfirmation){
                       Intent intent = new Intent(SignUpActivity.this, SignUpConfirmationActivity.class);
                       startActivity(intent);}}, 7000);
                }
        });
        }
    }