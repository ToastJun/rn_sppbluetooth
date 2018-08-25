package com.ecool.printer.bean;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class PrintImageSettingOptionsType {
    public int imageWidth; // 图片宽度
    public int imageHeight; // 图片的高度
    public int margin; // 多张图片的合成间距
    public int scale; // 比例
    public int row; // 行数
    public int column;// 列数

    // 可选参数
    public int startX; // cpcl x
    public int startY; // cpcl y

    public PrintImageSettingOptionsType() {
    }

    public PrintImageSettingOptionsType(int imageWidth, int imageHeight, int margin, int scale, int row, int column) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.margin = margin;
        this.scale = scale;
        this.row = row;
        this.column = column;
    }

    public static class PrintImageSourceType implements Parcelable {
        public String url;
        public String desc;
        public Bitmap bitmap;

        public PrintImageSourceType(String url, String desc) {
            this.url = url;
            this.desc = desc;
        }

        protected PrintImageSourceType(Parcel in) {
            url = in.readString();
            desc = in.readString();
            bitmap = in.readParcelable(Bitmap.class.getClassLoader());
        }

        public static final Creator<PrintImageSourceType> CREATOR = new Creator<PrintImageSourceType>() {
            @Override
            public PrintImageSourceType createFromParcel(Parcel in) {
                return new PrintImageSourceType(in);
            }

            @Override
            public PrintImageSourceType[] newArray(int size) {
                return new PrintImageSourceType[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(url);
            parcel.writeString(desc);
            parcel.writeParcelable(bitmap, i);
        }
    }
}
