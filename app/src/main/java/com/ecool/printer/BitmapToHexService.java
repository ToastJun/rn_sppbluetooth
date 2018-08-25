package com.ecool.printer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Message;
import android.util.Log;

import com.ecool.printer.bean.PrintImageSettingOptionsType;
import com.ecool.printer.utils.DownloadImgIntentService;
import com.ecool.printer.utils.MergeBitmapUtil;

import java.util.List;

/**
 * 图片转换成可被打印机识别的hex十六进制字符串
 */
public class BitmapToHexService {
    public static final int PRINTER_TYPE_NONE = -1;
    public static final int PRINTER_TYPE_ZICOX = 1;
    public static final int PRINTER_TYPE_SPRT = 2;

    private static BitmapToHexService mService;

    // 打印机的类型  默认为芝柯
    private int mPrintType = PRINTER_TYPE_ZICOX;

    private IBitmapPrinter mBitmapPrinter;

    public interface  OnGetHexStringListener{
        void onGetHexStringListener(String hex);
    }

    public static BitmapToHexService getInstance() {
        if (mService == null) {
            mService = new BitmapToHexService();
        }
        return mService;
    }

    /**
     * 设置打印机的类型  未设置默认为 芝柯
     * @param printType
     * @return
     */
    public BitmapToHexService setPrinterType(int printType) {
        mPrintType = printType;
        return this;
    }

    /**
     * 执行  将网络图片按照参数合成一张图,并且转换成可被打印机识别的hex字符串  的任务
     * @param context   Application 用于启动执行下载任务IntentService
     * @param sources   图片源
     * @param optionsType   合成图片的参数
     * @param onGetHexStringListener    获取到hex字符串的监听回调
     */
    public void executeWork(Context context, List<PrintImageSettingOptionsType.PrintImageSourceType> sources, PrintImageSettingOptionsType optionsType, OnGetHexStringListener onGetHexStringListener) {
        if (mPrintType == PRINTER_TYPE_NONE) {
            throw new NullPointerException("u should set printer type first");
        }
        downloadImg(context, sources, optionsType, onGetHexStringListener);
    }

    /**
     * 下载网络图片
     * @param context
     * @param sources
     * @param optionsType
     * @param onGetHexStringListener
     */
    private void downloadImg(final Context context, final List<PrintImageSettingOptionsType.PrintImageSourceType> sources, final PrintImageSettingOptionsType optionsType, final OnGetHexStringListener onGetHexStringListener) {
        if (null == sources || sources.isEmpty()) {
            return;
        }

        Intent intent = new Intent(context, DownloadImgIntentService.class);
        // 图片下载完成后的监听回调
        DownloadImgIntentService.setDownloadFinishListener(new DownloadImgIntentService.DownloadFinishListener() {
            @Override
            public void onDownloadFinish(Message message) {
                sources.get(message.what).bitmap = (Bitmap) message.obj;
                Log.e("下载图片", message.what + "");
                // 全部下载完成后
                if (message.what == sources.size() - 1) {
                    // 开始合成图片
                    MergeBitmapUtil.startMergeBitmap(sources, optionsType, new MergeBitmapUtil.OnBitmapMergeFinishListener() {
                        @Override
                        public void onBitmapMergeFinish(Bitmap bitmap) {
                            Log.e("合成完毕", String.valueOf(bitmap!=null));
                            // 根据不同的打印机类型 对bitmap进行转换
                            bitmap2HexString(mPrintType, bitmap, optionsType, onGetHexStringListener);
                        }
                    });
                }
            }
        });
        // 开始下载网络图片
        for(int i=0; i<sources.size(); i++) {
            intent.putExtra(DownloadImgIntentService.DOWNLOAD_URL, sources.get(i).url);
            intent.putExtra(DownloadImgIntentService.INDEX_FLAG, i);
            context.startService(intent);
        }
    }

    private void bitmap2HexString(int printType, final Bitmap bitmap, final PrintImageSettingOptionsType optionsType, final OnGetHexStringListener onGetHexStringListener) {
        if (printType == PRINTER_TYPE_ZICOX) {
            mBitmapPrinter = new ZicoxBitmapPrinter();
        } else if (printType == PRINTER_TYPE_SPRT) {
            mBitmapPrinter = new SprtBitmapPrinter();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String hexString = mBitmapPrinter.originalBmpToPrintHexString(bitmap, optionsType);
                if (onGetHexStringListener != null) {
                    onGetHexStringListener.onGetHexStringListener(hexString);
                }
            }
        }).start();
    }

    private void printTest() {

    }
}
