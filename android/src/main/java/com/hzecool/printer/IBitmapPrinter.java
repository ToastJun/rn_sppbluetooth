package com.hzecool.printer;

import android.graphics.Bitmap;

import com.hzecool.printer.bean.PrintImageSettingOptionsType;

public interface IBitmapPrinter {

    String originalBmpToPrintHexString(Bitmap bitmap, PrintImageSettingOptionsType optionsType);
}
