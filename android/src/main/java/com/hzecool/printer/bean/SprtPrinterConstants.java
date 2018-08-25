package com.hzecool.printer.bean;

public class SprtPrinterConstants {
    public static int paperWidth = 576;
    public static final int SIZE_58mm = 0;
    public static final int SIZE_80mm = 1;
    public static final int SIZE_108mm = 2;
    /**
     * 打印对齐方式
     */
    public static enum PAlign {
        START,
        CENTER,
        END,
        NONE;

        private PAlign() {
        }
    }
}
