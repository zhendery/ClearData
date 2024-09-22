package com.zhendery.cleardata;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Handler.Callback {

    /// 获取手机内部存储空间
    public static long GetInternalMemorySize() {
        File file = Environment.getDataDirectory();
        StatFs statFs = new StatFs(file.getPath());
        long blockSizeLong = statFs.getBlockSizeLong();
        long blockCountLong = statFs.getFreeBlocksLong();
        long size = blockCountLong * blockSizeLong;
        return size;
    }

    public static void deleteDirWihtFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory()){
                deleteDirWihtFile(file); // 递规的方式删除文件夹
                file.delete();// 删除目录本身
            }
        }
    }

    Button button ;
    TextView text ;
    ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }

        button = findViewById(R.id.button);
        bar = findViewById(R.id.progressBar);

        text = findViewById(R.id.text);
        text.setText("总容量：" + GetInternalMemorySize());

        button.setOnClickListener(this);
    }

    final int per = 204800;

    long total= 0, size = 1;
    byte[] datas = new byte[per];

    int step = 1;

    @Override
    public void onClick (View v){
        button.setEnabled(false);

        final Handler handler = new Handler(this);

        try {
            File file = new File("/sdcard");
            deleteDirWihtFile(file);

            file = new File("/sdcard/clear");
            file.createNewFile();

        } catch (Exception e) {
            e.printStackTrace();
        }

        size = GetInternalMemorySize();

        total = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                step = 1;
                Message msg;
                msg = new Message();
                msg.what = 2;
                handler.sendMessage(msg);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream("/sdcard/clear");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                final FileOutputStream finalFos = fos;

                for(int c =0;c<per;++c)
                    datas[c] = 0;

                while ((total+1) * per <= size) {
                    try {
                        finalFos.write(datas);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    total++;
                    msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                }

                try {
                    finalFos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                total = 0;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        step = 2;
                        Message msg;
                        msg = new Message();
                        msg.what = 3;
                        handler.sendMessage(msg);
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream("/sdcard/clear");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        final FileOutputStream finalFos = fos;

                        for(int c =0;c<per;++c)
                            datas[c] = 1;

                        while ((total+1) * per <= size) {
                            try {
                                finalFos.write(datas);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            total++;
                            msg = new Message();
                            msg.what = 1;
                            handler.sendMessage(msg);
                        }

                        try {
                            finalFos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        total = 0;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                step = 3;
                                Message msg;
                                msg = new Message();
                                msg.what = 4;
                                handler.sendMessage(msg);
                                FileOutputStream fos = null;
                                try {
                                    fos = new FileOutputStream("/sdcard/clear");
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                final FileOutputStream finalFos = fos;

                                final Random RANDOM = new Random();

                                while ((total+1) * per <= size) {
                                    RANDOM.nextBytes(datas);

                                    try {
                                        finalFos.write(datas);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    total++;
                                    msg = new Message();
                                    msg.what = 1;
                                    handler.sendMessage(msg);
                                }

                                try {
                                    finalFos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    File file = new File("/sdcard");
                                    deleteDirWihtFile(file);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                msg = new Message();
                                msg.what = 5;
                                handler.sendMessage(msg);
                            }
                        }).start();
                    }
                }).start();
            }
        }).start();

    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what){
            case 1:
                int p = (int)(100 * total * per / size);
                p = p * step / 3;
                bar.setProgress(p);
                button.setText(p + "%");
                break;
            case 2:
                text.setText("第一步：全0填充...");
                break;
            case 3:
                text.setText("第二步：全1填充...");
                break;
            case 4:
                text.setText("第三步：随机填充...");
                break;
            case 5:
                text.setText("擦除完成，可以删掉本软件啦");
                break;
        }

        return false;
    }

}
