package com.hzecool.printer.bean;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;

public class PrintImageSettingOptionsType {
    public BigDecimal imageWidth; // 图片宽度
    public BigDecimal imageHeight; // 图片的高度
    public BigDecimal margin; // 多张图片的合成间距
    public BigDecimal scale; // 比例
    public BigDecimal row; // 行数
    public BigDecimal column;// 列数

    // 可选参数
    public BigDecimal startX; // cpcl x
    public BigDecimal startY; // cpcl y

    public PrintImageSettingOptionsType() {
    }

    public PrintImageSettingOptionsType(BigDecimal imageWidth, BigDecimal imageHeight, BigDecimal margin, BigDecimal scale, BigDecimal row, BigDecimal column) {
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
        public String logoUrl;          // logo地址
        public String qrcodeContent;    // 二维码文本内容

        public PrintImageSourceType(String url, String desc, String logoUrl, String qrcodeContent) {
            this.url = url;
            this.desc = desc;
            this.logoUrl = logoUrl;
            this.qrcodeContent = qrcodeContent;
        }

        protected PrintImageSourceType(Parcel in) {
            url = in.readString();
            desc = in.readString();
            bitmap = in.readParcelable(Bitmap.class.getClassLoader());
            logoUrl = in.readString();
            qrcodeContent = in.readString();
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
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(url);
            dest.writeString(desc);
            dest.writeParcelable(bitmap, flags);
            dest.writeString(logoUrl);
            dest.writeString(qrcodeContent);
        }
    }
}
