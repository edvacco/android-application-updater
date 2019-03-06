package com.application.plugins.android;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import org.apache.cordova.LOG;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.BuildHelper;

import java.io.File;


public class ApplicationUpdater extends CordovaPlugin {

    static String apkFileName = "audiodio.apk";
    private String remoteUrl;
    //////////
    // Permissions
    //////////

    private static final int INSTALL_PERMISSION_REQUEST_CODE = 0;
    private static final int UNKNOWN_SOURCES_PERMISSION_REQUEST_CODE = 1;
    private static final int OTHER_PERMISSIONS_REQUEST_CODE = 2;

    private static String[] OTHER_PERMISSIONS = { Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };

    private Context mContext;
    private UpdateController mController;
    private CordovaInterface cordova;
    private CallbackContext callbackContext;

    public ApplicationUpdater(Context mContext, CordovaInterface cordova) {
        this.mContext = mContext;
        this.cordova = cordova;
        this.mController = new UpdateController();
        this.remoteUrl = "";
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.remoteUrl = args.getString(0);
        System.out.println(this.remoteUrl); // TESTING!!!
        if (action.equals("update")) {
            if (verifyInstallPermission() && verifyOtherPermissions()) {
                this.mController.onUpdate(this.remoteUrl);
                callbackContext.success(null);
                // TODO: callback success when controller has downloaded the apk file
            }
            return true;
        }
        callbackContext.error(Utils.makeJSON(Constants.NO_SUCH_METHOD, "No such method: " + action));
        return false;
    }

    // Prompt user for install permission if we don't already have it.
    public boolean verifyInstallPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!cordova.getActivity().getPackageManager().canRequestPackageInstalls()) {
                String applicationId = (String) BuildHelper.getBuildConfigValue(cordova.getActivity(),
                        "APPLICATION_ID");
                Uri packageUri = Uri.parse("package:" + applicationId);
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).setData(packageUri);
                cordova.setActivityResultCallback(this);
                cordova.getActivity().startActivityForResult(intent, INSTALL_PERMISSION_REQUEST_CODE);
                return false;
            }
        } else {
            try {
                if (Settings.Secure.getInt(cordova.getActivity().getContentResolver(),
                        Settings.Secure.INSTALL_NON_MARKET_APPS) != 1) {
                    Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                    cordova.setActivityResultCallback(this);
                    cordova.getActivity().startActivityForResult(intent, UNKNOWN_SOURCES_PERMISSION_REQUEST_CODE);
                    return false;
                }
            } catch (Settings.SettingNotFoundException e) {
            }
        }

        return true;
    }

    // Prompt user for all other permissions if we don't already have them all.
    public boolean verifyOtherPermissions() {
        boolean hasOtherPermissions = true;
        for (String permission : OTHER_PERMISSIONS)
            hasOtherPermissions = hasOtherPermissions && cordova.hasPermission(permission);

        if (!hasOtherPermissions) {
            cordova.requestPermissions(this, OTHER_PERMISSIONS_REQUEST_CODE, OTHER_PERMISSIONS);
            return false;
        }

        return true;
    }

    // React to user's response to our request for install permission.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INSTALL_PERMISSION_REQUEST_CODE) {
            if (!cordova.getActivity().getPackageManager().canRequestPackageInstalls()) {
                // getUpdateManager().permissionDenied("Permission Denied: " +
                // Manifest.permission.REQUEST_INSTALL_PACKAGES);
                return;
            }
            if (verifyOtherPermissions()) {
                this.mController.onUpdate(this.remoteUrl);
            }
        } else if (requestCode == UNKNOWN_SOURCES_PERMISSION_REQUEST_CODE) {
            try {
                if (Settings.Secure.getInt(cordova.getActivity().getContentResolver(),
                        Settings.Secure.INSTALL_NON_MARKET_APPS) != 1) {
                    // TODO: signal Permission denied
                    return;
                }
            } catch (Settings.SettingNotFoundException e) {
            }
            if (verifyOtherPermissions()) {
                this.mController.onUpdate(this.remoteUrl);
            }
        }
    }

    // React to user's response to our request for other permissions.
    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == OTHER_PERMISSIONS_REQUEST_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // getUpdateManager().permissionDenied("Permission Denied: " + permissions[i]);
                    return;
                }
            }
            this.mController.onUpdate(this.remoteUrl);
        }
    }
}