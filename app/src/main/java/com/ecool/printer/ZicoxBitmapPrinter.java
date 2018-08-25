package com.ecool.printer;

import android.graphics.Bitmap;

import com.ecool.printer.bean.PrintImageSettingOptionsType;
import com.ecool.printer.btlibrary.ZicoxPrinterDataCore;

public class ZicoxBitmapPrinter implements IBitmapPrinter {
    public static String LanguageEncode = "gb2312";

    @Override
    public String originalBmpToPrintHexString(Bitmap bitmap, PrintImageSettingOptionsType optionsType) {
        try {
            return getBitmapPrintHexString(String.valueOf(optionsType.startX),
                    String.valueOf(optionsType.startY), bitmap, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取打印图片的十六进制字符串
     * @param x
     * @param y
     * @param bmap
     * @param type
     * @return
     * @throws Exception
     */
    private static String getBitmapPrintHexString(String x, String y, Bitmap bmap, int type) throws Exception {
        String hexString = "";
        int var4;
        if (bmap.getWidth() % 8 == 0) {
            var4 = bmap.getWidth() / 8;
        } else {
            var4 = bmap.getWidth() / 8 + 1;
        }

        int var5 = bmap.getHeight();
        if (var4 > 999 | var5 > '\uffff') {
        } else {
            byte[] var8 = a(bmap, (byte) type);
            x = "CG " + var4 + " " + var5 + " " + x + " " + y + " ";
            y = "\r\n";
            byte[] var7 = x.getBytes(LanguageEncode);
            byte[] var9 = y.getBytes(LanguageEncode);
            hexString = toHexString(mergeBytes(var7, var8, var9));
        }

        return hexString;
    }

    /**
     * 将 x y 需要打印的bitmap的bytes合并
     * @param data1
     * @param data2
     * @param data3
     * @return 返回合并后的字节数组
     */
    private static byte[] mergeBytes(byte[] data1, byte[] data2, byte[] data3) {
        byte[] data4 = new byte[data1.length+data2.length+data3.length];
        System.arraycopy(data1,0,data4,0,data1.length);
        System.arraycopy(data2,0,data4,data1.length,data2.length);
        System.arraycopy(data3,0, data4, data2.length, data3.length);
        return data4;
    }

    private static byte[] a(Bitmap bitmap, byte var1) throws Exception {
        ZicoxPrinterDataCore var2;
        (var2 = new ZicoxPrinterDataCore()).HalftoneMode = var1;
        var2.ScaleMode = 0;
        return var2.PrintDataFormat(bitmap);
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
