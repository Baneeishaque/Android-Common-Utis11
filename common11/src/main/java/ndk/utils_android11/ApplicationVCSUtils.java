package ndk.utils_android11;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

import ndk.utils_android1.LogUtils;
import ndk.utils_android1.ToastUtils;

import static ndk.utils_android11.InstallApk.installApk;

public class ApplicationVCSUtils {

    public static void downloadAndInstallApk(String applicationName, float versionName, String updateUrl, final Context context) {

        //TODO : Use Permission Utils
        //get destination to update file and set Uri.
        //Download directory in external storage.
        String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
        String fileName = applicationName + "_" + versionName + ".apk";
        destination += fileName;
        final Uri uri = Uri.parse("file://" + destination);

        //Delete update file if exists
        File file = new File(destination);
        if (file.exists()) {
            if (!file.delete()) {

                LogUtils.debug(applicationName,"Deletion failure, please clear your downloads...");
                if (!BuildConfig.DEBUG) {
                    ToastUtils.longToast(context, "Deletion failure, please clear your downloads...");
                }
            }
        }

        //get url of app on server
        //set download manager
        LogUtils.debug(applicationName,"Update URL : " + updateUrl);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(updateUrl));
        request.setDescription("Downloading Update...");
        request.setTitle(applicationName + " " + versionName);

        //set destination
        request.setDestinationUri(uri);

        // get download service and enqueue file
        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = downloadManager.enqueue(request);

        //set BroadcastReceiver to install app when apk file is downloaded
        BroadcastReceiver downloadCompleteBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                installApk(uri, downloadManager, downloadId, context, this);
            }
        };

        //register receiver for when apk file download is compete
        context.registerReceiver(downloadCompleteBroadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}
