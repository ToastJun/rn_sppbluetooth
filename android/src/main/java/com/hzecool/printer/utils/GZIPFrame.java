package com.hzecool.printer.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.GZIPOutputStream;

public class GZIPFrame {
    public GZIPFrame() {
    }

    public static byte[] codec(byte[] data, int len) {
        byte[] gzipData = (byte[])null;
        ByteArrayOutputStream gzipStram = new ByteArrayOutputStream();

        try {
            GZIPOutputStream zos = new GZIPOutputStream(new BufferedOutputStream(gzipStram));
            zos.write(data);
            zos.close();
        } catch (IOException var9) {
            var9.printStackTrace();
        }

        gzipData = gzipStram.toByteArray();
        long length = (long)gzipData.length;
        CRC32 crc32 = new CRC32();
        crc32.update(gzipData, 8, (int)(length - 8L - 4L));
        long crc = crc32.getValue();
        gzipData[4] = (byte)((int)(length >> 0 & 255L));
        gzipData[5] = (byte)((int)(length >> 8 & 255L));
        gzipData[6] = (byte)((int)(length >> 16 & 255L));
        gzipData[7] = (byte)((int)(length >> 24 & 255L));
        gzipData[gzipData.length - 4] = (byte)((int)(crc >> 0 & 255L));
        gzipData[gzipData.length - 3] = (byte)((int)(crc >> 8 & 255L));
        gzipData[gzipData.length - 2] = (byte)((int)(crc >> 16 & 255L));
        gzipData[gzipData.length - 1] = (byte)((int)(crc >> 24 & 255L));
        return gzipData;
    }
}
