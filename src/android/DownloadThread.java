package com.application.plugins.android;

import android.os.Environment;
import android.os.Handler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class DownloadThread implements Runnable {

    static int SUCCESS = 0;
    static int ERROR = 1;

    private String remoteUrl;
    private String target;
    private Handler mHandler;

    public DownloadThread(Handler mHandler, String remoteUrl) {
        this.target = Environment.getExternalStorageDirectory() + "/" + "updates";
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
                File apkFile = new File(mSavePath, ApplicationUpdater.apkFileName);
                FileOutputStream fos = new FileOutputStream(apkFile);
                byte buf[] = new byte[1024];
                int numread = -1;
                while (numread != 0) {
                    numread = is.read(buf);
                    fos.write(buf, 0, numread);
                }
                mHandler.sendEmptyMessage(DownloadThread.SUCCESS);
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