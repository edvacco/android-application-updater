package com.application.wspresto.plugins;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.support.v4.content.FileProvider;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.File;

public class UpdateController {
    private final String TAG = "UpdateController";

    private CordovaInterface cordova;

    private String mApkDir;
    private String mApkFile;
    private Context mContext;
    private DownloadThread mDownloadThread;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            System.out.println("Audiodio: Got message"); // TESTING!!!
            System.out.println("Audiodio: " + msg.what); // TESTING!!!
            if (msg.what == DownloadThread.SUCCESS) {
                System.out.println("Audiodio: signalling success"); // TESTING!!!
                CallbackContext mCallBackContext = (CallbackContext) msg.obj;
                mCallBackContext.success();
            } else if (msg.what == DownloadThread.ERROR) {
                CallbackContext mCallBackContext = (CallbackContext) msg.obj;                
                mCallBackContext.error("Could not download apk file.");
            }
        }
    };

    public UpdateController(Context mContext, CordovaInterface cordova, String mApkDir, String mApkFile) {
        this.mApkDir = mApkDir;
        this.mApkFile = mApkFile;
        this.cordova = cordova;
        this.mContext = mContext;
    }

    public void onUpdate() {
        this.installApk();
    }

    public void onDownload(String remoteUrl, CallbackContext mCallBackContext) {
        this.mDownloadThread = new DownloadThread(this.mHandler, mCallBackContext, remoteUrl, this.mApkDir, this.mApkFile);
        this.cordova.getThreadPool().execute(this.mDownloadThread);
    }

    private void installApk() {
        LOG.d(TAG, "Installing APK");

        File apkFile = new File(this.mApkDir, this.mApkFile);
        if (!apkFile.exists()) {
            LOG.e(TAG, "Could not find APK: " + apkFile.toString());
            return;
        }
        // com.vaenow.appupdate.android.provider
        LOG.d(TAG, "APK Filename: " + apkFile.toString());
        Uri apkUri = FileProvider.getUriForFile(mContext, "com.application.wspresto.plugins.provider", apkFile);
        Intent i = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        i.setData(apkUri);
        i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        this.mContext.startActivity(i);

    }

    public static JSONObject makeJSON(int code, Object msg) {
        JSONObject json = new JSONObject();

        try {
            json.put("code", code);
            json.put("msg", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json;
    }

}