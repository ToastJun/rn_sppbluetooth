package com.hzecool.printer.bluetoothtea;

import android.util.Base64;

public class TeaUtil {
    /**
     * 将打印机发送的密文进行解密 然后返回base64的字符串
     * @param baseCode
     */
    public static String getPrinterEncryCodeWithBase64Code(String baseCode) {
        try {
            // 将base64进行转换成byte[]
            byte[] decode = Base64.decode(baseCode, Base64.DEFAULT);
            // 将decode去除头尾字节 （即去除头尾的$）
            decode = transformByteByIndex(decode, 1, decode.length - 1);
            decode = transformByteByIndex(decode, 0, decode.length - 2);

            // 进行tea解密
            byte[] decrypt = Tea.decrypt(decode, 0);
            // 用$添加到头尾
            byte[] resultByte = convertByteArray(decrypt);
            // 进行base64的加密
            return Base64.encodeToString(resultByte, Base64.DEFAULT);
        } catch (Exception e) {
            return "";
        }
    }

    private static byte[] transformByteByIndex(byte[] original,int start, int end) {
        if (end < start) {
            end = start;
        }
        int length = end - start + 1;
        byte[] result = new byte[length];
        for(int i=0; i<length; i++, start++) {
            result[i] = original[start];
        }
        return result;
    }

    /**
     * 对字节数组进行头尾添加$
     * @return
     */
    private static byte[] convertByteArray(byte[] original){
        byte[] flag = "$".getBytes();
        byte[] result = new byte[original.length+2];
        for(int i=0; i< result.length; i++) {
            if (i == 0 || i == result.length - 1) {
                result[i] = flag[0];
            } else {
                result[i] = original[i - 1];
            }
        }
        return result;
    }
}
