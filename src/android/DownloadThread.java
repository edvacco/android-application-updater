package com.application.wspresto.plugins;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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

    private String mRemoteUrl;
    private String mApkDir;
    private String mApkFile;
    private Handler mHandler;
    private Object mHandlerPayload;

    public DownloadThread(Handler mHandler, Object mHandlerPayload, String mRemoteUrl, String mApkDir, String mApkFile) {
        this.mApkDir = mApkDir;
        this.mApkFile = mApkFile;
        this.mRemoteUrl = mRemoteUrl;
        this.mHandler = mHandler;
        this.mHandlerPayload = mHandlerPayload;
    }

    @Override
    public void run() {
        this.download();
    }

    private void download() {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                URL url = new URL(this.mRemoteUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();
                File dir = new File(mApkDir);
                if (!dir.exists()) {
                    dir.mkdir();
                }

                File file = new File(mApkDir, mApkFile);
                // if (file.exists()) {
                //     file.delete();
                // }
                FileOutputStream fos = new FileOutputStream(file);
                byte buf[] = new byte[1024];
                int numread = 1;
                while (numread > 0) {                    
                    numread = is.read(buf);
                    if (numread > 0) {
                        fos.write(buf, 0, numread);
                    }
                }
                fos.flush();
                fos.close();
                is.close();
                System.out.println("Audiodio: " + file.getAbsolutePath()); // TESTING!!!
                Message done = new Message();
                done.what = DownloadThread.SUCCESS;
                done.obj = this.mHandlerPayload;
                mHandler.sendMessage(done);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Message error = new Message();
            error.what = DownloadThread.ERROR;
            error.obj = this.mHandlerPayload;            
            mHandler.sendMessage(error);
        } catch (IOException e) {
            e.printStackTrace();
            Message error = new Message();
            error.what = DownloadThread.ERROR;
            error.obj = this.mHandlerPayload;            
            mHandler.sendMessage(error);
        }
    }
}