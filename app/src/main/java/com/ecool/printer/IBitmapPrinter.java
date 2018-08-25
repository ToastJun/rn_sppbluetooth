package com.ecool.printer;

import android.graphics.Bitmap;

import com.ecool.printer.bean.PrintImageSettingOptionsType;

public interface IBitmapPrinter {

    String originalBmpToPrintHexString(Bitmap bitmap, PrintImageSettingOptionsType optionsType);
}
