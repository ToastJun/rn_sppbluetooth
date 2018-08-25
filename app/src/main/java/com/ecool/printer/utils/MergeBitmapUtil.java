package com.ecool.printer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;

import com.ecool.printer.bean.PrintImageSettingOptionsType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class MergeBitmapUtil {
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
        // 每张图片宽高
        int width = optionsType.imageWidth;
        int height = optionsType.imageHeight;
        // 绘制区域的高度 包括了文字 以及 文字和图片之间的间隔
        int areaHeight = optionsType.imageHeight + optionsType.margin + (int)(textPaint.getTextSize());
        int margin = optionsType.margin;

        //该方法即为设置基线上那个点究竟是left,center,还是right  这里我设置为center
        textPaint.setTextAlign(Paint.Align.CENTER);
        // 根据所需行列 计算需要的bitmap宽高
        int resultBitmapWidth = optionsType.column * width + (optionsType.column - 1) * margin;
        int resultBitmapHeight = optionsType.row * areaHeight + (optionsType.row - 1) * margin;
        // 创建一个最终图片大小的空白 bitmap
        Bitmap resultBitmap = Bitmap.createBitmap(resultBitmapWidth, resultBitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);
        // 画布背景为白色
        canvas.drawRGB(255,255,255);

        // 开始绘制每一个区域中的图片和文字
        for(int i=0; i<bitmaps.size(); i++) {
            int row = i / optionsType.column;           // i处在row行 0开始
            int column = i - row * optionsType.column;  // i处在column列 0开始
            // 根据参数 重新设置每个bitmap的大小
            Bitmap newSizeBitmap = getNewSizeBitmap(comp(bitmaps.get(i).bitmap), width, height);
            // 绘制图片  偏移的高度 top 为已绘制的区域高度+图片之间的margin
            canvas.drawBitmap(newSizeBitmap, column*(width+margin), row *(areaHeight+margin), null);
            // 绘制图片下的文字
            canvas.drawText(bitmaps.get(i).desc, width/2 + (width + margin) * column, (areaHeight + margin) * row + height + margin + textPaint.getTextSize()/2, textPaint);
        }
        return resultBitmap;
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

    //图片按比例大小压缩方法（根据Bitmap图片压缩）
    public static Bitmap comp(Bitmap image) {
        if (null == image) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (baos.toByteArray().length / 1024 > 1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }

    //一、质量压缩法
    public static Bitmap compressImage(Bitmap image) {


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100 && options > 50) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }
    /**
     * ==========================================   合成图片相关方法   ==========================================  end
     */

    public static class HexTaskBean{
        public String x;
        public String y;
        public Bitmap bitmap;

        public HexTaskBean(String x, String y, Bitmap bitmap) {
            this.x = x;
            this.y = y;
            this.bitmap = bitmap;
        }
    }
}
