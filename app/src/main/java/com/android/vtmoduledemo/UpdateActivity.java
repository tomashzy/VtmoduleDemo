package com.android.vtmoduledemo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateActivity  extends AppCompatActivity{
    private static final String TAG = UpdateActivity.class.getSimpleName();
    private static final int PROGRESS = 100;//进度
    private static final int DOWNLOAD_COMPLETE = 200;//下载完成
    private static final int INSTALL_PERMISS_CODE = 500;//安装权限
    private static final int INSTALL_COMPLETE = 600;
    private Button btnDownInstall;//下载安装
    //private static String appDownloadUrl = "https://pendobucket.us-east-1.linodeobjects.com/Chessnut_1.1.15.apk";//下载文件的地址
    private static String appDownloadUrl = "https://raw.githubusercontent.com/tomashzy/VtmoduleDemo/master/app/apk/vtmoduleDemo.apk";
    private ProgressBar mProgressBar;
    private ProgressBar hProgressBar;
    private CirclePgBar mCirclePgBar;
    private TextView mTvStatus;
    private TextView mTvProgress;
    private Context mContext;
    File file;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {

        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case DOWNLOAD_COMPLETE:
                    mTvStatus.setText("下载完成,准备安装！");
                    mProgressBar.setProgress(0);
                    hProgressBar.setProgress(0);
                    mTvProgress.setText("");
                    mCirclePgBar.reset();
                    mProgressBar.setVisibility(View.GONE);
                    hProgressBar.setVisibility(View.GONE);
                    mCirclePgBar.setVisibility(View.GONE);
                    installApk();
                    break;
                case PROGRESS:
                    int result = (int) msg.obj;
                    mProgressBar.setProgress(result);
                    hProgressBar.setProgress(result);
                    mCirclePgBar.setProgress(result);
                    mTvProgress.setText("" + result + "%");
                    break;
                case INSTALL_COMPLETE:
                    mTvStatus.setText("安装完成！");
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        initParamsAndValues();
        initView();
    }

    private void initParamsAndValues() {
        mContext = this;
    }

    private void initView() {
        mTvStatus = (TextView)findViewById(R.id.textViewStatus);
        mTvProgress = (TextView)findViewById(R.id.textViewProgress);
        btnDownInstall = (Button) findViewById(R.id.btn_down_install);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        hProgressBar = (ProgressBar)findViewById(R.id.hProgressBar);
        mCirclePgBar = (CirclePgBar)findViewById(R.id.circleProgressBar);

        mProgressBar.setVisibility(View.GONE);
        hProgressBar.setVisibility(View.GONE);
        mCirclePgBar.setVisibility(View.GONE);

        btnDownInstall.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mTvStatus.setText("正在下载...");
                mProgressBar.setVisibility(View.VISIBLE);
                hProgressBar.setVisibility(View.VISIBLE);
                mCirclePgBar.setVisibility(View.VISIBLE);
                downFile(appDownloadUrl);
            }
        });
    }

    // 接收到安装完成apk的广播
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Toast.makeText(context, "安装完成！", Toast.LENGTH_SHORT).show();
            Message message = handler.obtainMessage();
            message.what = INSTALL_COMPLETE;
            handler.sendMessage(message);
        }
    };

    /**
     * 后台在下面一个Apk 下载完成后返回下载好的文件
     *
     * @param httpUrl
     * @return
     */
    private File downFile(final String httpUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(httpUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    FileOutputStream fileOutputStream = null;
                    InputStream inputStream;
                    int responseCode = connection.getResponseCode();
                    System.out.println("responseCode =" +  responseCode);
                    if (responseCode == 200) {
                        inputStream = connection.getInputStream();
                        if (inputStream != null) {
                            final int appLength = connection.getContentLength();
                            file = getFile(httpUrl);
                            fileOutputStream = new FileOutputStream(file);
                            byte[] buffer = new byte[1024];
                            int length = 0;
                            int total = 0;
                            while ((length = inputStream.read(buffer)) != -1) {
                                //写入文件中
                                fileOutputStream.write(buffer, 0, length);
                                //统计进度
                                total += length;
                                int result = (int)(total * 1.0 / appLength * 100);
                                Message message = handler.obtainMessage();
                                message.what = PROGRESS;
                                message.obj = result;
                                handler.sendMessage(message);
                            }
                            fileOutputStream.close();
                            fileOutputStream.flush();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                    //下载完成,开始安装
                    Message message = handler.obtainMessage();
                    message.what = DOWNLOAD_COMPLETE;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return file;
    }

    /**
     * 安装APK
     */
    private void installApk() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (file != null && file.exists()){
            // 兼容7.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".fileProvider", file);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                //兼容8.0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    boolean hasInstallPermission = mContext.getPackageManager().canRequestPackageInstalls();
                    if (!hasInstallPermission) {
                        startInstallPermissionSettingActivity();
                        return;
                    }
                }
            } else {
                // <7.0
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            // activity任务栈中Activity的个数>0
            if (mContext.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
                mContext.startActivity(intent);
            }
        }
    }

    private void startInstallPermissionSettingActivity() {
        //注意这个是8.0新API
        Uri packageURI = Uri.parse("package:"+ mContext.getPackageName());
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,packageURI);
        startActivityForResult(intent, INSTALL_PERMISS_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 授权完成
        if (resultCode == RESULT_OK && requestCode == INSTALL_PERMISS_CODE) {
            Toast.makeText(this,"安装应用",Toast.LENGTH_SHORT).show();
            installApk();
        } else {
            Toast.makeText(this,"授权失败，无法安装应用",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("package");
        // 注册一个广播
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解除广播
        unregisterReceiver(broadcastReceiver);
    }

    /**
     * 根据传过来url创建文件
     */
    private File getFile(String url) {
        // 使用缓存目录,这个时候不需要申请存储权限
        // 目录不存在，那么创建
        File dir = new File(getExternalCacheDir(),"download");
        if (!dir.exists()){
            dir.mkdir();
        }
        // 创建文件
        File file = new File(dir,getFilePath(url));
        return file;
    }

    /**
     * 截取出url后面的apk的文件名    
     * @param url    
     * @return
     */
    private String getFilePath(String url) {
        return url.substring(url.lastIndexOf("/"), url.length());
    }
}