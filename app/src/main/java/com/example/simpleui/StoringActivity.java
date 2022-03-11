package com.example.simpleui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.core.Amplify;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collections;

public class StoringActivity extends AppCompatActivity {
    TextView resulttext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storing);
        //File newFile = makeFile();
        //uploadFile("testingfile.txt", newFile);
        //downloadFile("testingfile.txt");
        //
    }

    //Fungsi download file ke storage internal aplikasi
    public void downloadToInternal(String key, String filename) {
        Amplify.Storage.downloadFile(
                key,
                new File(getApplicationContext().getFilesDir() + filename),
                result -> Log.i("MyAmplifyApp", "Successfully downloaded: " + result.getFile().getName()),
                error -> Log.e("MyAmplifyApp", "Download Failure", error)
        );
    }

    //Fungsi download file dari AWS S3
    public void downloadFile(String key) {
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
    public void openWebURL(String inURL) {
        Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(inURL));
        startActivity(browse);
    }

    //Fungsi membuat file
    private File makeFile() {
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
    private void uploadFile(String filename, File exampleFile) {
        Amplify.Storage.uploadFile(
                filename,
                exampleFile,
                result -> Log.i("MyAmplifyApp", "Successfully uploaded: " + result.getKey()),
                storageFailure -> Log.e("MyAmplifyApp", "Upload failed", storageFailure)
        );
    }

    public void UploadDataToDynamoDB(String Username, String Date, String Time, String AvgHR, String AvgRestHR, String MaxHR, String MinHR, String Abnormalities) {
        String postOutput = UploadToDynamoDBTable(Username, Date, Time, AvgHR, AvgRestHR, MaxHR, MinHR, Abnormalities);
        String UploadToDynamoDBTableOutput;
        try {
            JSONObject status = new JSONObject(postOutput);
            UploadToDynamoDBTableOutput = status.getString("status");
        } catch (Throwable t) {
            UploadToDynamoDBTableOutput = "Failed";
        }
        if (UploadToDynamoDBTableOutput.equals("TableMade")) {
            Handler handler = new Handler();
            handler.postDelayed(() -> UploadToDynamoDBTable(Username, Date, Time, AvgHR, AvgRestHR, MaxHR, MinHR, Abnormalities), 7000);
            Log.i("MyAmplifyApp", "Tabel baru telah dibuat, dan data berhasil diupload.");
        } else if (UploadToDynamoDBTableOutput.equals("Successful")) {
            Log.i("MyAmplifyApp", "Upload data berhasil.");
        } else {
            Log.i("MyAmplifyApp", "Upload data gagal.");
        }
    }


    public void ScanningDynamoDB(String Username, String DateParameter) {
        String postOutput = ScanDynamoDBTable(Username, DateParameter);
        String ScanningDynamoDBOutput;
        try {
            JSONObject scanResult = new JSONObject(postOutput);
            ScanningDynamoDBOutput = scanResult.getString("data");
            if (!ScanningDynamoDBOutput.equals("[]")) {
                try {
                    JSONArray scanResultArray = new JSONArray(ScanningDynamoDBOutput);
                    for (int i = 0; i < scanResultArray.length(); i++) {
                        JSONObject json = scanResultArray.getJSONObject(i);
                        String date = json.getString("Date");
                        String time = json.getString("Time");
                        String averageHR = json.getString("AvgHR");
                        Log.i("MyAmplifyApp", date + " " + time + " " + averageHR);
                    }
                } catch (Throwable t) {
                    ScanningDynamoDBOutput = "Failed";
                }
            } else {
                Log.i("MyAmplifyApp", "Tidak ada data yang sesuai");
            }
        } catch (Throwable t) {
            ScanningDynamoDBOutput = "Failed";
        }
        if (ScanningDynamoDBOutput.equals("Failed")) {
            Log.e("MyAmplifyApp", "Scanning gagal.");
        }
    }

    public void GettingFromDynamoDB(String Username, String Date, String Time) {
        String date, time, AvgHR, AvgRestHR, MaxHR, MinHR, Abnormalities;
        String getOutput = GetFromDynamoDBTable(Username, Date, Time);
        String GettingFromDynamoDBOutput;
        if (getOutput.equals("{}")) {
            Log.i("MyAmplifyApp", "Tidak ada data yang ditemukan.");
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
                    Abnormalities = json.getString("Abnormalities");
                    Log.i("MyAmplifyApp", date + " " + time + " " + AvgHR + " " + AvgRestHR + " " + MaxHR + " " + MinHR + "" + Abnormalities);
                } catch (Throwable t) {
                    GettingFromDynamoDBOutput = "Failed";
                }
            } catch (Throwable t) {
                GettingFromDynamoDBOutput = "Failed";
            }
            if (GettingFromDynamoDBOutput.equals("Failed")) {
                Log.e("MyAmplifyApp", "GET gagal.");
            }
        }
    }

    //UPDATE DATA KE DYNAMODB
    //Prosedur ini diulang 2 kali, agar ketika tabel tidak ada, iterasi pertama akan membuat tabel
    //Contoh penggunaan, ikuti langkah yang dikomen di bawah
    /*UpdateDynamoDBTable("indrayanayp", "27/06/2000", "12:12:12", "109", "86", "140", "56", "1");
        Handler handler = new Handler();
        handler.postDelayed(() -> {
        UpdateDynamoDBTable("indrayanayp", "27/06/2000", "12:12:12", "109", "86", "140", "56", "1");
    }, 10000);*/






