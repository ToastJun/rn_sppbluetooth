package com.ecool.printer.utils;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.support.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadImgIntentService extends IntentService {
    public static final String DOWNLOAD_URL="download_url";
    public static final String INDEX_FLAG="index_flag";

    private static DownloadFinishListener downloadFinishListener;

    public DownloadImgIntentService() {
        super("DownloadImgIntentService");
    }

    public DownloadImgIntentService(String name) {
        super(name);
    }

    public static void setDownloadFinishListener(DownloadFinishListener downloadFinishListener) {
        DownloadImgIntentService.downloadFinishListener = downloadFinishListener;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //在子线程中进行网络请求
        Bitmap bitmap=downloadUrlBitmap(intent.getStringExtra(DOWNLOAD_URL));
        Message msg1 = Message.obtain();
        msg1.what = intent.getIntExtra(INDEX_FLAG,0);
        msg1.obj =bitmap;
        //通知主线程去更新UI
        if(downloadFinishListener!=null){
            downloadFinishListener.onDownloadFinish(msg1);
        }
    }

    public interface DownloadFinishListener{
        void onDownloadFinish(Message message);
    }

    private Bitmap downloadUrlBitmap(String urlString) {

        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        Bitmap bitmap=null;
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), 8 * 1024);
            bitmap= BitmapFactory.decodeStream(in);
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

}
