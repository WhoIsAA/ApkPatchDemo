package com.whoisaa.apkpatchdemo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    public static final String SDCARD_PATH = Environment.getExternalStorageDirectory() + File.separator;
    public static final String PATCH_FILE = "old-to-new.patch";
    public static final String NEW_APK_FILE = "new.apk";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((TextView) findViewById(R.id.tv_main)).setText("更新完成！\n当前版本号为 v" + getVersionName(MainActivity.this));

        findViewById(R.id.btn_main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //并行任务
                new ApkUpdateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
    }

    /**
     * 获取当前版本号
     * @param context
     * @return
     */
    private String getVersionName(Context context) {
        String versionName = "";
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 合并增量文件任务
     */
    private class ApkUpdateTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            String oldApkPath = ApkUtils.getCurApkPath(MainActivity.this);
            File oldApkFile = new File(oldApkPath);
            File patchFile = new File(getPatchFilePath());
            if(oldApkFile.exists() && patchFile.exists()) {
                Log("正在合并增量文件...");
                String newApkPath = getNewApkFilePath();
                BsPatchJNI.patch(oldApkPath, newApkPath, getPatchFilePath());
//                //检验文件MD5值
//                return Signtils.checkMd5(oldApkFile, MD5);

                Log("增量文件的MD5值为：" + SignUtils.getMd5ByFile(patchFile));
                Log("新文件的MD5值为：" + SignUtils.getMd5ByFile(new File(newApkPath)));

                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(result) {
                Log("合并成功，开始安装");
                ApkUtils.installApk(MainActivity.this, getNewApkFilePath());
            } else {
                Log("合并失败");
            }
        }
    }

    private String getPatchFilePath() {
        return SDCARD_PATH + PATCH_FILE;
    }

    private String getNewApkFilePath() {
        return SDCARD_PATH + NEW_APK_FILE;
    }

    /**
     * 打印日志
     * @param log
     */
    private void Log(String log) {
        Log.e("MainActivity", log);
    }

}
