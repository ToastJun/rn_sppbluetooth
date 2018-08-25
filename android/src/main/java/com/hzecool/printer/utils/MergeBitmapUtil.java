package com.hzecool.printer.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.hzecool.printer.bean.PrintImageSettingOptionsType;

import java.math.BigDecimal;
import java.util.List;

public class MergeBitmapUtil {
    private static int PrinterDotWidth = 576;

    /**
     * 图片去色,返回灰度图片
     * @param bmpOriginal 传入的图片
     * @return 去色后的图片
     */
    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
    /**
     * ==========================================   合成图片相关方法   ==========================================  start
     */
    public interface OnBitmapMergeFinishListener{
        void onBitmapMergeFinish(Bitmap bitmap);
    }

    /**
     * 合成图片的异步方法
     * @param sources       需要合成的图片bitmap集合
     * @param optionsType   合成图片的配置参数
     * @param listener      bitmap合成后的回调
     */
    public static void startMergeBitmap(List<PrintImageSettingOptionsType.PrintImageSourceType> sources, final PrintImageSettingOptionsType optionsType, final OnBitmapMergeFinishListener listener) {
        AsyncTask<List<PrintImageSettingOptionsType.PrintImageSourceType>, Void, Bitmap> asyncTask = new AsyncTask<List<PrintImageSettingOptionsType.PrintImageSourceType>, Void, Bitmap>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Bitmap doInBackground(List<PrintImageSettingOptionsType.PrintImageSourceType>... lists) {
                List<PrintImageSettingOptionsType.PrintImageSourceType> list = lists[0];
                Bitmap last = null;
                last = newBitmapByOption(list, optionsType);
                return last;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (listener != null) {
                    listener.onBitmapMergeFinish(bitmap);
                }
            }
        };
        asyncTask.execute(sources);
    }

    /**
     * 根据合成参数 创建新的bitmap
     * @param bitmaps
     * @param optionsType
     * @return
     */
    public static Bitmap newBitmapByOption(List<PrintImageSettingOptionsType.PrintImageSourceType> bitmaps, PrintImageSettingOptionsType optionsType) {
        // 图片下面的文字
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(20);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);
        // 每张图片宽高
        BigDecimal width = optionsType.imageWidth;
        BigDecimal height = optionsType.imageHeight;
        // 绘制区域的高度 包括了文字 以及 文字和图片之间的间隔
        //int areaHeight = optionsType.imageHeight + optionsType.margin + (int)(textPaint.getTextSize());
        BigDecimal areaHeight = optionsType.imageHeight.add(optionsType.margin).add(new BigDecimal(textPaint.getTextSize()));
        BigDecimal margin = optionsType.margin;

        //该方法即为设置基线上那个点究竟是left,center,还是right  这里我设置为center
        textPaint.setTextAlign(Paint.Align.CENTER);
        // 根据所需行列 计算需要的bitmap宽高
        // int resultBitmapWidth = optionsType.column * width + (optionsType.column - 1) * margin;
        BigDecimal resultBitmapWidth = optionsType.column.multiply(width).add(optionsType.column.subtract(new BigDecimal(1)).multiply(margin));

        // int resultBitmapHeight = optionsType.row * areaHeight + (optionsType.row - 1) * margin;
        BigDecimal resultBitmapHeight = optionsType.row.multiply(areaHeight).add(optionsType.row.subtract(new BigDecimal(1)).multiply(margin));

        // 创建一个最终图片大小的空白 bitmap
        if (resultBitmapWidth.intValue() > PrinterDotWidth) {
            resultBitmapWidth = new BigDecimal(PrinterDotWidth);
        }
        Bitmap resultBitmap = Bitmap.createBitmap(resultBitmapWidth.intValue(), resultBitmapHeight.intValue(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        // 画布背景为白色
        //canvas.drawRGB(255,255,255);
        canvas.drawColor(-1);

        // 开始绘制每一个区域中的图片和文字
        for(int i=0; i<bitmaps.size(); i++) {
            int row = i / optionsType.column.intValue();           // i处在row行 0开始
            int column = i - row * optionsType.column.intValue();  // i处在column列 0开始

            // 根据参数 重新设置每个bitmap的大小
            // 根据url和logoUrl判断 bitmap为直接展示的图片还是logo图片
            // 直接展示图片直接绘制新的sizebitmap，如果是logo图片 需要先生成二维码加logo图片
            Bitmap newSizeBitmap = null;
            if (!TextUtils.isEmpty(bitmaps.get(i).url)) {
                newSizeBitmap = getNewSizeBitmap(bitmaps.get(i).bitmap, width.intValue(), height.intValue());
            } else {
                newSizeBitmap = getNewSizeBitmap(syncEncodeQRCode(bitmaps.get(i).qrcodeContent, bitmaps.get(i).bitmap, width.intValue(), height.intValue()),width.intValue(), height.intValue());
            }
            // 绘制图片  偏移的高度 top 为已绘制的区域高度+图片之间的margin
            canvas.drawBitmap(newSizeBitmap, new BigDecimal(column).multiply(width.add(margin)).intValue(), new BigDecimal(row).multiply(areaHeight.add(margin)).intValue(), null);
            // 绘制图片下的文字
            canvas.drawText(bitmaps.get(i).desc==null?"":bitmaps.get(i).desc, width.intValue()/2 + (width.intValue() + margin.intValue()) * column, (areaHeight.intValue() + margin.intValue()) * row + height.intValue() + margin.intValue() + textPaint.getTextSize()/2, textPaint);
        }

        //return drawBitmap(toGrayscale(resultBitmap), optionsType.startX, optionsType.startY, resultBitmap.getWidth(), resultBitmap.getHeight(), 0);
        return toGrayscale(resultBitmap);
    }

    /**
     * 根据宽高 创建新的bitmap
     * @param bitmap
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap getNewSizeBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        if (bitmap == null) {
            return Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        }
        float scaleWidth = ((float) newWidth) / bitmap.getWidth();
        float scaleHeight = ((float) newHeight) / bitmap.getHeight();
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap bit1Scale = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                true);
        return bit1Scale;
    }

    /**
     * 生成二维码bitmap
     * @param qrcontent
     * @param logoBitmap
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap syncEncodeQRCode(String qrcontent, Bitmap logoBitmap, int newWidth, int newHeight) {
        if (null == qrcontent) {
            qrcontent = "";
        }
        Bitmap qrBitmap = null;
        if (logoBitmap != null) {
            // 将logo添加到二维码中
            qrBitmap = QRCodeEncoder.syncEncodeQRCode(qrcontent, newWidth, Color.BLACK, logoBitmap);
        } else {
            // 仅生成二维码
            qrBitmap = QRCodeEncoder.syncEncodeQRCode(qrcontent, newWidth);
        }
        return qrBitmap;
    }



    /**
     * ==========================================   合成图片相关方法   ==========================================  end
     */
}