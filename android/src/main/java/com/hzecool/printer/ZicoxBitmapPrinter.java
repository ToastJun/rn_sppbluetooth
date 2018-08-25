package com.hzecool.printer;

import android.graphics.Bitmap;

import com.hzecool.printer.bean.PrintImageSettingOptionsType;
import com.hzecool.printer.btlibrary.ZicoxPrinterDataCore;
import com.hzecool.printer.utils.GZIPFrame;

public class ZicoxBitmapPrinter implements IBitmapPrinter {
    public static String LanguageEncode = "gb2312";

    @Override
    public String originalBmpToPrintHexString(Bitmap bitmap, PrintImageSettingOptionsType optionsType) {
        byte[] bytes = drawBitmap(bitmap);
        return toHexString(bytes);
    }

    private byte[] drawBitmap(Bitmap bmp) {
        byte[] zipBitmapData = this.zp_page_print(bmp);
        return zipBitmapData;
    }

    public byte[] zp_page_print(Bitmap bitmap) {
        int myBitmapWidth = bitmap.getWidth();
        int myBitmapHeight = bitmap.getHeight();

        int len = (bitmap.getWidth() + 7) / 8;
        byte[] data = new byte[(len + 4) * myBitmapHeight];
        int ndata = 0;

        int[] RowData = new int[myBitmapWidth * myBitmapHeight];
        bitmap.getPixels(RowData, 0, myBitmapWidth, 0, 0, myBitmapWidth, myBitmapHeight);

        for(int i = 0; i < myBitmapHeight; ++i) {
            data[ndata + 0] = 31;
            data[ndata + 1] = 16;
            data[ndata + 2] = (byte)(len % 256);
            data[ndata + 3] = (byte)(len / 256);

            int j;
            for(j = 0; j < len; ++j) {
                data[ndata + 4 + j] = 0;
            }

            int size;
            for(j = 0; j < myBitmapWidth; ++j) {
                size = RowData[i * myBitmapWidth + j];
                int b = size >> 0 & 15;
                int g = size >> 8 & 15;
                int r = size >> 16 & 15;
                int grey = (r + g + b) / 3;
                if (grey < 12) {
                    data[ndata + 4 + j / 8] |= (byte)(128 >> j % 8);
                }
            }

            for(size = len - 1; size >= 0 && data[ndata + 4 + size] == 0; --size) {
                ;
            }

            ++size;
            data[ndata + 2] = (byte)(len % 256);
            data[ndata + 3] = (byte)(len / 256);
            ndata += 4 + len;
        }

        data = GZIPFrame.codec(data, ndata);
        return data;
    }

    /**
     * 数组转成十六进制字符串
     * @param
     * @return HexString
     */
    private static String toHexString(byte[] b){
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < b.length; ++i){
            buffer.append(toHexString(b[i]));
        }
        return buffer.toString();
    }

    private static String toHexString(byte b){
        String s = Integer.toHexString(b & 0xFF);
        if (s.length() == 1){
            return "0" + s;
        }else{
            return s;
        }
    }
}
