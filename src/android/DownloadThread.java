package com.application.plugins.android;

import android.AuthenticationOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import 	java.nio.charset.StandardCharsets;

public class DownloadThread implements Runnable {

    static SUCCESS = 0;
    static ERROR = 1;

    private String name;
    private String remoteUrl;
    private String target;
    private Handler mHandler;    
    public DownloadThread(Handler mHandler, String name, String remoteUrl) {
        this.target = Environment.getExternalStorageDirectory() + "/" + "updates";
        this.name = name;
        this.remoteUrl = remoteUrl;
        this.mHandler = mHandler;
    }

    @Override
    public void run() {
        this.download();
    }

     private void download() {
        try {

            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                URL url = new URL(remoteUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();
                File file = new File(mSavePath);
                if (!file.exists()) {
                    file.mkdir();
                }
                File apkFile = new File(mSavePath, this.name+".apk");
                FileOutputStream fos = new FileOutputStream(apkFile);
                int count = 0;
                byte buf[] = new byte[1024];
                do {
                    int numread = is.read(buf);
                    count += numread;
                    progress = (int) (((float) count / length) * 100);
                    if (numread <= 0) {
                        mHandler.sendEmptyMessage(DownloadThread.SUCCESS);
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (!cancelUpdate);
                fos.close();
                is.close();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(DownloadThread.ERROR);
        } catch (IOException e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(DownloadThread.ERROR);
        }

    }

}