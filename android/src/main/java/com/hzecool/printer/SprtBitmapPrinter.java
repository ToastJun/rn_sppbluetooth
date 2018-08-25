package com.hzecool.printer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.hzecool.printer.bean.PrintImageSettingOptionsType;
import com.hzecool.printer.bean.SprtPrinterConstants;


public class SprtBitmapPrinter implements IBitmapPrinter {

    @Override
    public String originalBmpToPrintHexString(Bitmap bitmap, PrintImageSettingOptionsType optionsType) {
        return toHexString(originalBmpToPrintByte(bitmap, SprtPrinterConstants.PAlign.NONE, optionsType.startX.intValue(), 128));
    }

    private byte[] originalBmpToPrintByte(Bitmap bmpOriginal, SprtPrinterConstants.PAlign alignType, int left, int level) {
        if (level < 0 | level > 255 | level == 255) {
            level = 128;
        }

        int picBytes = 0;
        int leftBytes = 0;
        int width = bmpOriginal.getWidth();
        int height = bmpOriginal.getHeight();
        int alignTypeOrdinal = alignType.ordinal();
        if (alignTypeOrdinal == SprtPrinterConstants.PAlign.START.ordinal()) {
            left = 0;
        } else if (alignTypeOrdinal == SprtPrinterConstants.PAlign.CENTER.ordinal()) {
            left = (SprtPrinterConstants.paperWidth - width) / 2;
            if (left % 8 != 0) {
                left = left / 8 * 8;
            }
        } else if (alignTypeOrdinal == SprtPrinterConstants.PAlign.END.ordinal()) {
            left = SprtPrinterConstants.paperWidth - width;
            if (left % 8 != 0) {
                left = left / 8 * 8;
            }
        } else if (alignTypeOrdinal == SprtPrinterConstants.PAlign.NONE.ordinal()) {
            if (left % 8 != 0) {
                left = left / 8 * 8;
            }
        } else {
            if (left % 8 != 0) {
                left = left / 8 * 8;
            }
        }

        int last = width % 8;
        // int picBytes;
        if (last != 0) {
            picBytes = width / 8 + 1;
        } else {
            picBytes = width / 8;
        }

        // int leftBytes;
        if (left % 8 != 0) {
            leftBytes = left / 8 + 1;
        } else {
            leftBytes = left / 8;
        }

        byte[] imgBuf = new byte[(picBytes + leftBytes + 4) * height];
        byte[] perLineBuf = null;
        int[] p = new int[8];
        int s = 0;
        int B = 0;
        int G = 0;
        boolean var16 = false;

        try {
            for(int x = 0; x < height; ++x) {
                perLineBuf = new byte[picBytes];

                int pixel;
                int n;
                int m;
//                int B;
//                int G;
                int R;
                for(n = 0; n < width / 8; ++n) {
                    for(m = 0; m < 8; ++m) {
                        pixel = bmpOriginal.getPixel(n * 8 + m, x);
                        R = Color.red(pixel);
                        G = Color.green(pixel);
                        B = Color.blue(pixel);
                        R = (int)(0.299D * (double)R + 0.587D * (double)G + 0.114D * (double)B);
                        if (R < level) {
                            p[m] = 1;
                        } else {
                            p[m] = 0;
                        }
                    }

                    m = p[0] << 7 | p[1] << 6 | p[2] << 5 | p[3] << 4 | p[4] << 3 | p[5] << 2 | p[6] << 1 | p[7];
                    perLineBuf[n] = (byte)m;
                }

                if (last > 0) {
                    n = 0;

                    for(m = 0; m < last; ++m) {
                        pixel = bmpOriginal.getPixel(width - (last - m), x);
                        R = Color.red(pixel);
                        G = Color.green(pixel);
                        B = Color.blue(pixel);
                        R = (int)(0.299D * (double)R + 0.587D * (double)G + 0.114D * (double)B);
                        if (R < level) {
                            p[m] = 1;
                        } else {
                            p[m] = 0;
                        }

                        n = p[0] << 7 | p[1] << 6 | p[2] << 5 | p[3] << 4 | p[4] << 3 | p[5] << 2 | p[6] << 1 | p[7];
                    }

                    perLineBuf[picBytes - 1] = (byte)n;
                }

                if (x != 0) {
                    ++s;
                    imgBuf[s] = 22;
                } else {
                    imgBuf[s] = 22;
                }

                ++s;
                imgBuf[s] = (byte)(picBytes + leftBytes);

                for(n = 0; n < left / 8; ++n) {
                    ++s;
                    imgBuf[s] = 0;
                }

                for(n = 0; n < picBytes; ++n) {
                    ++s;
                    imgBuf[s] = perLineBuf[n];
                }

                ++s;
                imgBuf[s] = 21;
                ++s;
                imgBuf[s] = 1;
            }
        } catch (Exception var20) {
            Log.e("Utils", var20.toString());
        }

        return imgBuf;
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
