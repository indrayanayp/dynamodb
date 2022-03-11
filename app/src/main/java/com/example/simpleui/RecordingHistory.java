package com.example.simpleui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.icu.text.AlphabeticIndex;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.core.Amplify;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RecordingHistory extends AppCompatActivity {
    ArrayAdapter<String> adapterItems;
    String[] items = {"Today", "This Month", "This Year", "All Time"};
    String mode = "All Time";
    Boolean continuing = false ;
    ArrayList<Recording> scannedData = new ArrayList<>();


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_history);
        TextView recordingSummary = findViewById(R.id.RecordingSummary);
        ListView mListView = findViewById(R.id.RecordingList);
        AutoCompleteTextView auto = findViewById(R.id.autoCompleteTextView);
        Button button = findViewById(R.id.button3);

        globalVariable globalVariable = com.example.simpleui.globalVariable.getInstance();

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int date = calendar.get(Calendar.DATE);
        String year_now = String.valueOf(year);
        String month_now;
        String date_now;
        if (month < 10){
            month_now = "0" + String.valueOf(month);
        }
        else{
            month_now = String.valueOf(month);
        }
        if (date < 10){
            date_now = "0" + String.valueOf(date);
        }
        else{
            date_now = String.valueOf(date);
        }

        adapterItems = new ArrayAdapter<String>(this, R.layout.dropdown_item, items);
        auto.setAdapter(adapterItems);
        Log.i("MyAmplifyApp", date + " " + month + " " + year);


        auto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mode = adapterView.getItemAtPosition(position).toString();
                Log.i("MyAmplifyApp", mode);
            }
        });

        continuing = false;

        button.setOnClickListener(view -> {
            String[] retrievedData = new String[1];
            String dateparameter = "";

            retrievedData[0] = "init";
            if (mode.equals("All Time")){
                dateparameter = "";
            }
            else if (mode.equals("Today")){
                dateparameter = year_now + "-" + month_now + "-" + date_now;
            }
            else if (mode.equals("This Month")){
                dateparameter = year_now + "-" + month_now;
            }
            else if (mode.equals("This Year")){
                dateparameter = year_now;
            }

            retrievedData = ScanningDynamoDB(globalVariable.getUsername_global(), dateparameter);

            while (retrievedData[0].equals("init")){
                Log.i("MyAmplifyApp", "Stuck in loop");
            }

            Log.i("MyAmplifyApp", "Success! " + retrievedData[0]);
            if (retrievedData[0].equals("0")) {
                recordingSummary.setText("Sorry, no data found on the chosen time range.");
            } else if (retrievedData[0].equals("-99")) {
                recordingSummary.setText("Sorry, an error has occured. Please try again.");
            } else  {
                recordingSummary.setText("Heart rate recording history: ");
                continuing = true;
                ArrayList<Recording> scannedData = new ArrayList<>();
                for (int i = 0; i < retrievedData.length-1; i = i+3){
                    Recording data = new Recording("Date: " + retrievedData[i], "Time: " + retrievedData[i+1], retrievedData[i+2] + " BPM");
                    scannedData.add(data);
                    RecordingListAdapter adapter = new RecordingListAdapter(this, R.layout.adapter_view_layout, scannedData);
                    mListView.setAdapter(adapter);
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (continuing == true) {
                    Recording selectedItem = (Recording) parent.getItemAtPosition(position);
                    String date_search = selectedItem.getDate().replaceAll("Date: ", "");
                    String time_search = selectedItem.getTime().replaceAll("Time: ", "");
                    Log.i("MyAmplifyApp", date_search + " " + time_search);
                    globalVariable.setDate_global(date_search);
                    globalVariable.setTime_global(time_search);
                    Intent intent = new Intent(RecordingHistory.this, ShowHistory.class);
                    startActivity(intent);
                }
            }
        });
        }

        public void downloadToInternal (String key, String filename){
            Amplify.Storage.downloadFile(
                    key,
                    new File(getApplicationContext().getFilesDir() + filename),
                    result -> Log.i("MyAmplifyApp", "Successfully downloaded: " + result.getFile().getName()),
                    error -> Log.e("MyAmplifyApp", "Download Failure", error)
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

        //Fungsi membuat file
        private File makeFile () {
            File exampleFile = new File(getApplicationContext().getFilesDir(), "ExampleKey");
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(exampleFile));
                writer.append("198079,198019,100 \n");
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

        public void UploadDataToDynamoDB (String Username, String Date, String Time, String
        AvgHR, String AvgRestHR, String MaxHR, String MinHR, String Duration, String Abnormalities){
            String postOutput = UploadToDynamoDBTable(Username, Date, Time, AvgHR, AvgRestHR, MaxHR, MinHR, Duration, Abnormalities);
            String UploadToDynamoDBTableOutput;
            try {
                JSONObject status = new JSONObject(postOutput);
                UploadToDynamoDBTableOutput = status.getString("status");
            } catch (Throwable t) {
                UploadToDynamoDBTableOutput = "Failed";
            }
            if (UploadToDynamoDBTableOutput.equals("TableMade")) {
                Handler handler = new Handler();
                handler.postDelayed(() -> UploadToDynamoDBTable(Username, Date, Time, AvgHR, AvgRestHR, MaxHR, MinHR, Duration, Abnormalities), 7000);
                Log.i("MyAmplifyApp", "Tabel baru telah dibuat, dan data berhasil diupload.");
            } else if (UploadToDynamoDBTableOutput.equals("Successful")) {
                Log.i("MyAmplifyApp", "Upload data berhasil.");
            } else {
                Log.i("MyAmplifyApp", "Upload data gagal.");
            }
        }

        public String[] ScanningDynamoDB (String Username, String DateParameter){
            String postOutput = ScanDynamoDBTable(Username, DateParameter);
            Log.i("MyAmplifyApp", "Masuk ke fungsi scanning");
            String ScanningDynamoDBOutput = "Started";
            String date;
            String time;
            String averageHR;
            String[] result = new String[300];
            int count = 0;

            try {
                JSONObject scanResult = new JSONObject(postOutput);
                ScanningDynamoDBOutput = scanResult.getString("data");
                if (!ScanningDynamoDBOutput.equals("[]")) {
                    try {
                        Log.i("MyAmplifyApp", "Masuk ke fungsi try");
                        JSONArray scanResultArray = new JSONArray(ScanningDynamoDBOutput);
                        for (int i = 0; i < scanResultArray.length(); i=i+1) {
                            JSONObject json = scanResultArray.getJSONObject(i);
                            date = json.getString("Date");
                            time = json.getString("Time");
                            averageHR = json.getString("AvgHR");
                            //Recording sampleData = new Recording(date, time, averageHR);
                            result[i*3] = date;
                            result[i*3+1] = time;
                            result[i*3+2] = averageHR;
                            count++;
                            Log.i("MyAmplifyApp", "Yes!" + time);
                        }
                        ScanningDynamoDBOutput = "Ok";
                    } catch (Throwable t) {
                        ScanningDynamoDBOutput = "Failed";
                    }
                } else {
                    ScanningDynamoDBOutput = "NoData";
                    Log.i("MyAmplifyApp", "Tidak ada data yang sesuai");
                }
            } catch (Throwable t) {
                ScanningDynamoDBOutput = "Failed";
            }
            String[] final_result;
            while (ScanningDynamoDBOutput.equals("Started")){}
            if (ScanningDynamoDBOutput.equals("Failed")) {
                //Recording sampleData = new Recording("-99", "-99", "-99");
                //result[0] = sampleData;
                final_result = new String[1];
                final_result[0] = "-99";

            } else if (ScanningDynamoDBOutput.equals("NoData")) {
                //Recording sampleData = new Recording("0", "0", "0");
                //result[0] = sampleData;
                final_result = new String[1];
                final_result[0] = "0";
            }
            else {
                final_result = new String[count*3];
                for (int i = 0; i < count; i=i+1) {
                    final_result[3*i] = result[3*i];
                    final_result[3*i+1] = result[3*i+1];
                    final_result[3*i+2] = result[3*i+2];
                    Log.i("MyAmplifyApp", result[i*3] + " " + result[i*3+1] + " " + result[i*3+2]);
                }
            }
            Log.i("MyAmplifyApp", "Output keluar dari scanning");
            return final_result;
        }

        public String UploadToDynamoDBTable (String Username, String Date, String Time, String
        AvgHR, String AvgRestHR, String MaxHR, String MinHR, String Duration, String Abnormalities){
            RestOptions options = RestOptions.builder()
                    .addPath("/ta2122")
                    .addQueryParameters(Collections.singletonMap("Username", Username))
                    .addQueryParameters(Collections.singletonMap("Date", Date))
                    .addQueryParameters(Collections.singletonMap("Time", Time))
                    .addQueryParameters(Collections.singletonMap("AvgHR", AvgHR))
                    .addQueryParameters(Collections.singletonMap("AvgRestHR", AvgRestHR))
                    .addQueryParameters(Collections.singletonMap("MaxHR", MaxHR))
                    .addQueryParameters(Collections.singletonMap("MinHR", MinHR))
                    .addQueryParameters(Collections.singletonMap("Duration", Duration))
                    .addQueryParameters(Collections.singletonMap("Abnormalities", Abnormalities))
                    .build();

            String[] postResult = {"Null"};

            Amplify.API.post(options,
                    response -> {
                        String str = response.getData().asString();
                        Log.i("MyAmplifyApp", "POST succeeded." + str);
                        postResult[0] = str;
                    },
                    error -> {
                        Log.e("MyAmplifyApp", "POST failed.", error);
                        postResult[0] = "Error";
                    }
            );

            while (postResult[0].equals("Null")) {
            } //Wait until function finishes

            return postResult[0];
        }

        public String ScanDynamoDBTable (String Username, String DateParameter){
            RestOptions options = RestOptions.builder()
                    .addPath("/ta2122")
                    .addQueryParameters(Collections.singletonMap("Username", Username))
                    .addQueryParameters(Collections.singletonMap("DateParameter", DateParameter))
                    .build();

            String[] scanOutput = {"Null"};

            Amplify.API.put(options,
                    response -> {
                        Log.i("MyAmplifyApp", "SCAN succeeded");
                        String str = response.getData().asString();
                        scanOutput[0] = str;
                    },
                    error -> {
                        Log.e("MyAmplifyApp", "SCAN failed.", error);
                        scanOutput[0] = "Failed";
                    }
            );

            while (scanOutput[0].equals("Null")) {
                Log.i("MyAmplifyApp", "Stuck in loop");
            } //Wait until function finishes


            Log.i("MyAmplifyApp", scanOutput[0]);
            return scanOutput[0];
        }
    }