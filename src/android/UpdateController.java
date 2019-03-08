package com.application.plugins.android;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.support.v4.content.FileProvider;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.BuildHelper;
import org.apache.cordova.LOG;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.File;

public class UpdateController {
    private final String TAG = "UpdateController";

    private CordovaInterface cordova;
    private String mApkPath;
    private Context mContext;
    private DownloadThread mDownloadThread;
        
    public UpdateController(Context mContext, CordovaInterface cordova, String mApkPath) {
        this.mApkPath = mApkPath;
        this.cordova = cordova;    
        this.mContext = mContext;
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (msg.what == DownloadThread.SUCCESS) {
                installApk();
            } else if (msg.what == DownloadThread.ERROR) {
                //callbackContext.error(makeJSON(Constants.UNKNOWN_ERROR, "unknown error"));
                //TODO: callback with error?
            }
        }
    };

    public void onUpdate(String remoteUrl) {
        this.mDownloadThread = new DownloadThread(mHandler, remoteUrl);
        this.cordova.getThreadPool().execute(this.mDownloadThread);         
    }

    private void installApk() {
        LOG.d(TAG, "Installing APK");

        File apkFile = new File(this.mApkPath);
        if (!apkFile.exists()) {
            LOG.e(TAG, "Could not find APK: " + this.mApkPath);
            return;
        }

        LOG.d(TAG, "APK Filename: " + apkFile.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LOG.d(TAG, "Build SDK Greater than or equal to Nougat");
            String applicationId = (String) BuildHelper.getBuildConfigValue((Activity) mContext, "APPLICATION_ID");
            Uri apkUri = FileProvider.getUriForFile(mContext, applicationId + ".appupdate.provider", apkFile);
            Intent i = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            i.setData(apkUri);
            i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.mContext.startActivity(i);
        } else {
            LOG.d(TAG, "Build SDK less than Nougat");
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
            this.mContext.startActivity(i);
        }
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