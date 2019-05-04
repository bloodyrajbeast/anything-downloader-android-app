package com.gaurav.tomar.downloader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    Button Download;
    EditText url;
    private static final int My_permission = 1;
    ProgressDialog progressDialog;
    double file_size = 0;
    String file_name ;
    String urlname;
    //ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Download = findViewById(R.id.Download);
        url = findViewById(R.id.url);

        Download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                urlname = url.getText().toString();

                if (urlname.isEmpty())
                    Toast.makeText(MainActivity.this, "Enter the Url first ", Toast.LENGTH_SHORT).show();
                else {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                            PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, My_permission);
                    } else {
                        //start download
                        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                                "/Mydownloadedfiles/");
                        try {
                            dir.mkdir();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Folder not created....", Toast.LENGTH_SHORT).show();
                        }

                        // file to be download....
                        new Downloadtask().execute(urlname);

                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case My_permission:
            {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                            "/Mydownloadedfiles/");
                    try{
                        dir.mkdir();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Folder not created....", Toast.LENGTH_SHORT).show();
                    }

                    urlname = url.getText().toString();
                    // file to be download....
                    new Downloadtask().execute(urlname);
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    private class Downloadtask extends AsyncTask<String, Integer, String>
    {

        @Override
        protected String doInBackground(String... strings) {
            file_name = strings[0].substring(strings[0].lastIndexOf("/") +1) ;
            try{
                InputStream input = null;
                OutputStream output = null;
                HttpURLConnection connection = null;
                try{
                    URL url = new URL(strings[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                        return "server returned HTTP" + connection.getResponseCode() + " " + connection.getResponseMessage();
                    }

                    int filelength = connection.getContentLength();
                    file_size = filelength;
                    input = connection.getInputStream();
                    output = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/Mydownloadedfiles/"+ file_name);

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    while((count= input.read(data)) != -1)
                    {
                        if(isCancelled()){
                            return null;
                        }
                        total += count;
                        if (filelength > 0)
                        {
                            publishProgress((int) (total * 100 / filelength));
                        }
                        output.write(data,0,count);
                    }
                }catch (Exception e)
                {
                    return e.toString();
                }finally {
                    try{
                        if(output != null)
                        {
                            output.close();
                        }
                        if(input != null)
                        {
                            input.close();
                        }
                    }catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    if (connection != null)
                    {
                        connection.disconnect();
                    }
                }

            } finally {

            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("downloading...");
            progressDialog.setMessage("File Size = "+file_size);
            progressDialog.setIndeterminate(true);
            progressDialog.setProgress(ProgressDialog.STYLE_HORIZONTAL);

            progressDialog.setCancelable(false);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Toast.makeText(MainActivity.this, "Download Cancelled...", Toast.LENGTH_SHORT).show();

                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                            "/Mydownloadedfiles/"+ file_name);
                    try{
                        dir.delete();
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            progressDialog.show();

           // progressBar = new ProgressBar(MainActivity.this);
            //progressBar.setProgress(ProgressBar.SCROLL_AXIS_HORIZONTAL);

            //progressBar.isShown();
        }                                        //progress dialog

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgress(values[0]);
            progressDialog.setMessage("File size: " + new DecimalFormat("##.##").format(file_size / 1000000) + "MB");

           // progressBar.setProgress(values[0]);
           // progressBar.setMax(100);
           // progressBar.getProgress();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            //progressBar.getDisplay();
            if(result != null){
                Toast.makeText(MainActivity.this, "Error: " + result, Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(MainActivity.this,"Downloaded...", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
