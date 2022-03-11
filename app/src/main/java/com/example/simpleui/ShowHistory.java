package com.example.simpleui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.core.Amplify;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collections;

public class ShowHistory extends AppCompatActivity {
TextView RD, ST, AHR, ARHR, MHR, MIHR, AD, RDU;
Button Download;
String username = "indrayanayp";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_history);

        RD = findViewById(R.id.recordingdate);
        ST = findViewById(R.id.starttime);
        AHR = findViewById(R.id.averageheartrate);
        ARHR = findViewById(R.id.averagerestingheartrate);
        MHR = findViewById(R.id.maximumheartrate);
        MIHR = findViewById(R.id.minimumheartrate);
        AD = findViewById(R.id.abnormalitiesdetected);
        RDU = findViewById(R.id.recordingduration);
        Download = findViewById(R.id.button2);

        String[] result = new String[8];
        globalVariable globalVariable = com.example.simpleui.globalVariable.getInstance();
        username = globalVariable.username_global;
        result = GettingFromDynamoDB(globalVariable.getUsername_global(), globalVariable.getDate_global(), globalVariable.getTime_global());
        Log.i("MyAmplifyApp", globalVariable.getUsername_global() + " " + globalVariable.getDate_global() + " " + globalVariable.getTime_global());
        if (result[0].equals("0")) {
            Log.i("MyAmplifyApp", "Tidak ada data yang diperoleh.");
        }
        else if (result[0].equals("-99")) {
            Log.i("MyAmplifyApp", "Error dalam proses GET.");
        }
        else {
            RD.setText(result[0]);
            ST.setText(result[1]);
            AHR.setText(result[2]+ " BPM");
            ARHR.setText(result[3]+ " BPM");
            MHR.setText(result[4]+ " BPM");
            MIHR.setText(result[5]+ " BPM");
            RDU.setText(result[6]);
            if (result[7].equals("1")){
                AD.setText("Tachycardia");}
            else if (result[7].equals("0")){
                AD.setText("None");
            }
            else if (result[7].equals("2")){
                AD.setText("Bradycardia");
            }

            String key = username + "/" + result[0] + "_" + result[1] + ".csv";
            Log.i("MyAmplifyApp", key);

            Download.setOnClickListener(view -> {
                downloadFile(key);
            });
        }

    }
    //Fungsi membuat file
    private File makeFile () {
        File exampleFile = new File(getApplicationContext().getFilesDir(), "Indrayana.csv");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(exampleFile));
            writer.append("190790,198019,100 \n");
            writer.append("198079,199019,101 \n");
            writer.append("199978,188019,99 \n");
            writer.close();
        } catch (Exception exception) {
            Log.e("MyAmplifyApp", "File creation failed", exception);
        }
        return exampleFile;
    }

    //Fungsi untuk upload file
    private void uploadFile (String filename, File exampleFile){
        Amplify.Storage.uploadFile(
                filename,
                exampleFile,
                result -> Log.i("MyAmplifyApp", "Successfully uploaded: " + result.getKey()),
                storageFailure -> Log.e("MyAmplifyApp", "Upload failed", storageFailure)
        );
    }
    //Fungsi download file dari AWS S3
    public void downloadFile (String key){
        Amplify.Storage.getUrl(
                key,
                result -> {
                    Log.i("MyAmplifyApp", "Successfully generated: " + result.getUrl());
                    openWebURL(result.getUrl().toString());
                },
                error -> Log.e("MyAmplifyApp", "URL generation failure", error)
        );
    }

    //Fungsi untuk download file data secara langsung dari S3
    public void openWebURL (String inURL){
        Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(inURL));
        startActivity(browse);
    }

    public String[] GettingFromDynamoDB (String Username, String Date, String Time){
        String date, time, AvgHR, AvgRestHR, MaxHR, MinHR, Duration, Abnormalities;
        String getOutput = GetFromDynamoDBTable(Username, Date, Time);
        String GettingFromDynamoDBOutput;
        String[] result = new String[8];
        Log.i("MyAmplifyApp", "Masuk ke Getting");
        if (getOutput.equals("{}")) {
            Log.i("MyAmplifyApp", "Tidak ada data yang ditemukan.");
            result[0] = "0";
        } else {
            try {
                JSONObject getResult = new JSONObject(getOutput);
                GettingFromDynamoDBOutput = getResult.getString("data");
                try {
                    JSONObject json = new JSONObject(GettingFromDynamoDBOutput);
                    date = json.getString("Date");
                    time = json.getString("Time");
                    AvgHR = json.getString("AvgHR");
                    AvgRestHR = json.getString("AvgRestHR");
                    MaxHR = json.getString("MaxHR");
                    MinHR = json.getString("MinHR");
                    Duration = json.getString("Duration");
                    Abnormalities = json.getString("Abnormalities");
                    Log.i("MyAmplifyApp", date + " " + time + " " + AvgHR + " " + AvgRestHR + " " + MaxHR + " " + MinHR + " " + " " + Duration + " " + Abnormalities);
                    result[0] = date;
                    result[1] = time;
                    result[2] = AvgHR;
                    result[3] = AvgRestHR;
                    result[4] = MaxHR;
                    result[5] = MinHR;
                    result[6] = Duration;
                    result[7] = Abnormalities;
                } catch (Throwable t) {
                    GettingFromDynamoDBOutput = "Failed";
                    result[0] = "-99";
                }
            } catch (Throwable t) {
                GettingFromDynamoDBOutput = "Failed";
                result[0] = "-99";
            }
            if (GettingFromDynamoDBOutput.equals("Failed")) {
                Log.e("MyAmplifyApp", "GET gagal.");
            }
        }
        Log.i("MyAmplifyApp", "Keluar dari Getting");
        return result;
    }

    public String GetFromDynamoDBTable (String Username, String Date, String Time){
        RestOptions options = RestOptions.builder()
                .addPath("/ta2122")
                .addQueryParameters(Collections.singletonMap("Username", Username))
                .addQueryParameters(Collections.singletonMap("Date", Date))
                .addQueryParameters(Collections.singletonMap("Time", Time))
                .build();

        String[] getOutput = {"Null"};

        Amplify.API.get(options,
                response -> {
                    Log.i("MyAmplifyApp", "GET succeeded");
                    String str = response.getData().asString();
                    getOutput[0] = str;
                },
                error -> {
                    Log.e("MyAmplifyApp", "GET failed.", error);
                    getOutput[0] = "Failed";
                }
        );

        while (getOutput[0].equals("Null")) {
            Log.i("MyAmplifyApp", "Stuck in loop of GetData");
        } //Wait until function finishes

        return getOutput[0];
    }
}