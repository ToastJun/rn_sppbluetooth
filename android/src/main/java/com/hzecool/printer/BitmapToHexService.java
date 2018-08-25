package com.hzecool.printer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.hzecool.printer.bean.PrintImageSettingOptionsType;
import com.hzecool.printer.bean.PrinterType;
import com.hzecool.printer.utils.DownloadImgIntentService;
import com.hzecool.printer.utils.MergeBitmapUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片转换成可被打印机识别的hex十六进制字符串
 */
public class BitmapToHexService {
    public static final int PRINTER_TYPE_NONE = -1;

    private static BitmapToHexService mService;

    // 打印机的类型
    private int mPrintType = PRINTER_TYPE_NONE;

    private IBitmapPrinter mBitmapPrinter;

    public interface  OnGetHexStringListener{
        void onGetHexStringListener(String hex);

        void onGetHexStringFailed(String message);
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
     */
    public void executeWork(Context context, List<PrintImageSettingOptionsType.PrintImageSourceType> sources, PrintImageSettingOptionsType optionsType, final Promise promise) {
        if (mPrintType == PRINTER_TYPE_NONE) {
            throw new NullPointerException("u should set printer type first");
        }
        downloadImg(context, sources, optionsType, new OnGetHexStringListener() {
            @Override
            public void onGetHexStringListener(String hex) {
                if (TextUtils.isEmpty(hex)){
                    promise.reject("-1",hex);
                }else {
                    promise.resolve(hex);
                }
            }

            @Override
            public void onGetHexStringFailed(String message) {
                promise.reject("-1", message);
            }
        });
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

        final ArrayList<PrintImageSettingOptionsType.PrintImageSourceType> resultSources = new ArrayList<>();
        for (PrintImageSettingOptionsType.PrintImageSourceType item : sources) {
            if (!TextUtils.isEmpty(item.url) || !TextUtils.isEmpty(item.qrcodeContent)) {
                resultSources.add(item);
            }
        }

        if (resultSources.isEmpty()) {
            onGetHexStringListener.onGetHexStringFailed("没有可以用于合成的图片");
            return;
        }

        Intent intent = new Intent(context, DownloadImgIntentService.class);
        // 图片下载完成后的监听回调
        DownloadImgIntentService.setDownloadFinishListener(new DownloadImgIntentService.DownloadFinishListener() {
            @Override
            public void onDownloadFinish(Message message) {
                resultSources.get(message.what).bitmap = (Bitmap) message.obj;
                // 全部下载完成后
                if (message.what == resultSources.size() - 1) {
                    // 开始合成图片
                    MergeBitmapUtil.startMergeBitmap(resultSources, optionsType, new MergeBitmapUtil.OnBitmapMergeFinishListener() {
                        @Override
                        public void onBitmapMergeFinish(Bitmap bitmap) {
                            if (bitmap == null) {
                                onGetHexStringListener.onGetHexStringFailed("合成图片失败,bitmap为null");
                            } else {
                                // 根据不同的打印机类型 对bitmap进行转换
                                bitmap2HexString(mPrintType, bitmap, optionsType, onGetHexStringListener);
                            }
                        }
                    });
                }
            }
        });
        // 开始下载网络图片
        for(int i=0; i<resultSources.size(); i++) {
            PrintImageSettingOptionsType.PrintImageSourceType printImageSourceType = resultSources.get(i);
            if (!TextUtils.isEmpty(printImageSourceType.url)) {
                intent.putExtra(DownloadImgIntentService.DOWNLOAD_URL, printImageSourceType.url);
            } else if (!TextUtils.isEmpty(printImageSourceType.logoUrl)) {
                intent.putExtra(DownloadImgIntentService.DOWNLOAD_URL, printImageSourceType.logoUrl);
            } else {
                intent.putExtra(DownloadImgIntentService.DOWNLOAD_URL, "");
            }
            intent.putExtra(DownloadImgIntentService.INDEX_FLAG, i);
            context.startService(intent);
        }
    }

    private void bitmap2HexString(int printType, final Bitmap bitmap, final PrintImageSettingOptionsType optionsType, final OnGetHexStringListener onGetHexStringListener) {
        if (printType == PrinterType.PRINTER_ZK_80 || printType == PrinterType.PRINTER_ZK_110) {
            mBitmapPrinter = new ZicoxBitmapPrinter();
        } else if (printType == PrinterType.PRINTER_SPRT_587 || printType == PrinterType.PRINTER_SPRT_887) {
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

}
