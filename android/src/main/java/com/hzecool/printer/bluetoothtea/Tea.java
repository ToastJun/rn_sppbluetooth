package com.hzecool.printer.bluetoothtea;


/**
 * Tea算法
 * 每次操作可以处理8个字节数据
 * KEY为16字节,应为包含4个int型数的int[]，一个int为4个字节
 * */
public class Tea {
    private static long[] key = new long[]{//加密解密所用的KEY
            getUnsignedInt(0x79E8BAC5), getUnsignedInt(0x50956F22),
            getUnsignedInt(0xB2278796), getUnsignedInt(0x11333140)
    };

    private static int times = 16;

    public static long getUnsignedInt (long data){     //取低四位字节。
        return data&0xffffffffL ;
    }

    //加密
    public static byte[] encrypt(byte[] content, int offset){//times为加密轮数
        int[] tempInt = byteToInt(content, offset);
        long y = getUnsignedInt(tempInt[0]), z = getUnsignedInt(tempInt[1]), i;
        long sum = 0;
        int delta=0x9e3779b9; //这是算法标准给的值

        long limit = getUnsignedInt(delta * times);

        while (getUnsignedInt(sum) != limit) { //注意：高位和地位交叉运算，利用sum操作的低两位进行密钥的部分选择
            y += getUnsignedInt(((z << 4) ^ (z >> 5)) + (z ^ getUnsignedInt(sum)) + key[(int) (getUnsignedInt(sum) & 3)]);
            y = getUnsignedInt(y);
            sum += delta;
            z += getUnsignedInt(((y << 4) ^ (y >> 5)) + (y ^ getUnsignedInt(sum)) + key[(int) ((getUnsignedInt(sum) >> 11) & 3)]);
            z = getUnsignedInt(z);
        }

        tempInt[0]= (int) getUnsignedInt(y);
        tempInt[1]= (int) getUnsignedInt(z);
        return intToByte(tempInt, 0);
    }
    //解密
    public static byte[] decrypt(byte[] encryptContent, int offset){
        int[] tempInt = byteToInt(encryptContent, offset);
        long y = getUnsignedInt(tempInt[0]), z = getUnsignedInt(tempInt[1]), sum = 0, i;
        long delta=0x9e3779b9; //这是算法标准给的值

        sum = getUnsignedInt(delta * times);
        while (getUnsignedInt(sum) != 0) {
            z -= getUnsignedInt(((y << 4) ^ (y >> 5)) + (y ^ getUnsignedInt(sum)) + key[(int) ((getUnsignedInt(sum) >> 11) & 3)]);
            z = getUnsignedInt(z);
            sum -= delta;
            sum = getUnsignedInt(sum);
            y -= getUnsignedInt(((z << 4) ^ (z >> 5)) + (z ^ getUnsignedInt(sum)) + key[(int) (getUnsignedInt(sum) & 3)]);
            y = getUnsignedInt(y);
        }

        tempInt[0]= (int) getUnsignedInt(y);
        tempInt[1]= (int) getUnsignedInt(z);
        return intToByte(tempInt, 0);
    }
    //byte[]型数据转成int[]型数据
    private static int[] byteToInt(byte[] content, int offset){

        int[] result = new int[content.length >> 2];//除以2的n次方 == 右移n位 即 content.length / 4 == content.length >> 2
        for(int i = 0, j = offset; j < content.length; i++, j += 4){
            result[i] = transform(content[j]) | transform(content[j + 1]) << 8 |
                    transform(content[j + 2]) << 16 | (int)content[j + 3] << 24;
        }
        return result;

    }
    //int[]型数据转成byte[]型数据
    private static byte[] intToByte(int[] content, int offset){
        byte[] result = new byte[content.length << 2];//乘以2的n次方 == 左移n位 即 content.length * 4 == content.length << 2
        for(int i = 0, j = offset; j < result.length; i++, j += 4){
            result[j] = (byte)(content[i] & 0xff);
            result[j + 1] = (byte)((content[i] >> 8) & 0xff);
            result[j + 2] = (byte)((content[i] >> 16) & 0xff);
            result[j + 3] = (byte)((content[i] >> 24) & 0xff);
        }
        return result;
    }
    //若某字节为负数则需将其转成无符号正数
    private static int transform(byte temp){
        int tempInt = (int)temp;
        if(tempInt < 0){
            tempInt += 256;
        }
        return tempInt;
    }
}