/*    try {
        JSONArray jsonarray = new JSONArray(str);
        for (int i = 0; i < jsonarray.length(); i++){
            JSONObject json = jsonarray.getJSONObject(i);
            String data1 = json.getString("data1");
            Log.i("MyAmplifyApp", data1);}
    } catch (Throwable t) {
        Log.e("MyAmplifyApp", "Could not parse malformed JSON: \"" + str + "\"");
    }*/

    //ADA 4 JENIS OUTPUT: Succesful, TableMade, TableNotMade, dan Failed
    public String UploadToDynamoDBTable(String Username, String Date, String Time, String AvgHR, String AvgRestHR, String MaxHR, String MinHR, String Abnormalities) {
        RestOptions options = RestOptions.builder()
                .addPath("/ta2122")
                .addQueryParameters(Collections.singletonMap("Username", Username))
                .addQueryParameters(Collections.singletonMap("Date", Date))
                .addQueryParameters(Collections.singletonMap("Time", Time))
                .addQueryParameters(Collections.singletonMap("AvgHR", AvgHR))
                .addQueryParameters(Collections.singletonMap("AvgRestHR", AvgRestHR))
                .addQueryParameters(Collections.singletonMap("MaxHR", MaxHR))
                .addQueryParameters(Collections.singletonMap("MinHR", MinHR))
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

    public String ScanDynamoDBTable(String Username, String DateParameter) {
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
                    Log.e("MyAmplifyApp", "POST failed.", error);
                    scanOutput[0] = "Failed";
                }
        );

        while (scanOutput[0].equals("Null")) {
        } //Wait until function finishes

        return scanOutput[0];
    }

    public String GetFromDynamoDBTable(String Username, String Date, String Time) {
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
        } //Wait until function finishes

        return getOutput[0];
    }

/*    public void UpdateDynamoDBTable(String Username, String Date, String Time, String AvgHR, String AvgRestHR, String MaxHR, String MinHR, String Abnormalities) {
        RestOptions options = RestOptions.builder()
                .addPath("/ta2122")
                .addQueryParameters(Collections.singletonMap("Username", Username))
                .addQueryParameters(Collections.singletonMap("Date", Date))
                .addQueryParameters(Collections.singletonMap("Time", Time))
                .addQueryParameters(Collections.singletonMap("AvgHR", AvgHR))
                .addQueryParameters(Collections.singletonMap("AvgRestHR", AvgRestHR))
                .addQueryParameters(Collections.singletonMap("MaxHR", MaxHR))
                .addQueryParameters(Collections.singletonMap("MinHR", MinHR))
                .addQueryParameters(Collections.singletonMap("Abnormalities", Abnormalities))
                .build();

        String[] outputofpost = {"Null"};

        Amplify.API.post(options,
                response -> {
                    Log.i("MyAmplifyApp", "POST succeeded");
                    String str = response.getData().asString();
                    outputofpost[0] = str;
                },
                error -> {
                    Log.e("MyAmplifyApp", "POST failed.", error);
                    outputofpost[0] = "Failed";
                }
        );

        while (outputofpost[0].equals("Null")){
            //Log.i("MyAmplifyApp", "stuck");
        }
        String str = outputofpost[0];

        try {
            JSONArray jsonarray = new JSONArray(str);
            for (int i = 0; i < jsonarray.length(); i++){
                JSONObject json = jsonarray.getJSONObject(i);
                String data1 = json.getString("data1");
                Log.i("MyAmplifyApp", data1);}
        } catch (Throwable t) {
            Log.e("MyAmplifyApp", "Could not parse malformed JSON: \"" + str + "\"");
        }
    }*/
}
//("indrayanayyy", "27/10/2000", "12:12:12", "109", "86", "140", "56", "1"), 7000);